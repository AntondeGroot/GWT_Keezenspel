package ADG.Games.Keezen;

import ADG.Games.Keezen.animations.*;
import ADG.Games.Keezen.handlers.SendHandler;
import ADG.Games.Keezen.handlers.TestMoveHandler;
import ADG.Games.Keezen.services.PollingService;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

import static ADG.Games.Keezen.MoveType.FORFEIT;
import static ADG.Games.Keezen.Util.CardValueCheck.isSeven;
import static ADG.Games.Keezen.ViewHelpers.ViewDrawing.drawTransparentCircle;
import static ADG.Games.Keezen.handlers.CanvasClickHandler.handleOnBoardClick;
import static ADG.Games.Keezen.handlers.CanvasClickHandler.handleOnCardsDeckClick;
import static java.lang.String.valueOf;

public class GameBoardPresenter {
    private final GameBoardModel model;
    private CardResponse storedCardResponse;
    private Board boardModel;
    private final GameBoardView view;
    private final GameStateServiceAsync gameStateService;
    private final CardsServiceAsync cardsService;
    private final PollingService pollingService;
    private GameStateResponse gameStateResponseUpdate = new GameStateResponse();
    private double loopAlpha = 0.6;
    private final PawnAndCardSelection pawnAndCardSelection;
    private final PlayerList playerList = new PlayerList();
    private final CardsDeck cardsDeck = new CardsDeck();

    public GameBoardPresenter(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameStateServiceAsync gameStateService, CardsServiceAsync cardsService, PollingService pollingService) {
        this.model = gameBoardModel;
        this.view = gameBoardView;
        this.gameStateService = gameStateService;
        this.cardsService = cardsService;
        this.pollingService = pollingService;
        this.storedCardResponse = new CardResponse();
        pawnAndCardSelection = new PawnAndCardSelection();
    }

    public void start() {
        animate();
        bindEventHandlers();
        startPollingServer();
        initializeGame();
    }

    private void startPollingServer() {
        pollingService.startPolling(200, this::pollServerForUpdates);
    }

    public void stop() {
        // todo: deregister all binders
        pollingService.stopPolling();
    }

    private void bindEventHandlers() {
        view.getSendButton().addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SendHandler.sendMoveToServer(pawnAndCardSelection.createMoveMessage());
            }
        }, ClickEvent.getType());

        view.getForfeitButton().addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pawnAndCardSelection.setMoveType(FORFEIT);
                SendHandler.sendMoveToServer(pawnAndCardSelection.createMoveMessage());
            }
        }, ClickEvent.getType());

        view.stepsPawn1.addChangeHandler(event -> {
            // validate entry
            pawnAndCardSelection.setNrStepsPawn1ForSplit(view.stepsPawn1.getValue());
            // split entry over the 2 text boxes
            view.stepsPawn1.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn1()));
            view.stepsPawn2.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn2()));

            TestMoveHandler.sendMoveToServer(pawnAndCardSelection.createTestMoveMessage());
        });

        view.canvasWrapper.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Point point = view.getPointClicked(event);
                if(point.getY() <= view.getCanvasBoard().getHeight()){
                    handleOnBoardClick(point, pawnAndCardSelection);
                }else{
                    handleOnCardsDeckClick(point, pawnAndCardSelection, cardsDeck);
                }
            }
        }, ClickEvent.getType());
    }

    private void pollServerForUpdates() {
        pollServerForGameState();
        pollServerForCards();
    }

    private void pollServerForGameState() {
        gameStateService.getGameState(new AsyncCallback<GameStateResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.resetStepsAnimation();
            }

            public void onSuccess(GameStateResponse result) {
                if (!gameStateResponseUpdate.equals(result)) {
                    GWT.log(result.toString());
                    gameStateResponseUpdate = result;
                }

                // only set the board when empty, e.g.
                // when the browser was refreshed or when you join the game for the first time
                if (!Board.isInitialized()) {
                    initializeBoardState(result);
                }
                updatePlayerList(result);
                view.enableButtons(currentPlayerIsPlaying(result));
            }
        });
    }

    private void initializeBoardState(GameStateResponse result) {
        Board board = new Board();
        Board.setPawns(result.getPawns());
        board.createBoard(result.getPlayers(), view.getBoardSize());
        view.drawBoard(Board.getTiles(), result.getPlayers(), Board.getCellDistance());
        view.drawPawns(result.getPawns(), pawnAndCardSelection);
    }

    private void updatePlayerList(GameStateResponse result) {
        playerList.setPlayers(result.getPlayers());
        if (!playerList.isIsUpToDate()) {
            gameStateService.getPlayers(new AsyncCallback<ArrayList<Player>>() {
                @Override
                public void onFailure(Throwable throwable) {}

                @Override
                public void onSuccess(ArrayList<Player> players) {
                    GWT.log("Players were updated: " + players);
                    model.setPlayers(players);
                    view.drawPlayers(model.getPlayers());
                }
            });
        }
    }

    private void initializeGame() {
        gameStateService.startGame(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log("Game is already running");
            }

            @Override
            public void onSuccess(Void o) {
                fetchAndInitializePlayers();
            }
        });
    }

    private void fetchAndInitializePlayers(){
        gameStateService.getPlayers(new AsyncCallback<ArrayList<Player>>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(ArrayList<Player> players) {
                GWT.log("players = " + players);
                model.setPlayers(players);
                view.drawPlayers(model.getPlayers());
                boardModel = new Board();
                GWT.log("gameStateService getPlayers board.create");

                boardModel.createBoard(players, view.getBoardSize());
            }
        });
    }

    private void pollServerForCards() {
        pawnAndCardSelection.setPlayerId(Cookie.getPlayerId());
        cardsService.getCards(Cookie.getPlayerId(), new AsyncCallback<CardResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.resetStepsAnimation();
            }

            public void onSuccess(CardResponse result) {

                if (!storedCardResponse.equals(result)) {
                    GWT.log(result.toString());
                    storedCardResponse = result;
                    cardsDeck.processCardResponse(result);
                    view.drawCards(
                            cardsDeck,
                            pawnAndCardSelection.getCard());
                    playerList.refresh();
                }
            }
        });
    }

    public void animate() {
        view.clearCanvasPawns();
        update(); // todo: improve
        draw();   // todo: improve
        view.showPawnTextBoxes(showTextBoxes(pawnAndCardSelection.getCard()));
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
        if (pawnAndCardSelection.getPawnId2() == null) {
            return false;
        }
        return true;
    }

    public void update(){
        view.clearCanvasSteps();
        if(pawnAndCardSelection.getDrawCards()) {
            view.clearCanvasCards();
        }
    }

    public void draw(){
        drawStepsAnimation();

        if(pawnAndCardSelection.getDrawCards()) {
            view.drawCards(
                    cardsDeck,
                    pawnAndCardSelection.getCard());
            pawnAndCardSelection.setCardsAreDrawn();
        }
        view.drawPawns(Board.getPawns(), pawnAndCardSelection);
    }

    public void drawStepsAnimation() {
        if(StepsAnimation.tileIdsToBeHighlighted == null){return;}

        view.clearCanvasSteps();

        loopAlpha -= 0.005;
        if (loopAlpha <= 0.0) {
            loopAlpha = 0.6; // make transparency run from 0.6 to 0
        }

        List<TileMapping>  tiles = Board.getTiles();
        for (TileId tileId : StepsAnimation.tileIdsToBeHighlighted) {
            for (TileMapping mapping : tiles) {
                if (mapping.getTileId().equals(tileId)) {
                    drawTransparentCircle(view.getCanvasStepsContext(), mapping.getPosition().getX(), mapping.getPosition().getY(),Board.getCellDistance()/2, loopAlpha);// todo: replace Board.getcelldistance
                }
            }
        }
    }

    private boolean currentPlayerIsPlaying(GameStateResponse result){
        return result.getPlayerIdTurn().equals(Cookie.getPlayerId());
    }
}
