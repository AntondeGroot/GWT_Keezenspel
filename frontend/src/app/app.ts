import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Board } from './features/board/board';
import { LanguageSelector } from './features/nav/language-selector/language-selector';
import { GameRules } from './features/nav/game-rules/game-rules';
import { LeaveGame } from './features/nav/leave-game/leave-game';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Board, LanguageSelector, GameRules, LeaveGame],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('frontend');
}
