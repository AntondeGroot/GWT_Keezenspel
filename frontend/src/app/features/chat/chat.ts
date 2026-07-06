import { Component, OnDestroy, OnInit, computed, inject } from '@angular/core';
import { ChatService } from './chat.service';
import { GameStore } from '../../game-store';
import { resolveGameSession } from '../../session';

/**
 * In-game chat, ported from the GWT board's chat widget. It shows only while the chat
 * server is reachable (`ChatService.available`), matching the GWT behaviour: no server,
 * no chat UI. Sits just above the footer. Messages are sent under the viewer's name and
 * obfuscated by the service (wire-compatible with GWT clients in the same room).
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

  protected send(input: HTMLInputElement): void {
    this.chat.send(input.value, this.sender());
    input.value = '';
  }
}