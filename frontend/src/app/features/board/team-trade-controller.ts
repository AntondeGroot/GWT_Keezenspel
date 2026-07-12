import { computed, signal } from '@angular/core';
import { Card as CardModel, GameStatePush, Trade } from '../../api';
import { TranslationKey } from '../../i18n/keys';

type Translate = (key: TranslationKey, ...args: (string | number)[]) => string;

/**
 * The team card-trade feature (step 5): a player may ask their teammate for a King or Ace. This
 * owns the "offering" UI state, the derived view state (whether the ask button shows, the other
 * party's name), and — driven from the board's push effect — reacts to the outcome of the viewer's
 * own offer with the swap animation and an accepted/rejected banner.
 *
 * Framework-light (signals + computeds, no DI): the board wires in the state/hand getters and the
 * three side effects (animate the swap, show a banner, translate), so this stays unit-testable.
 */
export class TeamTradeController {
  /** True while the viewer's outgoing offer dialog is open. */
  readonly offering = signal(false);

  private prevMyTrade: Trade | null = null;
  private prevHand: CardModel[] = [];
  private suppressOutcome = false;

  constructor(
    private readonly state: () => GameStatePush | undefined,
    private readonly hand: () => CardModel[],
    private readonly viewerId: string | null,
    private readonly onSwap: (otherId: string, received: CardModel, given: CardModel) => void,
    private readonly notify: (title: string, message: string) => void,
    private readonly t: Translate,
  ) {}

  /** The pending trade on the table, if any. */
  readonly pending = computed<Trade | null>(() => this.state()?.trade ?? null);

  /** Show the "Ask for a King/Ace" button: team game, trade sub-option on, no trade pending yet,
   *  and you have cards to offer. */
  readonly canAsk = computed(() => {
    const s = this.state();
    if (!s?.teamCardTrade || s.trade || !this.viewerId) return false;
    const inTeam = s.players?.find((p) => p.id === this.viewerId)?.teamId != null;
    return inTeam && this.hand().length > 0;
  });

  /** The other party's name — the teammate (when you're the requester) or the requester. */
  readonly otherName = computed(() => {
    const t = this.pending();
    const s = this.state();
    if (!t || !s?.players) return '';
    const otherId = t.requesterId === this.viewerId ? t.teammateId : t.requesterId;
    return s.players.find((p) => p.id === otherId)?.name ?? '';
  });

  /** Open the offer dialog (the "Ask for a King/Ace" button). */
  ask(): void {
    this.offering.set(true);
  }

  /** Close the offer dialog. */
  stopOffering(): void {
    this.offering.set(false);
  }

  /** You cancelled your own offer — suppress the next outcome banner/swap. */
  cancelledByMe(): void {
    this.suppressOutcome = true;
  }

  /**
   * Called from the board's push effect. When your outgoing offer resolves, tell you whether your
   * teammate gave you a card: an accept removes your offered card and adds the received one (so we
   * animate the swap and, for the requester, name the card), a reject leaves your hand unchanged.
   * A trade you cancelled yourself is silent.
   */
  reactToOutcome(): void {
    const t = this.pending();
    const me = this.viewerId;
    const iAmIn = t && (t.requesterId === me || t.teammateId === me) ? t : null;
    const wasIn = this.prevMyTrade;
    const hand = this.hand();
    // Act only on the transition OUT of a trade the viewer was part of.
    if (wasIn && !iAmIn) this.announceOutcome(wasIn, this.prevHand, hand);
    this.prevMyTrade = iAmIn;
    this.prevHand = hand;
  }

  /** Banner + swap for a just-resolved trade the viewer was in (accepted / rejected / cancelled). */
  private announceOutcome(wasIn: Trade, prevHand: CardModel[], hand: CardModel[]): void {
    if (this.suppressOutcome) {
      this.suppressOutcome = false; // you cancelled — no banner, no swap
      return;
    }
    const iRequested = wasIn.requesterId === this.viewerId;
    const received = hand.find((c) => !prevHand.some((p) => p.uuid === c.uuid));
    const given = prevHand.find((c) => !hand.some((h) => h.uuid === c.uuid));
    const mate = this.state()?.players?.find((p) => p.id === wasIn.teammateId)?.name ?? '';
    if (received && given) {
      // Accepted: animate the swap for both teammates; name the card only for the requester.
      this.onSwap(iRequested ? wasIn.teammateId : wasIn.requesterId, received, given);
      if (iRequested) {
        const key = received.value === 1 ? 'tradeGotAceMessage' : 'tradeGotKingMessage';
        this.notify(this.t('tradeGotTitle'), this.t(key, mate));
      }
    } else if (iRequested) {
      this.notify(this.t('tradeRejectedTitle'), this.t('tradeRejectedMessage', mate));
    }
  }
}
