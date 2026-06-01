package adg.keezen;

import adg.keezen.board.GameBoardPresenter;
import adg.keezen.board.GameBoardView;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class GameModule {

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
    GameBoardPresenter gameBoardPresenter = new GameBoardPresenter(gameBoardView);
    gameBoardPresenter.start();
  }
}