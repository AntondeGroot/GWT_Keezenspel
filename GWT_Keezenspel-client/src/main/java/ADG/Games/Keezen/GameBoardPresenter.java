package ADG.Games.Keezen;

import ADG.Games.Keezen.animations.GameAnimation;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ADG.Games.Keezen.animations.StepsAnimation;
import ADG.Games.Keezen.handlers.CanvasClickHandler;
import ADG.Games.Keezen.handlers.ForfeitHandler;
import ADG.Games.Keezen.handlers.SendHandler;
import ADG.Games.Keezen.services.PollingService;

import java.util.ArrayList;

public class GameBoardPresenter{
    private final GameBoardModel model;
    private Board boardModel;
    private final GameBoardView view;
    private final GameStateServiceAsync gameStateService;
    private final CardsServiceAsync cardsService;
    private final PollingService pollingService;
    private GameAnimation gameAnimation;

    public GameBoardPresenter(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameStateServiceAsync gameStateService, CardsServiceAsync cardsService, PollingService pollingService) {
        this.model = gameBoardModel;
        this.view = gameBoardView;
        this.gameStateService = gameStateService;
        this.cardsService = cardsService;
        this.pollingService = pollingService;
    }

    public void start() {
        // Start the game
        gameAnimation = new GameAnimation();
        animate();
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

                        boardModel.createBoard(players, 600); // todo: make createBoard accept a list of players
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
                TileId tileId = Board.getTileId(x,y);
                if(tileId != null) {
                    GWT.log("you clicked TileId: " + tileId);
                }
                // todo: maybe replace x,y parameters
                CanvasClickHandler.handleCanvasClick(event,x,y);
            }
        }, ClickEvent.getType());
    }

    public void stop() {
        // todo: deregister all binders
        pollingService.stopPolling();
    }

    private void bind() {
        view.getSendButton().addClickHandler(new SendHandler());
        view.getForfeitButton().addClickHandler(new ForfeitHandler());

        Element canvasElement = view.getCanvasCards();
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
                    GWT.log("server created nr pawns: "+result.getPawns().size());
                    GWT.log(result.getPawns().toString());
                    GWT.log("poll server board.create"+result);
                    board.createBoard(result.getPlayers(),600);
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
                view.enableButtons(result.getPlayerIdTurn().equals(Cookie.getPlayerId()));
            }
        });
    }

    private void pollServerForCards(){
        // todo: this should actually only get the cards for the Cookie.playerId
        PawnAndCardSelection.setPlayerId(Cookie.getPlayerId());
        cardsService.getCards(Cookie.getPlayerId(), new AsyncCallback<CardResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.reset();
            }
            public void onSuccess(CardResponse result) {
                if(CardsDeck.areCardsDifferent(result.getCards())){
                    CardsDeck.setCards(result.getCards());
                    view.drawCards(CardsDeck.getCards());
                    PlayerList.refresh();
                }
            }
        } );
    }

    public void animate(){
        view.getCanvasPawnsContext().clearRect(0,0, view.getCanvasPawns().getWidth(), view.getCanvasPawns().getHeight());
        gameAnimation.update();
        gameAnimation.draw();
        AnimationScheduler.AnimationCallback animationCallback = new AnimationScheduler.AnimationCallback() {
            @Override
            public void execute(double v) {
                animate();
            }
        };
        AnimationScheduler.get().requestAnimationFrame(animationCallback);
    }
}
