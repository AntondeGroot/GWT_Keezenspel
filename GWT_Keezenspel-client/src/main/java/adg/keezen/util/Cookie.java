package adg.keezen.util;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import java.util.Collection;
import java.util.Date;

public class Cookie {

  private static final String PLAYERID  = "playerid";
  private static final String SESSIONID = "sessionid";
  private static final String LANGUAGE  = "language";

  private static final long MILLIS_400_DAYS = 400L * 24L * 60L * 60L * 1000L;

  public static String getPlayerId() {
    createPlayerIdCookie();
    return Cookies.getCookie(PLAYERID);
  }

  public static String getSessionID() {
    createSessionIdCookie();
    return Cookies.getCookie(SESSIONID);
  }

  public static void createPlayerIdCookie() {
    String fromUrl = Window.Location.getParameter(PLAYERID);
    if (fromUrl != null && !fromUrl.isEmpty()) {
      Cookies.setCookie(PLAYERID, fromUrl, oneDayFromNow(), null, "/", isSecure());
      return;
    }
    Collection<String> cookieNames = Cookies.getCookieNames();
    if (!cookieNames.contains(PLAYERID)) {
      Cookies.setCookie(PLAYERID, UUID.get(), oneDayFromNow(), null, "/", isSecure());
    }
  }

  public static void createSessionIdCookie() {
    String fromUrl = Window.Location.getParameter(SESSIONID);
    if (fromUrl != null && !fromUrl.isEmpty()) {
      Cookies.setCookie(SESSIONID, fromUrl, oneDayFromNow(), null, "/", isSecure());
      return;
    }
    Collection<String> cookieNames = Cookies.getCookieNames();
    if (!cookieNames.contains(SESSIONID)) {
      Cookies.setCookie(SESSIONID, "123", oneDayFromNow(), null, "/", isSecure());
    }
  }

  public static Language getLanguage() {
    String value = Cookies.getCookie(LANGUAGE);
    if (value != null) {
      try { return Language.valueOf(value); } catch (IllegalArgumentException ignored) {}
    }
    return Language.en;
  }

  public static void createLanguageCookie() {
    Collection<String> cookieNames = Cookies.getCookieNames();
    if (!cookieNames.contains(LANGUAGE)) {
      Cookies.setCookie(LANGUAGE, Language.en.name(), longTermExpiry(), null, "/", isSecure());
    }
  }

  public static void setLanguage(Language language) {
    Cookies.setCookie(LANGUAGE, language.name(), longTermExpiry(), null, "/", isSecure());
  }

  public static void changeLanguage(Language language) {
    Cookies.setCookie(LANGUAGE, language.name(), longTermExpiry(), null, "/", isSecure());
    reloadWithLocale(language.name());
  }

  // Returns true if a navigation was triggered (caller should return immediately).
  public static boolean syncGwtLocale() {
    Language lang = getLanguage();
    String urlLocale = Window.Location.getParameter("locale");
    if (needsLocaleRedirect(lang, urlLocale)) {
      reloadWithLocale(lang.name());
      return true;
    }
    return false;
  }

  private static void reloadWithLocale(String locale) {
    String url = buildRedirectUrl(
        Window.Location.getPath(), locale,
        Window.Location.getParameter(SESSIONID), Cookies.getCookie(SESSIONID),
        Window.Location.getParameter(PLAYERID),  Cookies.getCookie(PLAYERID),
        Window.Location.getHash());
    Window.Location.replace(url);
  }

  // --- package-private pure logic, no GWT dependencies — tested in CookieTest ---

  static boolean needsLocaleRedirect(Language cookieLang, String urlLocale) {
    if (urlLocale == null || urlLocale.isEmpty()) {
      // No ?locale= in URL: only redirect when the cookie requests non-English.
      return cookieLang != Language.en;
    }
    return !cookieLang.name().equals(urlLocale);
  }

  static String pickValue(String urlVal, String cookieVal) {
    // URL parameter is authoritative (placed there by GameRoom for this session).
    // Cookie is only a fallback for page refreshes where the URL has no value.
    return (urlVal != null && !urlVal.isEmpty()) ? urlVal : cookieVal;
  }

  static String buildRedirectUrl(String path, String locale,
                                  String urlSession, String cookieSession,
                                  String urlPlayer,  String cookiePlayer,
                                  String hash) {
    StringBuilder query = new StringBuilder("?locale=").append(locale);
    String session = pickValue(urlSession, cookieSession);
    if (session != null && !session.isEmpty()) query.append("&sessionid=").append(session);
    String player = pickValue(urlPlayer, cookiePlayer);
    if (player != null && !player.isEmpty()) query.append("&playerid=").append(player);
    return path + query + hash;
  }

  private static Date oneDayFromNow() {
    return new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000);
  }

  private static Date longTermExpiry() {
    return new Date(new Date().getTime() + MILLIS_400_DAYS);
  }

  private static boolean isSecure() {
    return Window.Location.getProtocol().startsWith("https");
  }
}
