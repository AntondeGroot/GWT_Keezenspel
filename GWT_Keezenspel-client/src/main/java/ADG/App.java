package ADG;

import ADG.Games.Keezen.*;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Label;

public class App implements EntryPoint {

  private static final String SERVER_ERROR = "An error occurred while "
      + "attempting to contact the server. Please check your network "
      + "connection and try again.";

  public void onModuleLoad() {

    // Create a Canvas for the Parcheesi board
    if (Canvas.createIfSupported() == null) {
      RootPanel.get().add(new Label("Canvas is not supported in your browser."));
      return;
    }
    GameModule gameModule = new GameModule();
    gameModule.onStart();
  }
}
