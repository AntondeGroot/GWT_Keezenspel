package ADG.Games.Keezen.util;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
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
    String fromUrl = Window.Location.getParameter(PLAYERID);
    if (fromUrl != null && !fromUrl.isEmpty()) {
      Cookies.setCookie(PLAYERID, fromUrl);
      return;
    }
    Collection<String> cookieNames = Cookies.getCookieNames();
    if (!cookieNames.contains(PLAYERID)) {
      Cookies.setCookie(PLAYERID, UUID.get());
    }
  }

  public static void createSessionIdCookie() {
    String fromUrl = Window.Location.getParameter(SESSIONID);
    if (fromUrl != null && !fromUrl.isEmpty()) {
      Cookies.setCookie(SESSIONID, fromUrl);
      return;
    }
    Collection<String> cookieNames = Cookies.getCookieNames();
    if (!cookieNames.contains(SESSIONID)) {
      Cookies.setCookie(SESSIONID, "123");
    }
  }
}
