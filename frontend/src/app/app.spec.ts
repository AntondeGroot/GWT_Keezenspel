import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { Translations } from './i18n/translations.service';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('renders the localized game name in the title bar', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    // The title bar shows the localized game name (e.g. Tock/Keezen/Dog), not a
    // hardcoded string — so it tracks whatever the current locale resolves to.
    const expected = TestBed.inject(Translations).t('gameName');
    expect(compiled.querySelector('h1')?.textContent?.trim()).toBe(expected);
  });
});
