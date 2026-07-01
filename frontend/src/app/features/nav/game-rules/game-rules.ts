import { Component, inject, signal } from '@angular/core';
import { Translations } from '../../../i18n/translations.service';
import { TranslationKey } from '../../../i18n/translations';

interface RulesRow {
  card: string; // the chip label (A, K, 4, 7, J, Q)
  red: boolean; // red chip vs black
  hint: TranslationKey;
}
interface RulesSection {
  titleKey: TranslationKey;
  rows: RulesRow[];
}

/**
 * Game Rules button + modal, ported from the GWT GameRulesWidget. Clicking the
 * button opens a modal; clicking the dark backdrop closes it (clicks inside the
 * content don't). All text comes from the i18n service, so it follows the
 * current language.
 */
@Component({
  selector: 'app-game-rules',
  templateUrl: './game-rules.html',
  styleUrl: './game-rules.scss',
})
export class GameRules {
  protected readonly i18n = inject(Translations);
  protected readonly open = signal(false);

  protected readonly sections: RulesSection[] = [
    {
      titleKey: 'rulesGettingOnBoard',
      rows: [
        { card: 'A', red: true, hint: 'hintAce' },
        { card: 'K', red: true, hint: 'hintKing' },
      ],
    },
    {
      titleKey: 'rulesSpecialCards',
      rows: [
        { card: '4', red: false, hint: 'hintFour' },
        { card: '7', red: false, hint: 'hintSeven' },
        { card: 'J', red: true, hint: 'hintJack' },
        { card: 'Q', red: true, hint: 'hintQueen' },
      ],
    },
  ];
}