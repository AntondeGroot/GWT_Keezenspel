import { Component, effect, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Board } from './features/board/board';
import { LanguageSelector } from './features/nav/language-selector/language-selector';
import { GameRules } from './features/nav/game-rules/game-rules';
import { LeaveGame } from './features/nav/leave-game/leave-game';
import { MoveRejected } from './features/board/move-rejected/move-rejected';
import { WinnerBanner } from './features/winner-banner/winner-banner';
import { ChatPanel } from './features/chat/chat';
import { Translations } from './i18n/translations.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Board, LanguageSelector, GameRules, LeaveGame, MoveRejected, WinnerBanner, ChatPanel],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly i18n = inject(Translations);

  constructor() {
    // Keep the browser tab title in sync with the localized game name; t() reads
    // the language signal, so this re-runs whenever the language changes.
    effect(() => (document.title = this.i18n.t('gameName')));
  }
}
