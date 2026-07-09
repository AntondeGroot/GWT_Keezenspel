import { Component, computed, inject, input, output, signal } from '@angular/core';
import { Card } from '../card/card';
import { Card as CardModel, Trade, TradeAction, TradeService } from '../../../api';
import { Translations } from '../../../i18n/translations.service';

type Mode = 'respond' | 'waiting' | 'offer' | 'closed';

/**
 * The team card-trade "parley" (step 5, part 3). One dialog with three faces driven by the
 * shared trade state:
 *  - offer   — you opened it: pick a card to give and ask your teammate for a King/Ace;
 *  - waiting — you've asked, now wait (or cancel);
 *  - respond — your teammate asked you: pick a King/Ace to give, or decline.
 * All actions go straight to the trade endpoint; the swap and the closing come back over SSE.
 */
@Component({
  selector: 'app-trade-panel',
  imports: [Card],
  templateUrl: './trade-panel.html',
  styleUrl: './trade-panel.scss',
})
export class TradePanel {
  private readonly tradeService = inject(TradeService);
  protected readonly i18n = inject(Translations);

  readonly hand = input<CardModel[]>([]);
  readonly trade = input<Trade | null>(null);
  readonly viewerId = input<string>('');
  readonly sessionId = input<string>('');
  readonly offering = input<boolean>(false);
  /** The other party's display name — the requester (respond) or the teammate (waiting). */
  readonly otherName = input<string>('');
  readonly closed = output<void>();
  readonly cancelledByMe = output<void>();

  private readonly selectedUuid = signal<number | null>(null);

  protected readonly mode = computed<Mode>(() => {
    const t = this.trade();
    const me = this.viewerId();
    if (t && t.teammateId === me) return 'respond';
    if (t && t.requesterId === me) return 'waiting';
    return this.offering() ? 'offer' : 'closed';
  });

  protected isKingOrAce(c: CardModel): boolean {
    return c.value === 1 || c.value === 13;
  }
  /** In respond mode only a King/Ace may be given; in offer mode any card may be offered. */
  protected selectable(c: CardModel): boolean {
    return this.mode() !== 'respond' || this.isKingOrAce(c);
  }
  protected isSelected(c: CardModel): boolean {
    return this.selectedUuid() === c.uuid;
  }
  protected pick(c: CardModel): void {
    if (this.selectable(c)) this.selectedUuid.set(c.uuid ?? null);
  }
  protected readonly canConfirm = computed(() => {
    const c = this.hand().find((x) => x.uuid === this.selectedUuid());
    return !!c && this.selectable(c);
  });

  protected confirm(): void {
    const c = this.hand().find((x) => x.uuid === this.selectedUuid());
    if (!c) return;
    this.send(
      this.mode() === 'respond' ? TradeAction.ActionEnum.Accept : TradeAction.ActionEnum.Request,
      c,
    );
    this.close();
  }
  protected decline(): void {
    this.send(TradeAction.ActionEnum.Reject);
    this.close();
  }
  protected cancel(): void {
    this.send(TradeAction.ActionEnum.Cancel);
    this.cancelledByMe.emit();
    this.close();
  }
  /** Leave offer mode without asking. */
  protected dismiss(): void {
    this.close();
  }

  private send(action: TradeAction.ActionEnum, card?: CardModel): void {
    const sessionId = this.sessionId();
    const playerId = this.viewerId();
    if (!sessionId || !playerId) return;
    this.tradeService
      .teamTrade(sessionId, { action, playerId, card })
      .subscribe({ error: () => {} });
  }
  private close(): void {
    this.selectedUuid.set(null);
    this.closed.emit();
  }
}
