package ADG.Games.Keezen.util;

import com.google.gwt.user.client.Cookies;

import java.util.Collection;

public class Cookie {

  private static final String PLAYERID = "playerid";
  private static final String SESSIONID = "sessionid";

  public static String getPlayerId() {
    createPlayerIdCookie();
    return Cookies.getCookie(PLAYERID);
  }

  public static String getSessionID() {
    createSessionIdCookie();
    return Cookies.getCookie(SESSIONID);
  }

  public static void createPlayerIdCookie() {
    Collection<String> cookieNames = Cookies.getCookieNames();
    if (!cookieNames.contains(PLAYERID)) {
      Cookies.setCookie(PLAYERID, UUID.get());
    }
  }

  public static void createSessionIdCookie() {
    Collection<String> cookieNames = Cookies.getCookieNames();
    if (!cookieNames.contains(SESSIONID)) {
      Cookies.setCookie(SESSIONID,
          "123"); // todo: use a way to get the sessionID from the server (for example after joining a room_
    }
  }
}
