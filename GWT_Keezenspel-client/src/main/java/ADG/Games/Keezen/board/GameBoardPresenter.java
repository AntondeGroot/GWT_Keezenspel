package ADG.Games.Keezen.board;

import ADG.Games.Keezen.Cards.CardResponse;
import ADG.Games.Keezen.Cards.CardsServiceAsync;
import ADG.Games.Keezen.CardsDeck;
import ADG.Games.Keezen.MoveController;
import ADG.Games.Keezen.PawnAndCardSelection;
import ADG.Games.Keezen.PlayerList;
import ADG.Games.Keezen.State.GameStateResponse;
import ADG.Games.Keezen.State.GameStateServiceAsync;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Move.MovingServiceAsync;
import ADG.Games.Keezen.Player.Player;
import ADG.Games.Keezen.animations.*;
import ADG.Games.Keezen.moving.Move;
import ADG.Games.Keezen.services.PollingService;
import ADG.Games.Keezen.util.Cookie;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

import static ADG.Games.Keezen.Move.MoveType.FORFEIT;
import static ADG.Games.Keezen.ViewHelpers.ViewDrawing.updatePlayerProfileUI;
import static java.lang.String.valueOf;

public class GameBoardPresenter {
    private CardResponse storedCardResponse;
    private Board boardModel;
    private final GameBoardView view;
    private final GameStateServiceAsync gameStateService;
    private final CardsServiceAsync cardsService;
    private final MovingServiceAsync movingService;
    private final PollingService pollingService;
    private GameStateResponse gameStateResponseUpdate = new GameStateResponse();
    private final PawnAndCardSelection pawnAndCardSelection;
    private final PlayerList playerList = new PlayerList();
    private final CardsDeck cardsDeck = new CardsDeck();
    private final int BOARD_SIZE = 600; // todo: replace with CSS properties

    private MoveResponse storedMoveResponse = new MoveResponse();

    public GameBoardPresenter(GameBoardView gameBoardView, GameStateServiceAsync gameStateService, CardsServiceAsync cardsService, MovingServiceAsync movingService, PollingService pollingService) {
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
                Move.makeMove(pawnAndCardSelection.createMoveMessage());
            }
        }, ClickEvent.getType());

        view.getForfeitButton().addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pawnAndCardSelection.setMoveType(FORFEIT);
                Move.makeMove(pawnAndCardSelection.createMoveMessage());
            }
        }, ClickEvent.getType());

        view.stepsPawn1.addChangeHandler(event -> {
            // validate entry
            pawnAndCardSelection.setNrStepsPawn1ForSplit(view.stepsPawn1.getValue());
            // split entry over the 2 text boxes
            view.stepsPawn1.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn1()));
            view.stepsPawn2.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn2()));

            Move.testMove(pawnAndCardSelection.createTestMoveMessage());
        });
    }

    private void pollServerForUpdates() {
        pollServerForGameState();
        pollServerForCards();
        pollServerForMove();
    }

    private void pollServerForMove(){
        movingService.getMove(Cookie.getSessionID(), new AsyncCallback<MoveResponse>() {
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
        gameStateService.getGameState(Cookie.getSessionID(), new AsyncCallback<GameStateResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.resetStepsAnimation();
            }

            public void onSuccess(GameStateResponse result) {
                if (!Board.isInitialized()) {
                    initializeBoardState(result);
                    AnimationSpeed.setSpeed(result.getAnimationSpeed());
                }

                if (!gameStateResponseUpdate.equals(result)) {
                    GWT.log(result.toString());
                    gameStateResponseUpdate = result;
                    Board.setPawns(result.getPawns());
                    pawnAndCardSelection.updatePawns(result.getPawns());
                }
                // only set the board when empty, e.g.
                // when the browser was refreshed or when you join the game for the first time
                updatePlayerList(result);
                view.enableButtons(currentPlayerIsPlaying(result));
            }
        });
    }

    private void initializeBoardState(GameStateResponse result) {
        Board board = new Board();
        Board.setPawns(result.getPawns());
        board.createBoard(result.getPlayers(), BOARD_SIZE);
        view.drawBoard(Board.getTiles(), result.getPlayers(), Board.getCellDistance());
        view.createPawns(result.getPawns(), pawnAndCardSelection);
        view.animatePawns();
    }

    private void updatePlayerList(GameStateResponse result) {
        playerList.setPlayers(result.getPlayers());
        if (!playerList.isIsUpToDate()) {
            gameStateService.getPlayers(Cookie.getSessionID(), new AsyncCallback<ArrayList<Player>>() {
                @Override
                public void onFailure(Throwable throwable) {}

                @Override
                public void onSuccess(ArrayList<Player> players) {
                    updatePlayerProfileUI(players);
                }
            });
        }
    }

    private void initializeGame() {
        gameStateService.startGame(Cookie.getSessionID(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log("Game is already running");
                try{
                    fetchAndInitializePlayers();
                }catch (Exception ignored){
                }
            }

            @Override
            public void onSuccess(Void o) {
                fetchAndInitializePlayers();
            }
        });
    }

    private void fetchAndInitializePlayers(){
        gameStateService.getPlayers(Cookie.getSessionID(), new AsyncCallback<ArrayList<Player>>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(ArrayList<Player> players) {
                GWT.log("players = " + players);
                view.createPlayerList(players);
                boardModel = new Board();
                GWT.log("gameStateService getPlayers board.create");

                boardModel.createBoard(players, BOARD_SIZE);
            }
        });
    }

    private void pollServerForCards() {
        pawnAndCardSelection.setPlayerId(Cookie.getPlayerId());
        cardsService.getCards(Cookie.getSessionID(), Cookie.getPlayerId(), new AsyncCallback<CardResponse>() {
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

    public void draw(){

        if(pawnAndCardSelection.getDrawCards()) {
            view.drawCards(
                    cardsDeck,
                    pawnAndCardSelection);
            pawnAndCardSelection.setCardsAreDrawn();
        }
        view.animatePawns();
    }

    private boolean currentPlayerIsPlaying(GameStateResponse result){
        return result.getPlayerIdTurn().equals(Cookie.getPlayerId());
    }
}
