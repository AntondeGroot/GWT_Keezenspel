import { Component, inject } from '@angular/core';
import { Translations } from '../../../i18n/translations.service';
import { LANGUAGES, Locale } from '../../../i18n/translations';

/**
 * Language dropdown. Bound to the Translations service's `locale` signal, so the
 * whole app re-translates instantly on change (no page reload, unlike the GWT).
 */
@Component({
  selector: 'app-language-selector',
  templateUrl: './language-selector.html',
  styleUrl: './language-selector.scss',
})
export class LanguageSelector {
  protected readonly i18n = inject(Translations);
  protected readonly languages = LANGUAGES;

  protected onChange(value: string): void {
    this.i18n.setLanguage(value as Locale);
  }
}
