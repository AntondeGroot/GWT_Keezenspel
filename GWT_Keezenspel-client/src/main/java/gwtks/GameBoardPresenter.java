package gwtks;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import gwtks.animations.StepsAnimation;
import gwtks.handlers.CanvasClickHandler;
import gwtks.handlers.ForfeitHandler;
import gwtks.handlers.SendHandler;
import gwtks.services.PollingService;

import java.util.ArrayList;

public class GameBoardPresenter implements Presenter{
    private final GameBoardModel model;
    private Board boardModel;
    private final GameBoardView view;
    private final GameStateServiceAsync gameStateService;
    //todo: to remove
    private Context2d ctxPawns;
    private Context2d ctxBoard;

    public GameBoardPresenter(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameStateServiceAsync gameStateService) {
        this.model = gameBoardModel;
        this.view = gameBoardView;
        this.gameStateService = gameStateService;
    }

    @Override
    public void start() {
        bind();

        // todo: make service an input parameter
        PollingService pollingService = GWT.create(PollingService.class);
        pollingService.startPolling(200, this::pollServerForGameState);

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
                boardModel = new Board();
                boardModel.createBoard(players.size(), view.getCanvasBoard().getWidth()); // todo: make createBoard accept a list of players
            }
        });
        view.canvasWrapper.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // todo: replace getAbsoluteLeft / getAbsoluteTop by a non-deprecated way
                int canvasLeft = DOM.getAbsoluteLeft(view.getCanvasCards()) - Window.getScrollLeft();
                int canvasTop = DOM.getAbsoluteTop(view.getCanvasCards()) - Window.getScrollTop();
                int x = event.getClientX() - canvasLeft;
                int y = event.getClientY() - canvasTop + 30;

                GWT.log("Clicked at: (" + x + ", " + y + ")");
                // todo: maybe replace x,y parameters
                CanvasClickHandler.handleCanvasClick(event,x,y);
            }
        }, ClickEvent.getType());
        //todo: remove
        Document document = Document.get();
        ctxPawns = ((CanvasElement) document.getElementById("canvasPawns")).getContext2d();
        ctxBoard = ((CanvasElement) document.getElementById("canvasBoard")).getContext2d();
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

    public void pollServerForGameState(){
        gameStateService.getGameState( new AsyncCallback<GameStateResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.reset();
            }
            public void onSuccess(GameStateResponse result) {
                GWT.log(result.toString());
                // only set the board when empty, e.g.
                // when the browser was refreshed or when you join the game for the first time
                if(!Board.isInitialized()){
                    Board board = new Board();
                    Board.setPawns(result.getPawns());
                    board.createBoard(result.getNrPlayers(),600);
                    board.drawBoard(ctxBoard);// todo: remove
                    board.drawBoard(view.getCanvasBoard().getContext2d());
                    board.drawPawns(ctxPawns); // todo: remove
                    board.drawPawns(view.getCanvasPawns().getContext2d());
                    PlayerList.setNrPlayers(result.getNrPlayers());
                }
                PlayerList playerList = new PlayerList();
                PlayerList.setActivePlayers(result.getActivePlayers());
                PlayerList.setWinners(result.getWinners());
                playerList.setPlayerIdPlayingAndDrawPlayerList(result.getPlayerIdTurn());
            }
        } );
    }
}
