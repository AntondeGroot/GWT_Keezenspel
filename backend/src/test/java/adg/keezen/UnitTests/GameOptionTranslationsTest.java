package adg.keezen.UnitTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import adg.keezen.GameOptionTranslations;
import org.junit.jupiter.api.Test;

/** The localized-string lookup: every fallback path plus a real hit. */
class GameOptionTranslationsTest {

  private static final String KEY = "gameOption.exactMoveRequired.label";
  private static final String FALLBACK = "English fallback";

  @Test
  void nullKeyReturnsTheFallback() {
    assertEquals(FALLBACK, GameOptionTranslations.resolve(null, FALLBACK, "de"));
  }

  @Test
  void nullLocaleReturnsTheFallback() {
    assertEquals(FALLBACK, GameOptionTranslations.resolve(KEY, FALLBACK, null));
  }

  @Test
  void unknownKeyReturnsTheFallback() {
    assertEquals(FALLBACK, GameOptionTranslations.resolve("no.such.key", FALLBACK, "de"));
  }

  @Test
  void knownKeyButUnknownLocaleReturnsTheFallback() {
    assertEquals(FALLBACK, GameOptionTranslations.resolve(KEY, FALLBACK, "xx"));
  }

  @Test
  void returnsTheLocalizedStringForAKnownKeyAndLocale() {
    assertEquals("Exakter Zug erforderlich", GameOptionTranslations.resolve(KEY, FALLBACK, "de"));
  }

  @Test
  void localeLookupIsCaseInsensitive() {
    assertEquals("Exakter Zug erforderlich", GameOptionTranslations.resolve(KEY, FALLBACK, "DE"));
  }
}
