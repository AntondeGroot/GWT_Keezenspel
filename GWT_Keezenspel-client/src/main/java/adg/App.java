package adg;

import adg.keezen.GameModule;
import adg.keezen.i18n.AppConstants;
import adg.keezen.util.Cookie;
import adg.keezen.util.LanguageSelectorWidget;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class App implements EntryPoint {

  private static final AppConstants CONSTANTS = GWT.create(AppConstants.class);

  public void onModuleLoad() {
    // Persist sessionid/playerid from URL into cookies before any possible reload.
    Cookie.createPlayerIdCookie();
    Cookie.createSessionIdCookie();

    // Ensure the language cookie exists, then sync the GWT locale permutation.
    // If the cookie locale doesn't match ?locale= in the URL, the page reloads.
    Cookie.createLanguageCookie();
    if (Cookie.syncGwtLocale()) return;

    RootPanel.get("lang-selector-root").add(new LanguageSelectorWidget(true));

    if (Canvas.createIfSupported() == null) {
      RootPanel.get().add(new Label(CONSTANTS.canvasNotSupported()));
      return;
    }
    GameModule gameModule = new GameModule();
    gameModule.onStart();
  }
}