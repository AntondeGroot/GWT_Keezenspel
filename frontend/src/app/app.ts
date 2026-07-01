import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Board } from './features/board/board';
import { LanguageSelector } from './features/nav/language-selector/language-selector';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Board, LanguageSelector],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('frontend');
}
