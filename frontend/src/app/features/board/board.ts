import { Component, signal, inject, computed, effect, OnInit, OnDestroy } from '@angular/core';
import {
  GameStatePush,
  MovesService,
  CardsService,
  Card as CardModel,
  MoveRequest,
  MoveResponse,
  Pawn as ApiPawn,
  PositionKey,
  Trade,
} from '../../api';
import { buildBoard, fanCardBacks, Pt, BoardGeometry } from './board-geometry';
import { resolveGameSession } from '../../session';
import { basePath } from '../../base-path';
import { seatColor } from '../../player-colors';
import { SoundService } from '../../sound.service';
import { Pawn } from './pawn/pawn';
import { CardLayer } from '../../card-table/card-layer';
import { CardTable } from '../../card-table/card-table';
import { DefaultCardPositioner } from '../../card-table/default-positioner';
import { CardBackVM } from '../../card-table/card-table.types';
import { PlayerList } from '../player-list/player-list';
import { TradePanel } from './trade-panel/trade-panel';
import { highlightForPawn1, highlightForPawn2 } from './pawn-highlight';
import { PawnAnimator } from './pawn-animator';
import { PawnAndCardSelection } from './pawn-and-card-selection';
import { teammateCaptureKeys } from './teammate-capture';
import { pawnKey } from './pawn-key';
import { Translations } from '../../i18n/translations.service';
import { TranslationKey } from '../../i18n/keys';
import { GameStore } from '../../game-store';
import { MoveRejection } from './move-rejected/move-rejection.service';
import { TeamHandoff } from './team-handoff/team-handoff.service';
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
  imports: [Pawn, CardLayer, PlayerList, TradePanel],
  templateUrl: './board.html',
  styleUrl: './board.scss',
})
export class Board implements OnInit, OnDestroy {
  private reconnectTimer?: ReturnType<typeof setTimeout>;
  private destroyed = false;

  ngOnInit(): void {
    if (this.sessionId) this.connectStream();
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    clearTimeout(this.reconnectTimer);
    this.eventSource?.close();
  }

  /**
   * Open the game-state SSE stream. The server sends a fresh personalized snapshot on every
   * (re)subscribe, so if the browser gives up on the stream (readyState CLOSED) we re-establish
   * it after a short backoff — otherwise a dropped stream leaves a STALE board that then rejects
   * your moves as "not your turn" (ported from the GWT presenter's onError resync).
   */
  private connectStream(): void {
    if (!this.sessionId || this.destroyed) return;
    this.eventSource?.close();
    // The reconnect snapshot is a fresh baseline, not a new move/deal — don't animate it.
    this.prevMoveKey = undefined;
    this.prevHandUuids = undefined;

    const es = new EventSource(this.streamUrl);
    this.eventSource = es;
    es.addEventListener('gamestate', (event: MessageEvent) =>
      this.handleGameState(JSON.parse(event.data) as GameStatePush),
    );
    es.onerror = () => {
      // CONNECTING → the browser is auto-retrying, leave it. CLOSED → it gave up, so re-open.
      if (es.readyState === EventSource.CLOSED) {
        clearTimeout(this.reconnectTimer);
        this.reconnectTimer = setTimeout(() => this.connectStream(), 2000);
      }
    };
  }

  private handleGameState(next: GameStatePush): void {
    // Animate the pawns of the last move. Detect a NEW move (its paths changed)
    // and set it up BEFORE state.set, so pawns hold their start tiles instead of
    // snapping to the server's already-final positions. Skipped on the first push.
    const mr = next.lastMoveResponse;
    const moveKey = mr
      ? JSON.stringify([
          mr.movePawn1,
          mr.movePawn2,
          mr.movePawnKilledByPawn1,
          mr.movePawnKilledByPawn2,
        ])
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
      // A new round deals a fresh batch, growing the hand. Card uuids are REUSED across rounds, so
      // the finished round's pile must be cleared here — otherwise a redealt card whose uuid still
      // lingers in the (never-otherwise-cleared) pile is filtered out of the hand and silently
      // vanishes. Gate on the hand actually growing so a net-neutral trade (1 out, 1 in) doesn't
      // wipe the current round's pile.
      if (cards.length > prev.size) this.cardTable.clearPile();
      const fresh = cards.filter((c) => !prev.has(c.uuid)).map((c) => c.uuid);
      if (fresh.length > 0) this.cardTable.dealIn(fresh);
    }
    this.prevHandUuids = new Set(cards.map((c) => c.uuid));
    this.gameStore.players.set(next.players ?? []);
    this.gameStore.winners.set(next.winners ?? []);
    this.state.set(next);
  }
  private readonly movesService = inject(MovesService);
  private readonly cardsService = inject(CardsService);
  protected readonly i18n = inject(Translations);
  private readonly rejection = inject(MoveRejection);
  private readonly teamHandoff = inject(TeamHandoff);
  private readonly sound = inject(SoundService);
  private readonly gameStore = inject(GameStore);
  private readonly session = resolveGameSession();
  protected readonly sessionId = this.session.sessionId;
  protected readonly viewerId = this.session.playerId;
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
    if (!g || !s?.pawns) return [];
    const anim = this.pawnAnimator.positions();
    const playerOf = (playerId: string) => s.players!.find((p) => p.id === playerId);
    const colorOf = (playerId: string) => seatColor(playerOf(playerId)?.playerInt);
    const teamOf = (playerId: string) => playerOf(playerId)?.teamId ?? null;
    return s.pawns
      .map((pawn) => {
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
        return {
          x,
          y,
          zIndex: Math.round(y),
          color: colorOf(pawn.playerId),
          teamId: teamOf(pawn.playerId),
          id: pawnId,
          moveMs: a?.ms ?? 0,
        };
      })
      .filter((x) => x !== null);
  });
  protected readonly cell = computed(() => this.geometry()?.cellDistance ?? 0);

  // Face-down card backs for every OTHER player, fanned by their public card
  // count (nrOfCardsPerPlayer). Only counts are ever known here — never values —
  // so there is nothing to peek at in the DOM.
  protected readonly cardBacks = computed(() => {
    const g = this.geometry();
    const counts = this.state()?.nrOfCardsPerPlayer;
    if (!g || !counts) return [];
    // During a deal-in the backs start stacked at the deck (board centre) and fan
    // out to their slots, staggered — the same FLIP as the viewer's own cards.
    const dealing = this.cardTable.dealing();
    const atDeck = this.cardTable.stacked();

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

    // Positions are in the card-layer's %-space (board px / 6), like the hand + pile.
    const backs: CardBackVM[] = [];
    opponents.forEach(({ pid }, oi) => {
      const seat = oi + 1; // the viewer is seat 0; opponents take the next seats clockwise
      fanCardBacks(g.deckSegment(pid)!, counts[pid]).forEach((c, i) => {
        // Round-robin: i = round (card index), seat = offset within the round.
        const dealDelay = dealing ? i * 700 + seat * 350 : 0;
        backs.push({
          key: `${pid}:${i}`,
          x: (atDeck ? 315 : c.x) / 6, // deck centre while dealing
          y: (atDeck ? 300 : c.y) / 6,
          rot: atDeck ? 0 : c.rotDeg,
          dealDelay,
          // Sooner-flying backs sit on top of the deck stack (taken off the top).
          z: dealing ? 400 - Math.round(dealDelay / 20) : undefined,
        });
      });
    });
    return backs;
  });
  private eventSource?: EventSource;

  protected readonly hand = computed(() => this.state()?.playerCards ?? []);

  // The reusable card table: owns the pile, the flyer layer and the deal-in FLIP, and computes
  // each card's on-table position (hand fan / pile / deck). Keezen uses the default layout; the
  // board drives it (dealIn / flyToPile / clearPile) from its GameStatePush diffing below.
  private readonly positioner = new DefaultCardPositioner();
  protected readonly cardTable = new CardTable(() => this.hand(), this.positioner);
  // Which card values get the gold "special" highlight (Ace/Four/Seven/Jack/Queen/King).
  protected readonly isSpecial = (value: number): boolean => SPECIAL_CARD_VALUES.has(value);

  private prevCounts: Record<string, number> | undefined;
  private prevHandUuids: Set<number> | undefined;

  // Pawn move animation: a small engine that walks pawns along pixel waypoints and exposes their
  // live positions; the `pawns` computed reads those to place a pawn mid-move (see below).
  private readonly pawnAnimator = new PawnAnimator();
  private prevMoveKey: string | undefined;

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
      // In team play you may also move your teammate's pawns once all your own are home.
      this.selection.setControllablePlayerIds(this.controllablePlayerIds());
      this.selection.updatePawns(
        (s?.pawns ?? []).map((p) => ({
          id: pawnKey(p.pawnId),
          playerId: p.pawnId.playerId,
          tileNr: p.currentTileId.tileNr,
        })),
      );
      // Exclude cards already on the pile so a played card can never be (auto-)selected —
      // the display hand does the same (see `handCards`). Guards against a played card
      // lingering in the server's playerCards for a beat after it flew to the pile.
      const pileUuids = new Set(this.cardTable.pile().map((c) => c.uuid));
      this.selection.setHand(
        (s?.playerCards ?? [])
          .filter((c) => !pileUuids.has(c.uuid))
          .map((c) => ({ id: c.uuid, value: c.value })),
      );
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
          if (!(pid in prev)) continue;
          const before = prev[pid];
          const dropped = before - n;
          if (dropped === 1)
            this.flyOpponentCard(pid, before, played); // played one card
          else if (dropped > 1) this.flyOpponentForfeit(pid, before, dropped, played); // forfeit
        }
      }
      this.prevCounts = counts ? { ...counts } : undefined;
    });

    // Announce the hand-off once: the moment your own pawns are all home in a team game, you may
    // start playing your teammate's pawns. Fires on the transition, like the card-fly effect above.
    effect(() => {
      const allHome = this.viewerOwnPawnsAllHome();
      if (allHome && !this.prevOwnPawnsHome) {
        this.teamHandoff.show(this.i18n.t('teamHandoffTitle'), this.i18n.t('teamHandoffMessage'));
      }
      this.prevOwnPawnsHome = allHome;
    });

    // Team trade outcome (requester side): when your outgoing offer resolves, tell you whether
    // your teammate gave you a card. An accept removes your offered card from your hand; a reject
    // leaves it. Suppress the message when you cancelled it yourself.
    effect(() => {
      const t = this.state()?.trade ?? null;
      const hand = this.hand();
      const me = this.viewerId;
      const iAmIn = t && (t.requesterId === me || t.teammateId === me) ? t : null;
      const wasIn = this.prevMyTrade;
      const prevHand = this.prevHandForTrade;
      if (wasIn && !iAmIn) {
        const iRequested = wasIn.requesterId === me;
        const received = hand.find((c) => !prevHand.some((p) => p.uuid === c.uuid));
        const given = prevHand.find((c) => !hand.some((h) => h.uuid === c.uuid));
        const mate = this.state()?.players?.find((p) => p.id === wasIn.teammateId)?.name ?? '';
        if (this.suppressTradeOutcome) {
          this.suppressTradeOutcome = false; // you cancelled — no banner, no swap
        } else if (received && given) {
          // Accepted: animate the swap for both teammates; name the card only for the requester.
          this.animateTradeSwap(iRequested ? wasIn.teammateId : wasIn.requesterId, received, given);
          if (iRequested) {
            const key = received.value === 1 ? 'tradeGotAceMessage' : 'tradeGotKingMessage';
            this.teamHandoff.show(this.i18n.t('tradeGotTitle'), this.i18n.t(key, mate));
          }
        } else if (iRequested) {
          this.teamHandoff.show(
            this.i18n.t('tradeRejectedTitle'),
            this.i18n.t('tradeRejectedMessage', mate),
          );
        }
      }
      this.prevMyTrade = iAmIn;
      this.prevHandForTrade = hand;
    });

    // Sound effects (ported from the GWT AudioPlayer): a soft click when the turn passes to a
    // new player, and a fanfare when a player finishes (gains a place). Fires on the transition.
    effect(() => {
      const s = this.state();
      const cur = s?.currentPlayerId;
      if (cur && this.prevCurrentPlayerId !== undefined && cur !== this.prevCurrentPlayerId) {
        this.sound.play('turnChange');
      }
      if (cur) this.prevCurrentPlayerId = cur;

      const medals = (s?.players ?? []).filter((p) => (p.place ?? -1) > -1).length;
      if (this.prevMedalCount >= 0 && medals > this.prevMedalCount) {
        this.sound.play('medalAwarded');
      }
      this.prevMedalCount = medals;
    });
  }

  private prevCurrentPlayerId: string | undefined;
  private prevMedalCount = -1;

  private prevOwnPawnsHome = false;
  private prevMyTrade: Trade | null = null;
  private prevHandForTrade: CardModel[] = [];
  private suppressTradeOutcome = false;

  // ── Team card trade (step 5) ──────────────────────────────────────────────
  protected readonly offering = signal(false);
  protected readonly trade = computed<Trade | null>(() => this.state()?.trade ?? null);

  /** Show the "Ask for a King/Ace" button: team game, trade sub-option on, no trade pending yet,
   *  and you have cards to offer. */
  protected readonly canAskTrade = computed(() => {
    const s = this.state();
    if (!s?.teamCardTrade || s.trade || !this.viewerId) return false;
    const inTeam = s.players?.find((p) => p.id === this.viewerId)?.teamId != null;
    return inTeam && this.hand().length > 0;
  });

  /** The other party's name — the teammate (when you're the requester) or the requester. */
  protected readonly tradeOtherName = computed(() => {
    const t = this.trade();
    const s = this.state();
    if (!t || !s?.players) return '';
    const otherId = t.requesterId === this.viewerId ? t.teammateId : t.requesterId;
    return s.players.find((p) => p.id === otherId)?.name ?? '';
  });

  protected askForTrade(): void {
    this.offering.set(true);
  }
  protected onTradeCancelled(): void {
    this.suppressTradeOutcome = true;
  }

  // The players whose pawns the viewer may move: themselves, plus their teammate once all the
  // viewer's own pawns are home (team play phase-2). Fed to the selection so a teammate's pawns
  // become selectable — the backend enforces the same rule.
  private readonly controllablePlayerIds = computed(() => {
    const me = this.viewerId;
    if (!me) return [];
    const ids = [me];
    if (this.viewerOwnPawnsAllHome()) {
      const s = this.state();
      const myTeam = s?.players?.find((p) => p.id === me)?.teamId;
      const mate =
        myTeam != null ? s?.players?.find((p) => p.id !== me && p.teamId === myTeam) : undefined;
      if (mate) ids.push(mate.id);
    }
    return ids;
  });

  // Team play: are all of the viewer's own pawns home (finish tiles, tileNr ≥ 16)? Drives the
  // one-time hand-off announcement. False outside a team game.
  private readonly viewerOwnPawnsAllHome = computed(() => {
    const s = this.state();
    if (!s?.pawns || !s.players || !this.viewerId) return false;
    const inTeam = s.players.find((p) => p.id === this.viewerId)?.teamId != null;
    if (!inTeam) return false;
    const own = s.pawns.filter((p) => p.playerId === this.viewerId);
    return own.length > 0 && own.every((p) => (p.currentTileId?.tileNr ?? -1) >= 16);
  });

  // --- Pawn move animation (drives the reusable PawnAnimator engine) -------

  /** Walk each of a move's pawns along its path; killed pawns go home after the killer. */
  private animateMove(mr: MoveResponse): void {
    const g = this.geometry();
    if (!g) return;
    if (mr.moveType === 'onBoard') this.sound.play('pawnOnBoard');
    const d1 = this.walkPawn(g, mr.pawn1, mr.movePawn1, 0);
    const d2 = this.walkPawn(g, mr.pawn2, mr.movePawn2, 0);
    this.walkPawn(g, mr.pawnKilledByPawn1, mr.movePawnKilledByPawn1, d1);
    this.walkPawn(g, mr.pawnKilledByPawn2, mr.movePawnKilledByPawn2, d2);
    // A captured pawn "dies" as it's flung home — play the kill sound as that begins.
    if (mr.pawnKilledByPawn1) this.sound.play('pawnKilled', d1);
    if (mr.pawnKilledByPawn2) this.sound.play('pawnKilled', d2);
  }

  /** Convert a pawn's tile path to pixel waypoints (board geometry) and hand it to the animator. */
  private walkPawn(
    g: BoardGeometry,
    pawn: ApiPawn | undefined,
    move: PositionKey[] | undefined,
    delayMs: number,
  ): number {
    if (!pawn || !move) return 0;
    const points: Pt[] = [];
    for (const t of move) {
      const p = g.position(t.playerId, t.tileNr);
      if (p) points.push(p);
    }
    return this.pawnAnimator.walk(pawnKey(pawn.pawnId), points, delayMs);
  }

  /** Animate an opponent's just-played card from its fan slot to the pile. */
  private flyOpponentCard(playerId: string, fanCount: number, played: string[]): void {
    const segment = this.geometry()?.deckSegment(playerId);
    const last = played[played.length - 1];
    if (!segment || !last) return;
    const [suit, value] = last.split('_').map(Number);
    const slot = fanCardBacks(segment, fanCount).at(-1); // the outermost card leaves
    if (!slot) return;
    // Back-sized start; leaves the fan face-down and turns over mid-flight to reveal at the pile.
    this.cardTable.flyToPile(
      { suit, value, x: slot.x / 6, y: slot.y / 6, rot: slot.rotDeg },
      { startScale: 0.3, flip: 'in' },
    );
  }

  /**
   * Animate a completed card trade for a participant: the card you gave flies out to your
   * teammate's fan, and the King/Ace you received flies in from it to its slot in your hand.
   * Only the two teammates run this (their hands changed); opponents see nothing.
   */
  private animateTradeSwap(otherId: string, received: CardModel, given: CardModel): void {
    const g = this.geometry();
    if (!g) return;
    const seg = g.deckSegment(otherId);
    const partner = seg
      ? { x: (seg[0].x + seg[1].x) / 12, y: (seg[0].y + seg[1].y) / 12 } // fan midpoint in board-% (/6)
      : this.positioner.pileCenter();
    const hand = this.hand();
    const i = hand.findIndex((c) => c.uuid === received.uuid);
    const slot = this.positioner.handSlot(i, hand.length);
    const handAnchor = this.positioner.handSlot(0, 1); // fan centre — where the given card starts

    this.cardTable.tradeSwap(
      partner,
      { suit: given.suit, value: given.value, from: handAnchor },
      { uuid: received.uuid, suit: received.suit, value: received.value, to: slot },
    );
  }

  /** Forfeit: fly all of an opponent's cards from their fan to the pile, staggered. */
  private flyOpponentForfeit(
    playerId: string,
    fanCount: number,
    dropped: number,
    played: string[],
  ): void {
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
          this.cardTable.flyToPile({ suit, value, x: slot.x / 6, y: slot.y / 6, rot: slot.rotDeg }),
        i * 120, // small stagger between cards
      );
    });
  }

  private findPawn(id: string) {
    return this.state()?.pawns?.find((p) => pawnKey(p.pawnId) === id);
  }

  // Reactive selectors over the selection (reading rev() makes them recompute).
  protected readonly selectedCardUuid = computed(() => (this.rev(), this.selection.getCard()?.id));
  protected readonly pawn1Id = computed(() => (this.rev(), this.selection.getPawnId1()));
  protected readonly pawn2Id = computed(() => (this.rev(), this.selection.getPawnId2()));
  protected readonly splitVisible = computed(
    () => (this.rev(), this.selection.isSplitBoxesVisible()),
  );
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

  // Of those, the ones that would land on a teammate's pawn (team play only) — the board
  // warns on these in red instead of the usual gold. The mover is the viewer (you only ever
  // preview your own move); step 4 (playing a teammate's pawns) will generalise the mover.
  protected readonly teammateCaptureTiles = computed(() => {
    const s = this.state();
    if (!s?.pawns || !s.players || !this.viewerId) return new Set<string>();
    const teamOf = (playerId: string) => s.players!.find((p) => p.id === playerId)?.teamId ?? null;
    const occupants = s.pawns.map((p) => ({
      key: `${p.currentTileId.playerId}:${p.currentTileId.tileNr}`,
      ownerId: p.playerId,
    }));
    return teammateCaptureKeys(this.previewTiles(), occupants, teamOf, this.viewerId);
  });
  protected isTeammateCapture(playerId: string, tileNr: number): boolean {
    return this.teammateCaptureTiles().has(`${playerId}:${tileNr}`);
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
          this.previewTiles.set(new Set((res.tiles ?? []).map((t) => `${t.playerId}:${t.tileNr}`)));
        },
        error: () => {
          /* fire-and-forget: errors are non-critical here */
        },
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
    this.sound.play('buttonClick');

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
    const start = card ? this.cardTable.cards().find((c) => c.uuid === card.uuid) : undefined;
    this.touch();
    this.movesService.makeMove(this.sessionId, this.viewerId, move).subscribe({
      next: (response) => {
        if (response.result === 'CAN_MAKE_MOVE') {
          this.selection.reset(); // accepted → clear the selection
          // Fly the captured card (its id + face + hand slot) to the pile, popping as it goes.
          if (start) this.cardTable.flyToPile(start, { pop: true });
          else if (card) this.cardTable.pile.update((p) => [...p, card]);
          this.touch();
        } else {
          // Rejected by the rules (still a 200): explain why, and keep the
          // selection so the player can adjust it.
          this.rejection.show(
            this.i18n.t(
              rejectionMessageKey(response.rejectionReason),
              response.rejectionDetail ?? '',
            ),
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
    this.sound.play('buttonClick');
    // Snapshot my hand-card positions BEFORE the server push clears them, then fly
    // each to the pile, staggered (same as an opponent's forfeit).
    const myCards = this.cardTable.cards().filter((c) => !c.inPile);
    this.cardsService.playerForfeits(this.sessionId, this.viewerId).subscribe({
      error: () => {
        /* fire-and-forget: errors are non-critical here */
      },
    });
    this.selection.reset();
    this.previewTiles.set(new Set());
    this.touch();
    myCards.forEach((c, i) => setTimeout(() => this.cardTable.flyToPile(c), i * 120));
  }
}
