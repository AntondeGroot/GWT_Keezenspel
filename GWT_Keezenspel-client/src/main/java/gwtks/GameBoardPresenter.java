package gwtks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import gwtks.handlers.CanvasClickHandler;
import gwtks.handlers.ForfeitHandler;
import gwtks.handlers.SendHandler;

import java.util.ArrayList;

public class GameBoardPresenter implements Presenter{
    private final GameBoardModel model;
    private final GameBoardView view;
    private final GameStateServiceAsync gameStateService;

    public GameBoardPresenter(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameStateServiceAsync gameStateService) {
        this.model = gameBoardModel;
        this.view = gameBoardView;
        this.gameStateService = gameStateService;
    }

    @Override
    public void start() {
        bind();

        // todo: remove the following: test data
        Timer timer = new Timer() {
            @Override
            public void run() {
            }
        };
        timer.schedule(100);

        for (int i = 0; i < 8; i++) {
            Player player = new Player("player"+i,"123-567");
            if(i==0){
                player.setIsPlaying(true);
            }
            gameStateService.addPlayer(player, new AsyncCallback<Player>() {
                @Override
                public void onFailure(Throwable throwable) {
                }
                @Override
                public void onSuccess(Player o) {
                }
            });
            timer.run();
        }
        // todo: remove the above: TESTDATA
        timer.run();
        gameStateService.getPlayers(new AsyncCallback<ArrayList<Player>>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(ArrayList<Player> players) {
                GWT.log("players = "+players);
                model.setPlayers(players);
                view.drawPlayers(players);
            }
        });
    }

    @Override
    public void stop() {
        // todo: deregister all binders
    }

    private void bind() {
        view.getSendButton().addClickHandler(new SendHandler());
        view.getForfeitButton().addClickHandler(new ForfeitHandler());

        Element canvasElement = (Element) view.getCanvasCards();
        DOM.sinkEvents(canvasElement, Event.ONCLICK);
        DOM.setEventListener(canvasElement, event -> {
            if (DOM.eventGetType(event) == Event.ONCLICK) {
                new CanvasClickHandler();
            }
        });
    }
}
