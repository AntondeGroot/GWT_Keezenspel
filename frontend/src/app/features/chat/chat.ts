import { Component, OnDestroy, OnInit, computed, effect, inject, signal } from '@angular/core';
import { ChatService } from './chat.service';
import { GameStore } from '../../game-store';
import { resolveGameSession } from '../../session';

/**
 * In-game chat, ported from the GWT board's chat widget. It shows only while the chat
 * server is reachable (`ChatService.available`): no server, no chat UI.
 *
 * Desktop: an always-open panel just above the footer. Mobile: it collapses (like the GWT
 * mobile layout) into a floating chat button with an unread-count badge; tapping it opens the
 * panel as a popup over the board and clears the badge.
 */
@Component({
  selector: 'app-chat',
  templateUrl: './chat.html',
  styleUrl: './chat.scss',
})
export class ChatPanel implements OnInit, OnDestroy {
  protected readonly chat = inject(ChatService);
  private readonly store = inject(GameStore);
  private readonly session = resolveGameSession();

  /** Mobile popup open/closed (ignored on desktop, where the panel is always shown). */
  protected readonly open = signal(false);
  /** Messages the viewer has already seen — everything present when chat first loads counts as
   *  seen, so history never shows as unread (matches the GWT badge). */
  private readonly seenCount = signal(0);
  private seenInitialized = false;

  /** Unread badge count for the mobile chat button. */
  protected readonly unread = computed(() =>
    Math.max(0, this.chat.messages().length - this.seenCount()),
  );

  constructor() {
    effect(() => {
      const total = this.chat.messages().length;
      // Baseline existing history the first time it loads.
      if (!this.seenInitialized && total > 0) {
        this.seenCount.set(total);
        this.seenInitialized = true;
      }
      // While the popup is open, keep everything marked seen so the badge stays cleared.
      if (this.open()) this.seenCount.set(total);
    });
  }

  /** The viewer's display name — falls back to their id, then a generic label. */
  private readonly sender = computed(() => {
    const me = this.store.players().find((p) => p.id === this.session.playerId);
    return me?.name ?? this.session.playerId ?? 'anon';
  });

  ngOnInit(): void {
    if (this.session.sessionId) this.chat.connect(this.session.sessionId);
  }

  ngOnDestroy(): void {
    this.chat.disconnect();
  }

  protected toggle(): void {
    this.open.update((o) => !o);
  }

  protected close(): void {
    this.open.set(false);
  }

  protected send(input: HTMLInputElement): void {
    this.chat.send(input.value, this.sender());
    input.value = '';
  }
}
