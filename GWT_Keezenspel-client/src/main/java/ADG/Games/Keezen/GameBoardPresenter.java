package ADG.Games.Keezen;

import ADG.Games.Keezen.animations.GameAnimation;
import ADG.Games.Keezen.handlers.TestMoveHandler;
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
import static ADG.Games.Keezen.Util.CardValueCheck.isSeven;

public class GameBoardPresenter {
    private final GameBoardModel model;
    private CardResponse storedCardResponse;
    private Board boardModel;
    private final GameBoardView view;
    private final GameStateServiceAsync gameStateService;
    private final CardsServiceAsync cardsService;
    private final PollingService pollingService;
    private GameAnimation gameAnimation;
    private GameStateResponse gameStateResponseUpdate = new GameStateResponse();

    public GameBoardPresenter(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameStateServiceAsync gameStateService, CardsServiceAsync cardsService, PollingService pollingService) {
        this.model = gameBoardModel;
        this.view = gameBoardView;
        this.gameStateService = gameStateService;
        this.cardsService = cardsService;
        this.pollingService = pollingService;
        this.storedCardResponse = new CardResponse();
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
                        GWT.log("players = " + players);
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
                TileId tileId = Board.getTileId(x, y);
                if (tileId != null) {
                    GWT.log("you clicked TileId: " + tileId);
                }
                // todo: maybe replace x,y parameters
                CanvasClickHandler.handleCanvasClick(event, x, y, view.getStepsPawn1(), view.getStepsPawn2());
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

        view.stepsPawn1.addChangeHandler(event -> {
            String value = view.stepsPawn1.getValue(); // Get the current value of the TextBox

            // Check if the value is of length 1 and numerical
            if (value.length() == 1 && value.matches("\\d")) {
                if (Integer.parseInt(value) > 7 || Integer.parseInt(value) < 0) {
                    view.stepsPawn1.setValue("4");
                    view.stepsPawn2.setValue("3");
                    PawnAndCardSelection.setNrStepsPawn1(4);
                    PawnAndCardSelection.setNrStepsPawn2(3);
                } else {
                    view.stepsPawn2.setValue(String.valueOf(7 - Integer.parseInt(value)));
                    PawnAndCardSelection.setNrStepsPawn1(Integer.parseInt(value));
                    PawnAndCardSelection.setNrStepsPawn2(7 - Integer.parseInt(value));
                }
            } else {
                // Invalid input
                view.stepsPawn1.setValue("4");
                view.stepsPawn2.setValue("3");
                PawnAndCardSelection.setNrStepsPawn1(4);
                PawnAndCardSelection.setNrStepsPawn2(3);
            }
            new TestMoveHandler().sendMoveToServer();
        });
    }

    private void pollServerForUpdates() {
        pollServerForGameState();
        pollServerForCards();
    }

    private void pollServerForGameState() {
        gameStateService.getGameState(new AsyncCallback<GameStateResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.reset();
            }

            public void onSuccess(GameStateResponse result) {
                if (!gameStateResponseUpdate.equals(result)) {
                    GWT.log("\n" + result.toString());
                    gameStateResponseUpdate = result;
                } else {
                    // todo: maybe skip something down below,
//                    return;
                }
                // only set the board when empty, e.g.
                // when the browser was refreshed or when you join the game for the first time
                if (!Board.isInitialized()) {
                    Board board = new Board();
                    Board.setPawns(result.getPawns());
                    GWT.log("server created nr pawns: " + result.getPawns().size());
                    GWT.log(result.getPawns().toString());
                    GWT.log("poll server board.create" + result);
                    board.createBoard(result.getPlayers(), 600);
                    view.drawBoard(Board.getTiles(), result.getPlayers(), Board.getCellDistance());
                    board.drawPawns(view.getCanvasPawnsContext());
                    PlayerList.setNrPlayers(result.getNrPlayers());
                }
                // update model
                PlayerList playerList = new PlayerList();
                PlayerList.setActivePlayers(result.getActivePlayers());
                PlayerList.setWinners(result.getWinners());
                if (!PlayerList.isIsUpToDate()) {
                    gameStateService.getPlayers(new AsyncCallback<ArrayList<Player>>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                        }

                        @Override
                        public void onSuccess(ArrayList<Player> players) {
                            GWT.log("Players were updated: " + players);
                            model.setPlayers(players);
                            view.getPlayerListContainer().clear();
                            view.drawPlayers(model.getPlayers());
                        }
                    });
                }
                playerList.setPlayerIdPlayingAndDrawPlayerList(result.getPlayerIdTurn());// todo: old
                view.enableButtons(result.getPlayerIdTurn().equals(Cookie.getPlayerId()));
            }
        });
    }

    private void pollServerForCards() {
        PawnAndCardSelection.setPlayerId(Cookie.getPlayerId());
        cardsService.getCards(Cookie.getPlayerId(), new AsyncCallback<CardResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.reset();
            }

            public void onSuccess(CardResponse result) {

                if (!storedCardResponse.equals(result)) {
                    storedCardResponse = result;
                    GWT.log(result.toString());
                    CardsDeck.setCards(result.getCards());
                    CardsDeck.setNrCardsPerPlayer(result.getNrOfCardsPerPlayer());
                    CardsDeck.setPlayedCards(result.getPlayedCards());
                    view.drawCards(CardsDeck.getCards(), result.getNrOfCardsPerPlayer(), CardsDeck.getPlayedCards());
                    PlayerList.refresh();
                }
            }
        });
    }

    public void animate() {
        view.getCanvasPawnsContext().clearRect(0, 0, view.getCanvasPawns().getWidth(), view.getCanvasPawns().getHeight());
        gameAnimation.update();
        gameAnimation.draw();
        view.showPawnTextBoxes(showTextBoxes(PawnAndCardSelection.getCard()));
        AnimationScheduler.AnimationCallback animationCallback = new AnimationScheduler.AnimationCallback() {
            @Override
            public void execute(double v) {
                animate();
            }
        };
        AnimationScheduler.get().requestAnimationFrame(animationCallback);
    }

    private boolean showTextBoxes(Card card) {
        if (card == null) {
            return false;
        }
        if (!isSeven(card)) {
            return false;
        }
        if (PawnAndCardSelection.getPawnId2() == null) {
            return false;
        }
        return true;
    }
}
