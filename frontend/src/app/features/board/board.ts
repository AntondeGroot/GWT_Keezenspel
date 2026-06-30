import { Component, signal, inject, computed, OnInit, OnDestroy } from '@angular/core';
import { GameStatePush, MovesService, Card as CardModel } from '../../api';
import { buildBoard } from './board-geometry';
import { resolveGameSession } from '../../session';
import { Pawn } from './pawn/pawn';
import { Card } from './card/card';
import {highlightForPawn1, highlightForPawn2} from './pawn-highlight';

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

  protected readonly selectedCardUuid = signal<number | undefined>(undefined);
  protected readonly selectedPawnId = signal<string | undefined>(undefined);
  protected readonly selectedPawn2Id = signal<string | undefined>(undefined);

  private findPawn(id: string) {
    return this.state()?.pawns?.find(
      (p) => `${p.pawnId.playerId}:${p.pawnId.pawnNr}` === id,
    );
  }

  protected selectCard(uuid: number): void {
    this.selectedCardUuid.set(uuid);
    this.tryMove();}
  protected selectPawn(id: string): void {
    const card = this.hand().find((c) => c.uuid === this.selectedCardUuid());
    const isJack = card?.value === 11;
    // A Jack (switch) needs two pawns: keep the first, set the second on the
    // next click on a different pawn. Any other card just (re)selects one pawn.
    if (isJack && this.selectedPawnId() !== undefined && this.selectedPawnId() !== id) {
      this.selectedPawn2Id.set(id);
    } else {
      this.selectedPawnId.set(id);
      this.selectedPawn2Id.set(undefined);
    }
    this.tryMove();
  }

  protected readonly highlightForPawn1 = highlightForPawn1;
  protected readonly highlightForPawn2 = highlightForPawn2;
  private tryMove(): void {
    const cardUuid = this.selectedCardUuid();
    const pawnId = this.selectedPawnId();
    if (cardUuid === undefined || pawnId === undefined) return;

    const card = this.hand().find((c) => c.uuid === cardUuid);
    const pawn1 = this.findPawn(pawnId);
    if (!card || !pawn1 || !this.sessionId || !this.viewerId) return;

    // A Jack is a switch: wait for a second pawn, then send both. The server
    // classifies it as SWITCH from (jack + two on-board pawns).
    let pawn2;
    if (card.value === 11) {
      const pawn2Id = this.selectedPawn2Id();
      if (pawn2Id === undefined) return; // still waiting for the second pawn
      pawn2 = this.findPawn(pawn2Id);
      if (!pawn2) return;
    }

    const move = {
      playerId: this.viewerId,
      cardId: cardUuid,
      pawn1Id: pawn1.pawnId,
      pawn2Id: pawn2?.pawnId, // undefined for non-switch moves
      stepsPawn1: card.value,
      tempMessageType: 'MAKE_MOVE' as const,
    };

    this.movesService.makeMove(this.sessionId, this.viewerId, move).subscribe({
      // Only fly the card to the pile if the server actually accepted the move.
      next: (response) => {
        if (response.result === 'CAN_MAKE_MOVE') {
          this.pile.update((p) => [...p, card]);
        }
      },
      error: () => {}, // illegal / not your turn (400): card stays in the hand
    });
    this.selectedCardUuid.set(undefined);
    this.selectedPawnId.set(undefined);
    this.selectedPawn2Id.set(undefined);
  }
}
