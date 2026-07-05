import { Component, effect, inject, signal } from '@angular/core';
import { GameStore } from '../../game-store';
import { Translations } from '../../i18n/translations.service';

const MEDALS: Record<number, string> = { 1: '🥇', 2: '🥈', 3: '🥉' };

interface WinnerView {
  name: string;
  medal: string;
  color: string;
}

/**
 * A celebratory banner that pops when a player finishes (all pawns home). It
 * watches the store's `winners` list and announces each new finisher with their
 * medal, then auto-dismisses. Their standing also shows as a medal in the roster.
 */
@Component({
  selector: 'app-winner-banner',
  templateUrl: './winner-banner.html',
  styleUrl: './winner-banner.scss',
})
export class WinnerBanner {
  private readonly store = inject(GameStore);
  protected readonly i18n = inject(Translations);
  protected readonly current = signal<WinnerView | null>(null);

  // -1 until the first push; then the number of finishers we've already seen, so
  // joining a game that already has winners doesn't replay a stale banner.
  private seen = -1;
  private timer: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    effect(() => {
      const players = this.store.players();
      const winners = this.store.winners();

      // Arm only once the first real push has arrived (players populated) and adopt
      // whatever winners already exist silently — joining a game mid-way must not
      // replay a banner for someone who finished before you connected.
      if (this.seen < 0) {
        if (players.length > 0) this.seen = winners.length;
        return;
      }

      if (winners.length > this.seen) {
        const place = winners.length;
        const player = players.find((p) => p.id === winners[place - 1]);
        this.announce({
          name: player?.name ?? '',
          medal: MEDALS[place] ?? '🏅',
          color: player?.color ?? '#f4d03f',
        });
      }
      this.seen = winners.length;
    });
  }

  private announce(w: WinnerView): void {
    this.current.set(w);
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.current.set(null), 4500);
  }

  protected dismiss(): void {
    clearTimeout(this.timer);
    this.current.set(null);
  }
}
