package gwtks;

public class GameBoardPresenter implements Presenter{
    private GameBoardView view;
    private GameStateServiceAsync gameStateService;

    public GameBoardPresenter(GameBoardView gameBoardView) {
        this.view = gameBoardView;
    }

    @Override
    public void start() {
        bind();
    }

    @Override
    public void stop() {

    }

    private void bind() {
        // todo: bind clickhandlers
    }
}
