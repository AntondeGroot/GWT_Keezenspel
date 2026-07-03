import { describe, expect, it } from 'vitest';
import { pickValue } from './session';

// Partial port of the GWT client's CookieTest (JUnit -> Vitest).
//
// Only the `pickValue` group is migrated. GWT's `needsLocaleRedirect` and
// `buildRedirectUrl` model reloading the page with a `?locale=` param, which
// GWT needed because it compiles a separate permutation per locale. Angular
// uses runtime i18n (Translations service) and never reloads to switch locale,
// so those two functions have no Angular counterpart to test. Their 12 GWT
// tests stay GWT-only and retire when GWT is removed (see TEST_MIGRATION_CHECKLIST.md).

// ── pickValue — URL preferred over cookie ─────────────────────────────────────
describe('Cookie - pickValue', () => {
  it('pickValue_urlPresent_returnsUrl', () => {
    expect(pickValue('url-session', 'cookie-session')).toBe('url-session');
  });

  it('pickValue_urlNull_returnsCookie', () => {
    expect(pickValue(null, 'cookie-session')).toBe('cookie-session');
  });

  it('pickValue_urlEmpty_returnsCookie', () => {
    expect(pickValue('', 'cookie-session')).toBe('cookie-session');
  });

  it('pickValue_bothNull_returnsNull', () => {
    expect(pickValue(null, null)).toBeNull();
  });

  it('pickValue_bothEmpty_returnsEmpty', () => {
    expect(pickValue('', '')).toBe('');
  });

  it('pickValue_urlPresent_cookieNull_returnsUrl', () => {
    expect(pickValue('url-player', null)).toBe('url-player');
  });
});