package ADG.Games.Keezen;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Cards.CardResponse;
import ADG.Games.Keezen.Cards.CardsServiceAsync;
import ADG.Games.Keezen.State.GameStateResponse;
import ADG.Games.Keezen.State.GameStateServiceAsync;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Move.MovingServiceAsync;
import ADG.Games.Keezen.Player.Player;
import ADG.Games.Keezen.animations.*;
import ADG.Games.Keezen.handlers.SendHandler;
import ADG.Games.Keezen.handlers.TestMoveHandler;
import ADG.Games.Keezen.services.PollingService;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

import static ADG.Games.Keezen.Move.MoveType.FORFEIT;
import static ADG.Games.Keezen.Cards.CardValueCheck.isSeven;
import static ADG.Games.Keezen.ViewHelpers.ViewDrawing.drawTransparentCircle;
import static java.lang.String.valueOf;

public class GameBoardPresenter {
    private final GameBoardModel model;
    private CardResponse storedCardResponse;
    private Board boardModel;
    private final GameBoardView view;
    private final GameStateServiceAsync gameStateService;
    private final CardsServiceAsync cardsService;
    private final MovingServiceAsync movingService;
    private final PollingService pollingService;
    private GameStateResponse gameStateResponseUpdate = new GameStateResponse();
    private double loopAlpha = 0.6;
    private final PawnAndCardSelection pawnAndCardSelection;
    private final PlayerList playerList = new PlayerList();
    private final CardsDeck cardsDeck = new CardsDeck();

    private MoveResponse storedMoveResponse = new MoveResponse();

    public GameBoardPresenter(GameBoardModel gameBoardModel, GameBoardView gameBoardView, GameStateServiceAsync gameStateService, CardsServiceAsync cardsService, MovingServiceAsync movingService, PollingService pollingService) {
        this.model = gameBoardModel;
        this.view = gameBoardView;
        this.gameStateService = gameStateService;
        this.cardsService = cardsService;
        this.movingService = movingService;
        this.pollingService = pollingService;
        this.storedCardResponse = new CardResponse();
        pawnAndCardSelection = new PawnAndCardSelection();
    }

    public void start() {
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
    }

    private void pollServerForUpdates() {
        pollServerForGameState();
        pollServerForCards();
        pollServerForMove();
    }

    private void pollServerForMove(){
        movingService.getMove(new AsyncCallback<MoveResponse>() {
            @Override public void onFailure(Throwable throwable) {}

            @Override
            public void onSuccess(MoveResponse moveResponse) {
                if(!moveResponse.equals(storedMoveResponse) && !moveResponse.equals(new MoveResponse())){
                    StepsAnimation.resetStepsAnimation();
                    MoveController.movePawn(moveResponse);
                    draw();
                    GWT.log(moveResponse.toString());
                    storedMoveResponse = moveResponse;
                }
            }
        });
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
        view.createPawns(result.getPawns(), pawnAndCardSelection);
        view.animatePawns();
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
                            pawnAndCardSelection);
                    playerList.refresh();
                }
            }
        });
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
                    pawnAndCardSelection);
            pawnAndCardSelection.setCardsAreDrawn();
        }
        view.animatePawns();
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
