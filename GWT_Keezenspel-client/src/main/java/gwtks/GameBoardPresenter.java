package gwtks;

public class GameBoardPresenter implements Presenter{
    private GameBoardView view;
    private PresenterManager presenterManager;
    private GameStateServiceAsync gameStateService;

    public GameBoardPresenter(GameBoardModel gameBoardModel, GameBoardView gameBoardView, PresenterManager presenterManager, GameStateServiceAsync gameStateServiceAsync) {
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
