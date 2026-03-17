package ADG.Games.Keezen;

import ADG.Games.Keezen.board.GameBoardPresenter;
import ADG.Games.Keezen.board.GameBoardView;
import ADG.Games.Keezen.services.PollingService;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class GameModule {

  private final PollingService pollingService = GWT.create(PollingService.class);
  private GameBoardView gameBoardView;

  public void onStart() {

    // Create a Canvas for the Parcheesi board
    if (Canvas.createIfSupported() == null) {
      RootPanel.get().add(new Label("Canvas is not supported in your browser."));
      return;
    }

    gameBoardView = new GameBoardView();
    RootPanel.get().clear();
    RootPanel.get().add(gameBoardView);
    GameBoardPresenter gameBoardPresenter =
        new GameBoardPresenter(gameBoardView, pollingService);
    gameBoardPresenter.start();
  }
}