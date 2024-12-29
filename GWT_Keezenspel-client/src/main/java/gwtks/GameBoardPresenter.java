package gwtks;

import java.util.ArrayList;

public class GameBoardPresenter implements Presenter{
    private GameBoardView view;
    private GameStateServiceAsync gameStateService;

    public GameBoardPresenter(GameBoardView gameBoardView) {
        this.view = gameBoardView;
    }

    @Override
    public void start() {
        bind();

        // todo: request all player info
        ArrayList<Player> players = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            players.add(new Player("player"+i));
        }
        players.get(0).setPlace(1);
        players.get(3).setPlace(3);

        //todo: replace 2nd argument, maybe include isPlaying in player property
        view.drawPlayers(players, 1);
    }

    @Override
    public void stop() {

    }

    private void bind() {
        // todo: bind clickhandlers
    }
}
