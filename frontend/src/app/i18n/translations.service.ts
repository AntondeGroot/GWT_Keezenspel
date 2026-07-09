import { Injectable, signal } from '@angular/core';
import { LANGUAGES, Locale, TranslationKey, translations } from './translations';

/**
 * Runtime i18n. Holds the current language as a signal, so any template binding
 * that calls `t(...)` re-renders instantly when the language changes — no reload
 * (unlike the GWT client, which had to reload the page). The choice is persisted
 * in the same `language` cookie the GWT used, and initialised from ?locale= then
 * that cookie, defaulting to English.
 */
@Injectable({ providedIn: 'root' })
export class Translations {
  private static readonly COOKIE = 'language';
  private static readonly CODES = LANGUAGES.map((l) => l.code);

  private readonly language = signal<Locale>(this.initialLocale());
  /** The current language, read-only, for the selector to reflect. */
  readonly locale = this.language.asReadonly();

  /** Translate a key in the current language; `%s` placeholders are filled in order. */
  t(key: TranslationKey, ...args: (string | number)[]): string {
    const dict = translations[this.language()];
    let value = dict[key] ?? translations.en[key] ?? key;
    for (const arg of args) {
      value = value.replace('%s', String(arg));
    }
    return value;
  }

  setLanguage(locale: Locale): void {
    this.language.set(locale);
    const oneYear = 60 * 60 * 24 * 365;
    document.cookie = `${Translations.COOKIE}=${locale};path=/;max-age=${oneYear}`;
  }

  private initialLocale(): Locale {
    const fromUrl = new URLSearchParams(window.location.search).get('locale');
    const fromCookie = document.cookie
      .split('; ')
      .find((c) => c.startsWith(`${Translations.COOKIE}=`))
      ?.split('=')[1];
    const candidate = fromUrl ?? fromCookie ?? 'en';
    return Translations.CODES.includes(candidate as Locale) ? (candidate as Locale) : 'en';
  }
}
