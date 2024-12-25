package gwtks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class PresenterManager {
    // Models
    private final GameBoardModel gameBoardModel = new GameBoardModel();
    // Views
    private final GameBoardView gameBoardView = new GameBoardView();
    // Presenters
    private GameBoardPresenter gameBoardPresenter;
    private Presenter currentPresenter;
    // Services
    private final GameStateServiceAsync gameStateServiceAsync = GWT.create(GameStateService.class);

    public void switchToGameRoom() {
        if (gameBoardPresenter == null) {
            gameBoardPresenter = new GameBoardPresenter(gameBoardModel, gameBoardView, this, gameStateServiceAsync);
        }
        switchPresenter(gameBoardPresenter, gameBoardView);
    }

    private void switchPresenter(Presenter newPresenter, Widget newView) {
        if (currentPresenter != null) {
            currentPresenter.stop();
        }
        RootPanel.get().clear();
        RootPanel.get().add(newView);
        currentPresenter = newPresenter;
        currentPresenter.start();
    }
}
