import { Component, inject } from '@angular/core';
import { PlayersService } from '../../../api';
import { Translations } from '../../../i18n/translations.service';
import { resolveGameSession } from '../../../session';

/**
 * Leave game button (fixed top-left), ported from the GWT leaveGameButton. Asks
 * for confirmation, then DELETEs the player from the game (leaveGame) and
 * navigates away — matching the GWT, which redirects on success or failure alike.
 */
@Component({
  selector: 'app-leave-game',
  templateUrl: './leave-game.html',
  styleUrl: './leave-game.scss',
})
export class LeaveGame {
  protected readonly i18n = inject(Translations);
  private readonly players = inject(PlayersService);
  private readonly session = resolveGameSession();

  /** Only shown while actually in a game. */
  protected readonly inGame = !!(this.session.sessionId && this.session.playerId);

  protected leave(): void {
    const { sessionId, playerId } = this.session;
    if (!sessionId || !playerId) return;
    if (!confirm(this.i18n.t('confirmLeaveGame'))) return;

    // Leave, then return to the game room whether or not the call succeeds (GWT parity).
    const done = () => (window.location.href = this.gameRoomUrl());
    this.players.leaveGame(sessionId, playerId).subscribe({ next: done, error: done });
  }

  /**
   * Ported from the GWT buildGameRoomUrl(): the GameRoom lives at the host root
   * (no port), and a `roomid` URL param carries the room to return to.
   */
  private gameRoomUrl(): string {
    const base = `${window.location.protocol}//${window.location.hostname}`;
    const roomId = new URLSearchParams(window.location.search).get('roomid');
    return roomId ? `${base}/?locale=${this.i18n.locale()}#room=${roomId}` : base;
  }
}