package adg.keezen.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CookieTest {

    // -------------------------------------------------------------------------
    // pickValue — URL preferred over cookie
    // -------------------------------------------------------------------------

    @Test
    void pickValue_urlPresent_returnsUrl() {
        assertEquals("url-session", Cookie.pickValue("url-session", "cookie-session"));
    }

    @Test
    void pickValue_urlNull_returnsCookie() {
        assertEquals("cookie-session", Cookie.pickValue(null, "cookie-session"));
    }

    @Test
    void pickValue_urlEmpty_returnsCookie() {
        assertEquals("cookie-session", Cookie.pickValue("", "cookie-session"));
    }

    @Test
    void pickValue_bothNull_returnsNull() {
        assertNull(Cookie.pickValue(null, null));
    }

    @Test
    void pickValue_bothEmpty_returnsEmpty() {
        assertEquals("", Cookie.pickValue("", ""));
    }

    @Test
    void pickValue_urlPresent_cookieNull_returnsUrl() {
        assertEquals("url-player", Cookie.pickValue("url-player", null));
    }

    // -------------------------------------------------------------------------
    // needsLocaleRedirect
    // -------------------------------------------------------------------------

    @Test
    void noRedirect_whenLocaleMatches() {
        assertFalse(Cookie.needsLocaleRedirect(Language.nl, "nl"));
        assertFalse(Cookie.needsLocaleRedirect(Language.en, "en"));
        assertFalse(Cookie.needsLocaleRedirect(Language.de, "de"));
    }

    @Test
    void redirect_whenLocaleMismatch() {
        assertTrue(Cookie.needsLocaleRedirect(Language.nl, "en"));
        assertTrue(Cookie.needsLocaleRedirect(Language.en, "nl"));
        assertTrue(Cookie.needsLocaleRedirect(Language.de, "fr"));
    }

    @Test
    void noRedirect_whenNoLocaleInUrl_andCookieIsEnglish() {
        assertFalse(Cookie.needsLocaleRedirect(Language.en, null));
        assertFalse(Cookie.needsLocaleRedirect(Language.en, ""));
    }

    @Test
    void redirect_whenNoLocaleInUrl_andCookieIsNonEnglish() {
        assertTrue(Cookie.needsLocaleRedirect(Language.nl, null));
        assertTrue(Cookie.needsLocaleRedirect(Language.nl, ""));
        assertTrue(Cookie.needsLocaleRedirect(Language.de, null));
        assertTrue(Cookie.needsLocaleRedirect(Language.fr, ""));
        assertTrue(Cookie.needsLocaleRedirect(Language.nb, null));
    }

    // -------------------------------------------------------------------------
    // buildRedirectUrl
    // -------------------------------------------------------------------------

    @Test
    void buildRedirectUrl_usesUrlSessionAndPlayer_whenBothPresentInUrl() {
        String url = Cookie.buildRedirectUrl(
                "/mobile.html", "nl",
                "new-session", "old-session",
                "new-player",  "old-player",
                "");
        assertEquals("/mobile.html?locale=nl&sessionid=new-session&playerid=new-player", url);
    }

    @Test
    void buildRedirectUrl_fallsBackToCookie_whenUrlParamsMissing() {
        String url = Cookie.buildRedirectUrl(
                "/mobile.html", "nl",
                null, "cookie-session",
                null, "cookie-player",
                "");
        assertEquals("/mobile.html?locale=nl&sessionid=cookie-session&playerid=cookie-player", url);
    }

    @Test
    void buildRedirectUrl_omitsSessionAndPlayer_whenAllEmpty() {
        String url = Cookie.buildRedirectUrl(
                "/mobile.html", "en",
                null, null,
                null, null,
                "");
        assertEquals("/mobile.html?locale=en", url);
    }

    @Test
    void buildRedirectUrl_preservesHash() {
        String url = Cookie.buildRedirectUrl(
                "/keezen/mobile.html", "nl",
                "sess", null,
                "player", null,
                "#room=123");
        assertEquals("/keezen/mobile.html?locale=nl&sessionid=sess&playerid=player#room=123", url);
    }

    @Test
    void buildRedirectUrl_mixedSources_urlSessionCookiePlayer() {
        String url = Cookie.buildRedirectUrl(
                "/mobile.html", "nl",
                "url-session", "old-cookie-session",
                "",            "cookie-player",
                "");
        assertEquals("/mobile.html?locale=nl&sessionid=url-session&playerid=cookie-player", url);
    }

    @Test
    void buildRedirectUrl_newGameAfterLeaving_usesNewSessionFromUrl() {
        // Simulates: player left game with session A, now GameRoom sends them
        // to game B. The URL has session B; cookie still has old session A.
        String url = Cookie.buildRedirectUrl(
                "/mobile.html", "nl",
                "game-B-session", "game-A-session",
                "player-uuid",   "player-uuid",
                "");
        assertEquals("/mobile.html?locale=nl&sessionid=game-B-session&playerid=player-uuid", url);
    }
}