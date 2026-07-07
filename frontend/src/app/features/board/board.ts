import { Component, signal, inject, computed, effect, OnInit, OnDestroy } from '@angular/core';
import {
  GameStatePush, MovesService, CardsService, Card as CardModel, MoveRequest,
  MoveResponse, Pawn as ApiPawn, PositionKey,
} from '../../api';
import { buildBoard, fanCardBacks, Pt, BoardGeometry } from './board-geometry';
import { resolveGameSession } from '../../session';
import { basePath } from '../../base-path';
import { seatColor } from '../../player-colors';
import { Pawn } from './pawn/pawn';
import { Card } from './card/card';
import { PlayerList } from '../player-list/player-list';
import {highlightForPawn1, highlightForPawn2} from './pawn-highlight';
import { PawnAndCardSelection } from './pawn-and-card-selection';
import { pawnKey } from './pawn-key';
import { Translations } from '../../i18n/translations.service';
import { TranslationKey } from '../../i18n/keys';
import { GameStore } from '../../game-store';
import { MoveRejection } from './move-rejected/move-rejection.service';
import { localRejectionKey, rejectionMessageKey } from './rejection-message';

// Cards that do something special (Ace, Four, Seven, Jack, Queen, King): they get
// a gold highlight in the hand and a hint/suggestion when hovered or selected.
const SPECIAL_CARD_VALUES = new Set([1, 4, 7, 11, 12, 13]);
const HINT_KEYS: Record<number, TranslationKey> = {
  1: 'hintAce',
  4: 'hintFour',
  7: 'hintSeven',
  11: 'hintJack',
  12: 'hintQueen',
  13: 'hintKing',
};

@Component({
  selector: 'app-board',
  imports: [Pawn, Card, PlayerList],
  templateUrl: './board.html',
  styleUrl: './board.scss',
})
export class Board implements OnInit, OnDestroy{

  ngOnInit(): void {
    if(this.sessionId){
      this.eventSource = new EventSource(this.streamUrl);
      this.eventSource.addEventListener('gamestate', (event: MessageEvent) => {
        const next = JSON.parse(event.data) as GameStatePush;

        // Animate the pawns of the last move. Detect a NEW move (its paths changed)
        // and set it up BEFORE state.set, so pawns hold their start tiles instead of
        // snapping to the server's already-final positions. Skipped on the first push.
        const mr = next.lastMoveResponse;
        const moveKey = mr
          ? JSON.stringify([mr.movePawn1, mr.movePawn2, mr.movePawnKilledByPawn1, mr.movePawnKilledByPawn2])
          : '';
        if (this.prevMoveKey !== undefined && moveKey && moveKey !== this.prevMoveKey) {
          this.animateMove(mr!);
        }
        this.prevMoveKey = moveKey;

        // Detect a deal (cards that weren't in the hand before) and start the
        // deal-in BEFORE setting state, so those cards render at the deck rather
        // than flashing at their slots first. The FIRST push after (re)connecting
        // has no baseline, so it isn't animated — a refresh mid-game just shows the
        // current hand; only a genuine new round during the session deals in.
        const cards = next.playerCards ?? [];
        const prev = this.prevHandUuids;
        if (prev) {
          const fresh = cards.filter((c) => !prev.has(c.uuid)).map((c) => c.uuid);
          if (fresh.length > 0) this.animateDeal(fresh);
        }
        this.prevHandUuids = new Set(cards.map((c) => c.uuid));
        this.gameStore.players.set(next.players ?? []);
        this.gameStore.winners.set(next.winners ?? []);
        this.state.set(next);
      });
    }
  }

  ngOnDestroy(): void {
    this.eventSource?.close();
  }
  private readonly movesService = inject(MovesService);
  private readonly cardsService = inject(CardsService);
  protected readonly i18n = inject(Translations);
  private readonly rejection = inject(MoveRejection);
  private readonly gameStore = inject(GameStore);
  private readonly session = resolveGameSession();
  private readonly sessionId = this.session.sessionId;
  private readonly viewerId = this.session.playerId;
  private readonly streamUrl = `${basePath()}/gamestates/${this.sessionId}/${this.viewerId}/stream`;
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
      seatColor(s.players!.find((p) => p.id === playerId)?.playerInt);
    return g.tiles.map((t) => ({
      ...t,
      color: t.tileNr <= 0 || t.tileNr >= 16 ? colorOf(t.playerId) : '#f2f2f2',
    }));
  });
  protected readonly pawns = computed(() => {
    const g = this.geometry();
    const s = this.state();
    if(!g || !s?.pawns) return [];
    const anim = this.pawnAnim();
    const colorOf = (playerId: string) =>
      seatColor(s.players!.find((p) => p.id === playerId)?.playerInt);
    return s.pawns.map((pawn) => {
      const pawnId = pawnKey(pawn.pawnId);
      // While a pawn is moving, its position (and step transition ms) comes from the
      // animation override instead of the server's already-final tile.
      const a = anim.get(pawnId);
      let x: number, y: number;
      if (a) {
        x = a.x;
        y = a.y;
      } else {
        const tile = pawn.currentTileId;
        const pt = g.position(tile.playerId, tile.tileNr);
        if (!pt) return null;
        x = pt.x;
        y = pt.y;
      }
      return { x, y, zIndex: Math.round(y), color: colorOf(pawn.playerId), id: pawnId, moveMs: a?.ms ?? 0 };
    }).filter(x => x !== null);
  })
  protected readonly cell  = computed(() => this.geometry()?.cellDistance ?? 0);

  // Face-down card backs for every OTHER player, fanned by their public card
  // count (nrOfCardsPerPlayer). Only counts are ever known here — never values —
  // so there is nothing to peek at in the DOM.
  protected readonly cardBacks = computed(() => {
    const g = this.geometry();
    const counts = this.state()?.nrOfCardsPerPlayer;
    if (!g || !counts) return [];
    // During a deal-in the backs start stacked at the deck (board centre) and fan
    // out to their slots, staggered — the same FLIP as the viewer's own cards.
    const dealing = this.dealtIndex().size > 0;
    const atDeck = this.atDeck();

    // Deal clockwise: order opponents by their angle around the board centre,
    // clockwise from the viewer (screen coords: clockwise = increasing atan2).
    const mid = (seg: [Pt, Pt]) => ({ x: (seg[0].x + seg[1].x) / 2, y: (seg[0].y + seg[1].y) / 2 });
    const vSeg = this.viewerId ? g.deckSegment(this.viewerId) : undefined;
    const vm = vSeg ? mid(vSeg) : { x: 300, y: 600 };
    const viewerAngle = Math.atan2(vm.y - 300, vm.x - 300);
    const opponents = Object.keys(counts)
      .filter((pid) => pid !== this.viewerId && g.deckSegment(pid))
      .map((pid) => {
        const m = mid(g.deckSegment(pid)!);
        const cw = (Math.atan2(m.y - 300, m.x - 300) - viewerAngle + 2 * Math.PI) % (2 * Math.PI);
        return { pid, cw };
      })
      .sort((a, b) => a.cw - b.cw);

    const backs: {
      key: string; x: number; y: number; rot: number; delay: number; z?: number;
    }[] = [];
    opponents.forEach(({ pid }, oi) => {
      const seat = oi + 1; // the viewer is seat 0; opponents take the next seats clockwise
      fanCardBacks(g.deckSegment(pid)!, counts[pid]).forEach((c, i) => {
        // Round-robin: i = round (card index), seat = offset within the round.
        const delay = dealing ? i * 700 + seat * 350 : 0;
        backs.push({
          key: `${pid}:${i}`,
          x: atDeck ? 315 : c.x, // deck centre while dealing
          y: atDeck ? 300 : c.y,
          rot: atDeck ? 0 : c.rotDeg,
          delay,
          // Sooner-flying backs sit on top of the deck stack (taken off the top).
          z: dealing ? 400 - Math.round(delay / 20) : undefined,
        });
      });
    });
    return backs;
  });
  private eventSource?: EventSource;

  protected readonly hand = computed(() => this.state()?.playerCards ?? []);

  // Cards the viewer has played, kept client-side so the same element can fly to
  // the pile (the server's playedCards is just strings, no uuid to track).
  // Opponents' played cards are appended here too (synthetic negative uuids) so
  // they persist on the pile after their fly-in.
  protected readonly pile = signal<CardModel[]>([]);

  // Transient face-up cards flying from an opponent's fan to the pile as they
  // play. x/y are board-% (like the cards); the `.card` transition animates them.
  protected readonly flyers = signal<
    { id: number; x: number; y: number; rot: number; scale: number; suit: number; value: number }[]
  >([]);
  private flyerSeq = 0;
  private prevCounts: Record<string, number> | undefined;
  private syntheticUuid = -1;

  // Deal-in animation: while a deal is in flight, `dealtIndex` maps each freshly
  // dealt card uuid to its stagger order; `atDeck` is true for the first frame so
  // the cards render stacked at the deck (board centre) before fanning to their
  // slots. Reading both in the `cards` computed drives the FLIP.
  private readonly dealtIndex = signal<Map<number, number>>(new Map());
  private readonly atDeck = signal(false);
  private prevHandUuids: Set<number> | undefined;

  // Pawn move animation: pawnId -> its current animated pixel position + the
  // transition duration for the current step. Present only while a pawn is moving.
  protected readonly pawnAnim = signal<Map<string, { x: number; y: number; ms: number }>>(new Map());
  private prevMoveKey: string | undefined;

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
    // Deal-in FLIP: a freshly dealt card renders at the deck (board centre) for the
    // first frame, then transitions to its slot with a per-card stagger delay.
    const dealt = this.dealtIndex();
    const atDeck = this.atDeck();
    return [...handCards, ...pile]
      .sort((a, b) => a.uuid - b.uuid)
      .map((c) => {
        const t = target.get(c.uuid)!;
        const dealOrder = dealt.get(c.uuid);
        const dealing = dealOrder !== undefined;
        const useDeck = dealing && atDeck;
        // Round-robin deal: delay by ROUND (the card's index in the hand). The
        // viewer is seat 0, so no per-seat offset. 700ms between rounds (slow).
        const dealDelay = dealing ? (dealOrder ?? 0) * 700 : 0;
        return {
          uuid: c.uuid,
          suit: c.suit,
          value: c.value,
          inPile: pileUuids.has(c.uuid), // played cards are inert (no click / hover)
          // Only hand cards are highlighted; a played/pile card is not "special" anymore.
          special: !pileUuids.has(c.uuid) && SPECIAL_CARD_VALUES.has(c.value),
          x: useDeck ? 52.5 : t.x,
          y: useDeck ? 50 : t.y,
          rot: t.rot,
          scale: useDeck ? 0.3 : t.scale, // deck-sized (≈ the 5% backs) then grows to hand size
          // While dealing, the sooner a card flies (lower delay) the higher it sits,
          // so it's taken off the TOP of the deck stack.
          z: dealing ? 400 - Math.round(dealDelay / 20) : t.z,
          dealDelay,
          dealing, // true while this card is dealing in (drives the flip)
        };
      });
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
          id: pawnKey(p.pawnId),
          playerId: p.pawnId.playerId,
          tileNr: p.currentTileId.tileNr,
        })),
      );
      this.selection.setHand((s?.playerCards ?? []).map((c) => ({ id: c.uuid, value: c.value })));
      this.touch();
    });

    // Detect when another player plays a single card (count −1) and fly it from
    // their fan to the pile. A forfeit drops the count by more than one, so it is
    // deliberately not animated.
    effect(() => {
      const counts = this.state()?.nrOfCardsPerPlayer;
      const played = this.state()?.playedCards ?? [];
      const prev = this.prevCounts;
      if (counts && prev) {
        for (const [pid, n] of Object.entries(counts)) {
          if (pid === this.viewerId) continue;
          const before = prev[pid];
          if (before === undefined) continue;
          const dropped = before - n;
          if (dropped === 1) this.flyOpponentCard(pid, before, played); // played one card
          else if (dropped > 1) this.flyOpponentForfeit(pid, before, dropped, played); // forfeit
        }
      }
      this.prevCounts = counts ? { ...counts } : undefined;
    });
  }

  /** Kick off the deal-in FLIP for the given freshly-dealt card uuids. */
  private animateDeal(uuids: number[]): void {
    const order = new Map<number, number>();
    uuids.forEach((u, i) => order.set(u, i));
    this.dealtIndex.set(order);
    this.atDeck.set(true); // first frame: stacked at the deck
    // Release next frame so they transition out to their slots (staggered by delay).
    requestAnimationFrame(() => requestAnimationFrame(() => this.atDeck.set(false)));
    // Clear once the last card has finished (last round's delay + the transition).
    setTimeout(() => this.dealtIndex.set(new Map()), uuids.length * 700 + 2000);
  }

  // --- Pawn move animation (ported from PawnAnimation) ---------------------

  /** Walk each of a move's pawns along its path; killed pawns go home after the killer. */
  private animateMove(mr: MoveResponse): void {
    const g = this.geometry();
    if (!g) return;
    const d1 = this.animatePawnPath(g, mr.pawn1, mr.movePawn1, 0);
    const d2 = this.animatePawnPath(g, mr.pawn2, mr.movePawn2, 0);
    this.animatePawnPath(g, mr.pawnKilledByPawn1, mr.movePawnKilledByPawn1, d1);
    this.animatePawnPath(g, mr.pawnKilledByPawn2, mr.movePawnKilledByPawn2, d2);
  }

  /**
   * Hold a pawn at its path start (so the server's final tile doesn't snap), then
   * walk its waypoints after `delayMs`. Returns the total animation time in ms.
   */
  private animatePawnPath(
    g: BoardGeometry,
    pawn: ApiPawn | undefined,
    move: PositionKey[] | undefined,
    delayMs: number,
  ): number {
    if (!pawn || !move || move.length < 2) return 0;
    const id = pawnKey(pawn.pawnId);
    const points: Pt[] = [];
    for (const t of move) {
      const p = g.position(t.playerId, t.tileNr);
      if (p) points.push(p);
    }
    if (points.length < 2) return 0;

    let total = 0;
    for (let i = 1; i < points.length; i++) {
      total += Math.hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y);
    }
    const speed = this.moveSpeed(total); // px/ms
    this.setPawnAnim(id, points[0].x, points[0].y, 0); // hold at the start now
    const walk = () =>
      requestAnimationFrame(() => requestAnimationFrame(() => this.stepPawnPath(id, points, 1, speed)));
    if (delayMs > 0) setTimeout(walk, delayMs);
    else walk();
    return Math.round(total / speed);
  }

  private stepPawnPath(id: string, points: Pt[], i: number, speed: number): void {
    if (i >= points.length) {
      this.clearPawnAnim(id); // done — settle onto the server's final tile
      return;
    }
    const d = Math.hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y);
    const ms = Math.max(16, Math.round(d / speed));
    this.setPawnAnim(id, points[i].x, points[i].y, ms);
    setTimeout(() => this.stepPawnPath(id, points, i + 1, speed), ms);
  }

  /** px/ms — faster over longer paths (ported from calculateSpeed). */
  private moveSpeed(distance: number): number {
    if (distance > 400) return 0.16;
    if (distance > 200) return 0.12;
    return 0.1;
  }

  private setPawnAnim(id: string, x: number, y: number, ms: number): void {
    this.pawnAnim.update((m) => new Map(m).set(id, { x, y, ms }));
  }
  private clearPawnAnim(id: string): void {
    this.pawnAnim.update((m) => {
      const n = new Map(m);
      n.delete(id);
      return n;
    });
  }

  /**
   * Spawn a card flying from `from` to the pile centre, then drop the flyer and
   * leave `landCard` on the pile. Shared by opponents' plays and the viewer's own.
   */
  private spawnFlyer(
    from: { x: number; y: number; rot: number },
    startScale: number,
    suit: number,
    value: number,
    landCard: CardModel,
  ): void {
    const id = ++this.flyerSeq;
    this.flyers.update((f) => [
      ...f,
      { id, x: from.x, y: from.y, rot: from.rot, scale: startScale, suit, value },
    ]);
    // Next frame, fly to the pile centre at pile size.
    requestAnimationFrame(() =>
      requestAnimationFrame(() =>
        this.flyers.update((f) =>
          f.map((fl) => (fl.id === id ? { ...fl, x: 52.5, y: 50, rot: 0, scale: 0.6 } : fl)),
        ),
      ),
    );
    setTimeout(() => {
      this.flyers.update((f) => f.filter((fl) => fl.id !== id));
      this.pile.update((p) => [...p, landCard]);
    }, 600);
  }

  /** Animate an opponent's just-played card from its fan slot to the pile. */
  private flyOpponentCard(playerId: string, fanCount: number, played: string[]): void {
    const segment = this.geometry()?.deckSegment(playerId);
    const last = played[played.length - 1];
    if (!segment || !last) return;
    const [suit, value] = last.split('_').map(Number);
    const slot = fanCardBacks(segment, fanCount).at(-1); // the outermost card leaves
    if (!slot) return;
    // Back-sized start (0.3 of a full card ≈ a 30px back); synthetic pile card.
    this.spawnFlyer({ x: slot.x / 6, y: slot.y / 6, rot: slot.rotDeg }, 0.3, suit, value, {
      uuid: this.syntheticUuid--,
      suit,
      value,
    } as CardModel);
  }

  /** Animate the viewer's own just-played card from its (snapshotted) hand slot. */
  private flyOwnCard(card: CardModel, from: { x: number; y: number }): void {
    this.spawnFlyer({ x: from.x, y: from.y, rot: 0 }, 1, card.suit ?? 0, card.value ?? 1, card);
  }

  /** Forfeit: fly all of an opponent's cards from their fan to the pile, staggered. */
  private flyOpponentForfeit(playerId: string, fanCount: number, dropped: number, played: string[]): void {
    const segment = this.geometry()?.deckSegment(playerId);
    if (!segment) return;
    const fan = fanCardBacks(segment, fanCount);
    const forfeited = played.slice(played.length - dropped); // the discarded cards (public)
    forfeited.forEach((str, i) => {
      const [suit, value] = str.split('_').map(Number);
      const slot = fan[i] ?? fan.at(-1);
      if (!slot) return;
      setTimeout(
        () =>
          this.spawnFlyer({ x: slot.x / 6, y: slot.y / 6, rot: slot.rotDeg }, 0.3, suit, value, {
            uuid: this.syntheticUuid--,
            suit,
            value,
          } as CardModel),
        i * 120, // small stagger between cards
      );
    });
  }

  private findPawn(id: string) {
    return this.state()?.pawns?.find(
      (p) => pawnKey(p.pawnId) === id,
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

  // The backend allows a forfeit only when the player has no legal move; when they
  // do have one they must play it, so the Forfeit button is disabled. Defaults to
  // enabled until a state with the flag arrives (don't lock the player out).
  protected readonly canForfeit = computed(() => this.state()?.canForfeit ?? true);

  protected selectCard(uuid: number): void {
    const handCard = this.hand().find((c) => c.uuid === uuid);
    if (!handCard) return;
    this.selection.setCard({ id: handCard.uuid, value: handCard.value });
    this.touch();
    this.checkMove();
  }

  /** The card value currently hovered in the hand (drives the hint), or null. */
  private readonly hoveredCardValue = signal<number | null>(null);
  protected hoverCard(value: number | null): void {
    this.hoveredCardValue.set(value);
  }

  /**
   * Hint/suggestion for the special card the player is eyeing: the hovered card,
   * falling back to the selected card when nothing is hovered. Empty for a regular
   * card — only Ace/Four/Seven/Jack/Queen/King have a hint (mirrors the GWT
   * updateCardHint). `rev()` makes it recompute when the selection changes.
   */
  protected readonly hint = computed(() => {
    this.rev();
    const value = this.hoveredCardValue() ?? this.selection.getCard()?.value ?? null;
    const key = value == null ? undefined : HINT_KEYS[value];
    return key ? this.i18n.t(key) : '';
  });

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

  // The − / + steppers; setNr...ForSplit wraps 8→0 and −1→7, like the GWT.
  protected stepPawn1(delta: number): void {
    this.onStepsPawn1(String(this.stepsPawn1() + delta));
  }
  protected stepPawn2(delta: number): void {
    this.onStepsPawn2(String(this.stepsPawn2() + delta));
  }

  private playerColor(playerId: string): string {
    return seatColor(this.state()?.players?.find((p) => p.id === playerId)?.playerInt);
  }

  // Step-box label + input-border colours match each pawn's board highlight colour
  // (which depends on the pawn's own colour), like the GWT updateStepBoxColors.
  protected readonly pawn1Highlight = computed(() => {
    const id = this.pawn1Id();
    return id ? highlightForPawn1(this.playerColor(id.split(':')[0])) : undefined;
  });
  protected readonly pawn2Highlight = computed(() => {
    const id = this.pawn2Id();
    return id ? highlightForPawn2(this.playerColor(id.split(':')[0])) : undefined;
  });

  protected readonly highlightForPawn1 = highlightForPawn1;
  protected readonly highlightForPawn2 = highlightForPawn2;

  // Submit the current selection (the green "play card" button). The server
  // re-derives the move type from the card + pawns, so we just send the pieces.
  protected playCard(): void {
    const card = this.selection.getCard();
    const pawn1 = this.selection.getPawn1();
    if (!card || !pawn1 || !this.sessionId || !this.viewerId) return;

    // Explain selections the server would reject with a bare 400 (no reason),
    // instead of misreporting them as "not your turn".
    const localKey = localRejectionKey(pawn1.tileNr, card.value);
    if (localKey) {
      this.rejection.show(this.i18n.t(localKey));
      return;
    }

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
    this.previewTiles.set(new Set()); // stop the move preview while submitting
    // Snapshot the played card's current hand slot BEFORE sending: the server push
    // will have removed it from the hand by the time the move is confirmed, so on
    // success we fly a clone from here (ported from the GWT captureCardStartPos).
    const start = card ? this.cards().find((c) => c.uuid === card.uuid) : undefined;
    this.touch();
    this.movesService.makeMove(this.sessionId, this.viewerId, move).subscribe({
      next: (response) => {
        if (response.result === 'CAN_MAKE_MOVE') {
          this.selection.reset(); // accepted → clear the selection
          if (card && start) this.flyOwnCard(card, { x: start.x, y: start.y });
          else if (card) this.pile.update((p) => [...p, card]);
          this.touch();
        } else {
          // Rejected by the rules (still a 200): explain why, and keep the
          // selection so the player can adjust it.
          this.rejection.show(
            this.i18n.t(rejectionMessageKey(response.rejectionReason), response.rejectionDetail ?? ''),
          );
        }
      },
      error: () => {
        // 400 — typically not your turn (or a stale double-submit).
        this.rejection.show(this.i18n.t('moveRejectedNotYourTurn'));
      },
    });
  }

  // Forfeit the turn (the amber forfeit button) — DELETE /cards/{session}/{player}.
  protected forfeit(): void {
    if (!this.sessionId || !this.viewerId) return;
    // Snapshot my hand-card positions BEFORE the server push clears them, then fly
    // each to the pile, staggered (same as an opponent's forfeit).
    const myCards = this.cards().filter((c) => !c.inPile);
    this.cardsService.playerForfeits(this.sessionId, this.viewerId).subscribe({ error: () => {} });
    this.selection.reset();
    this.previewTiles.set(new Set());
    this.touch();
    myCards.forEach((c, i) =>
      setTimeout(
        () =>
          this.spawnFlyer({ x: c.x, y: c.y, rot: 0 }, 1, c.suit, c.value, {
            uuid: this.syntheticUuid--,
            suit: c.suit,
            value: c.value,
          } as CardModel),
        i * 120,
      ),
    );
  }
}
