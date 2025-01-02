package gwtks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
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
    private final CardsServiceAsync cardsService = GWT.create(CardsService.class);//todo: remove from presenter and inject it
    // todo: make service an input parameter
    private final PollingService pollingService = GWT.create(PollingService.class);

    public GameBoardPresenter(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameStateServiceAsync gameStateService) {
        this.model = gameBoardModel;
        this.view = gameBoardView;
        this.gameStateService = gameStateService;
    }

    @Override
    public void start() {
        bind();

        pollingService.startPolling(200, this::pollServerForUpdates);

        //todo: bind startGame method to a button
        gameStateService.startGame(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log("Game is already running");
            }
            @Override
            public void onSuccess(Void o) {
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
                        GWT.log("gameStateService getPlayers board.create");

                        boardModel.createBoard(players.size(), 600); // todo: make createBoard accept a list of players
                    }
                });
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
    }

    @Override
    public void stop() {
        // todo: deregister all binders
        pollingService.stopPolling();
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

    private void pollServerForUpdates(){
        pollServerForGameState();
        pollServerForCards();
    }

    private void pollServerForGameState(){
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
                    GWT.log("poll server board.create"+result);
                    board.createBoard(result.getNrPlayers(),600);
                    board.drawBoard(view.getCanvasBoardContext()); // todo: make view.drawBoard(model);
                    board.drawPawns(view.getCanvasPawnsContext());
                    PlayerList.setNrPlayers(result.getNrPlayers());
                }
                // update model
                PlayerList playerList = new PlayerList();
                PlayerList.setActivePlayers(result.getActivePlayers());
                PlayerList.setWinners(result.getWinners());
                if(!PlayerList.isIsUpToDate()){//todo: modernize
                    gameStateService.getPlayers(new AsyncCallback<ArrayList<Player>>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                        }

                        @Override
                        public void onSuccess(ArrayList<Player> players) {
                            model.setPlayers(players);
                            view.getPlayerListContainer().clear();
                            view.drawPlayers(model.getPlayers());
                }});}
                playerList.setPlayerIdPlayingAndDrawPlayerList(result.getPlayerIdTurn());// todo: old
            }
        });
    }

    private void pollServerForCards(){
        // todo: this should actually only get the cards for the Cookie.playerId
        cardsService.getCards(PlayerList.getPlayerIdPlaying(), new AsyncCallback<CardResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.reset();
            }
            public void onSuccess(CardResponse result) {

                PawnAndCardSelection.setPlayerId(result.getPlayerId());
                if(CardsDeck.areCardsDifferent(result.getCards())){
                    CardsDeck.setCards(result.getCards());
                    view.drawCards(CardsDeck.getCards());// todo: keep new method
                    PlayerList.refresh();
                }
            }
        } );
    }
}
