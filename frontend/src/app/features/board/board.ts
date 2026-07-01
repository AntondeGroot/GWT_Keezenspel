import { Component, signal, inject, computed, effect, OnInit, OnDestroy } from '@angular/core';
import { GameStatePush, MovesService, CardsService, Card as CardModel, MoveRequest } from '../../api';
import { buildBoard } from './board-geometry';
import { resolveGameSession } from '../../session';
import { Pawn } from './pawn/pawn';
import { Card } from './card/card';
import {highlightForPawn1, highlightForPawn2} from './pawn-highlight';
import { PawnAndCardSelection } from './pawn-and-card-selection';

@Component({
  selector: 'app-board',
  imports: [Pawn, Card],
  templateUrl: './board.html',
  styleUrl: './board.scss',
})
export class Board implements OnInit, OnDestroy{

  ngOnInit(): void {
    if(this.sessionId){
      this.eventSource = new EventSource(this.streamUrl);
      this.eventSource.addEventListener('gamestate', (event: MessageEvent) => {this.state.set(JSON.parse(event.data))});
    }
  }

  ngOnDestroy(): void {
    this.eventSource?.close();
  }
  private readonly movesService = inject(MovesService);
  private readonly cardsService = inject(CardsService);
  private readonly session = resolveGameSession();
  private readonly sessionId = this.session.sessionId;
  private readonly viewerId = this.session.playerId;
  private readonly streamUrl = `/gamestates/${this.sessionId}/${this.viewerId}/stream`;
  protected readonly state = signal<GameStatePush | undefined>(undefined);
  protected readonly geometry = computed(() => {
    const s = this.state();
    const viewer = this.viewerId;
    if (!s?.players || !viewer) return undefined;
    return buildBoard(
      s.players.map((p) => ({ id: p.id, playerInt: p.playerInt! })),
      viewer,
    );
  });
  protected readonly tiles = computed(() => {
    const g = this.geometry();
    const s = this.state();
    if (!g || !s?.players) return [];
    const colorOf = (playerId: string) =>
      s.players!.find((p) => p.id === playerId)?.color || '#f2f2f2';
    return g.tiles.map((t) => ({
      ...t,
      color: t.tileNr <= 0 || t.tileNr >= 16 ? colorOf(t.playerId) : '#f2f2f2',
    }));
  });
  protected readonly pawns = computed(() => {
    const g = this.geometry();
    const s = this.state();
    if(!g || !s?.pawns) return [];
    const colorOf = (playerId: string) =>
      s.players!.find((p) => p.id === playerId)?.color || '#f2f2f2';
    return s.pawns.map((pawn) => {
      const tile = pawn.currentTileId;
      const pt = g.position(tile.playerId, tile.tileNr)
      const colorOfPawn = colorOf(pawn.playerId);
      const pawnId = `${pawn.pawnId.playerId}:${pawn.pawnId.pawnNr}`;
      if(!pt) return null;
      return {x: pt.x, y: pt.y, zIndex: Math.round(pt.y), color: colorOfPawn, id: pawnId}}).filter(x => x !== null);
  })
  protected readonly cell  = computed(() => this.geometry()?.cellDistance ?? 0);
  private eventSource?: EventSource;

  protected readonly hand = computed(() => this.state()?.playerCards ?? []);

  // Cards the viewer has played, kept client-side so the same element can fly to
  // the pile (the server's playedCards is just strings, no uuid to track).
  protected readonly pile = signal<CardModel[]>([]);

  // One list of every card (hand + pile), each with a target position. Moving a
  // card from a hand slot to the pile target makes the same element animate there.
  protected readonly cards = computed(() => {
    const pile = this.pile();
    const pileUuids = new Set(pile.map((c) => c.uuid));
    const handCards = this.hand().filter((c) => !pileUuids.has(c.uuid));
    const n = handCards.length;

    // Per-uuid target position: hand slot (fanned row below the board) or pile slot.
    // z follows PLAY ORDER (pile index) so the newest card is on top, regardless of
    // the DOM order (which is sorted by uuid for stable transitions).
    const target = new Map<number, { x: number; y: number; rot: number; scale: number; z?: number }>();
    handCards.forEach((c, i) =>
      target.set(c.uuid, { x: 50 + (i - (n - 1) / 2) * 18, y: 116, rot: 0, scale: 1 }),
    );
    pile.forEach((c, i) => {
      const angle = ((90 + i * 45) * Math.PI) / 180; // each card +45° around the circle
      target.set(c.uuid, {
        x: (315 + 10 * Math.cos(angle)) / 6, // GWT: pile centre 315, radius 10 → board %
        y: (300 + 10 * Math.sin(angle)) / 6, // centre 300
        rot: 0,
        scale: 0.6, // pile cards ~60px vs 100px hand
        z: 100 + i, // newest played card stacks on top
      });
    });

    // Stable DOM order (by uuid) so playing a card never reorders the list — only
    // its target changes, so the same element transitions cleanly every time.
    return [...handCards, ...pile]
      .sort((a, b) => a.uuid - b.uuid)
      .map((c) => ({ uuid: c.uuid, suit: c.suit, value: c.value, ...target.get(c.uuid)! }));
  });

  // --- Selection: delegated to the ported PawnAndCardSelection state machine ---
  private readonly selection = new PawnAndCardSelection();
  // The selection is a plain mutable object, not a signal; bump `rev` after every
  // change so the computed selectors below recompute and the view updates.
  private readonly rev = signal(0);
  private touch(): void {
    this.rev.update((v) => v + 1);
  }

  constructor() {
    // Feed the selection the current player, pawns (with live positions) and hand
    // from every server push, so it can validate moves and auto-select cards.
    effect(() => {
      const s = this.state();
      if (this.viewerId) this.selection.setPlayerId(this.viewerId);
      this.selection.updatePawns(
        (s?.pawns ?? []).map((p) => ({
          id: `${p.pawnId.playerId}:${p.pawnId.pawnNr}`,
          playerId: p.pawnId.playerId,
          tileNr: p.currentTileId.tileNr,
        })),
      );
      this.selection.setHand((s?.playerCards ?? []).map((c) => ({ id: c.uuid, value: c.value })));
      this.touch();
    });
  }

  private findPawn(id: string) {
    return this.state()?.pawns?.find(
      (p) => `${p.pawnId.playerId}:${p.pawnId.pawnNr}` === id,
    );
  }

  // Reactive selectors over the selection (reading rev() makes them recompute).
  protected readonly selectedCardUuid = computed(() => (this.rev(), this.selection.getCard()?.id));
  protected readonly pawn1Id = computed(() => (this.rev(), this.selection.getPawnId1()));
  protected readonly pawn2Id = computed(() => (this.rev(), this.selection.getPawnId2()));
  protected readonly splitVisible = computed(() => (this.rev(), this.selection.isSplitBoxesVisible()));
  protected readonly stepsPawn1 = computed(() => (this.rev(), this.selection.getNrStepsPawn1()));
  protected readonly stepsPawn2 = computed(() => (this.rev(), this.selection.getNrStepsPawn2()));
  protected readonly canPlay = computed(
    () => (this.rev(), this.selection.getCard() != null && this.selection.getPawn1() != null),
  );

  protected selectCard(uuid: number): void {
    const handCard = this.hand().find((c) => c.uuid === uuid);
    if (!handCard) return;
    this.selection.setCard({ id: handCard.uuid, value: handCard.value });
    this.touch();
    this.checkMove();
  }

  protected selectPawn(id: string): void {
    this.selection.addPawnById(id);
    this.touch();
    this.checkMove();
  }

  // Tiles the previewed move would land on (key = "playerId:tileNr"); they pulse.
  protected readonly previewTiles = signal<Set<string>>(new Set());
  protected isPreview(playerId: string, tileNr: number): boolean {
    return this.previewTiles().has(`${playerId}:${tileNr}`);
  }

  // Preview the current selection: ask the server which tile(s) it would land on
  // and pulse them. When a 7-split first forms, adopt the recommended allocation
  // once, then re-check to preview it. Mirrors the GWT presenter's checkMove().
  private checkMove(): void {
    const card = this.selection.getCard();
    const pawn1 = this.selection.getPawn1();
    if (!card || !pawn1 || !this.sessionId || !this.viewerId) {
      this.previewTiles.set(new Set());
      return;
    }
    const apiPawn1 = this.findPawn(pawn1.id);
    if (!apiPawn1) return;
    const pawn2 = this.selection.getPawn2();
    const apiPawn2 = pawn2 ? this.findPawn(pawn2.id) : undefined;

    this.movesService
      .checkMove(this.sessionId, this.viewerId, {
        playerId: this.viewerId,
        cardId: card.id,
        pawn1Id: apiPawn1.pawnId,
        pawn2Id: apiPawn2?.pawnId,
        stepsPawn1: this.selection.getNrStepsPawn1(),
        stepsPawn2: this.selection.getNrStepsPawn2(),
        tempMessageType: 'CHECK_MOVE',
      })
      .subscribe({
        next: (res) => {
          // First time a 7-split forms: adopt the recommended split, then re-check.
          if (this.selection.isSplitDefaultPending()) {
            this.selection.clearSplitDefaultPending();
            const s1 = res.recommendedStepsPawn1 ?? -1;
            const s2 = res.recommendedStepsPawn2 ?? -1;
            if (s1 >= 0 && s2 >= 0) {
              this.selection.setNrStepsPawn1(s1);
              this.selection.setNrStepsPawn2(s2);
              this.touch();
              this.checkMove();
              return;
            }
          }
          // Highlight the landing tile(s) — the last tile of each pawn's path.
          this.previewTiles.set(
            new Set((res.tiles ?? []).map((t) => `${t.playerId}:${t.tileNr}`)),
          );
        },
        error: () => {},
      });
  }

  // The 7-split step inputs (shown when splitVisible()).
  protected onStepsPawn1(value: string): void {
    this.selection.setNrStepsPawn1ForSplit(value);
    this.touch();
    this.checkMove();
  }
  protected onStepsPawn2(value: string): void {
    this.selection.setNrStepsPawn2ForSplit(value);
    this.touch();
    this.checkMove();
  }

  protected readonly highlightForPawn1 = highlightForPawn1;
  protected readonly highlightForPawn2 = highlightForPawn2;

  // Submit the current selection (the green "play card" button). The server
  // re-derives the move type from the card + pawns, so we just send the pieces.
  protected playCard(): void {
    const card = this.selection.getCard();
    const pawn1 = this.selection.getPawn1();
    if (!card || !pawn1 || !this.sessionId || !this.viewerId) return;
    const apiPawn1 = this.findPawn(pawn1.id);
    if (!apiPawn1) return;
    const pawn2 = this.selection.getPawn2();
    const apiPawn2 = pawn2 ? this.findPawn(pawn2.id) : undefined;
    const handCard = this.hand().find((c) => c.uuid === card.id);

    this.send(handCard, {
      playerId: this.viewerId,
      cardId: card.id,
      pawn1Id: apiPawn1.pawnId,
      pawn2Id: apiPawn2?.pawnId,
      stepsPawn1: this.selection.getNrStepsPawn1(),
      stepsPawn2: this.selection.getNrStepsPawn2(),
      tempMessageType: 'MAKE_MOVE',
    });
  }

  private send(card: CardModel | undefined, move: MoveRequest): void {
    if (!this.sessionId || !this.viewerId) return;
    this.movesService.makeMove(this.sessionId, this.viewerId, move).subscribe({
      // Only fly the card to the pile if the server actually accepted the move.
      next: (response) => {
        if (response.result === 'CAN_MAKE_MOVE' && card) {
          this.pile.update((p) => [...p, card]);
        }
      },
      error: () => {}, // illegal / not your turn (400): card stays in the hand
    });
    this.selection.reset();
    this.previewTiles.set(new Set());
    this.touch();
  }

  // Forfeit the turn (the amber forfeit button) — DELETE /cards/{session}/{player}.
  protected forfeit(): void {
    if (!this.sessionId || !this.viewerId) return;
    this.cardsService.playerForfeits(this.sessionId, this.viewerId).subscribe({ error: () => {} });
    this.selection.reset();
    this.previewTiles.set(new Set());
    this.touch();
  }
}
