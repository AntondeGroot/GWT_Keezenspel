package ADG.Games.Keezen.board;

import ADG.Games.Keezen.Cards.CardResponse;
import ADG.Games.Keezen.Cards.CardsServiceAsync;
import ADG.Games.Keezen.CardsDeck;
import ADG.Games.Keezen.PawnAndCardSelection;
import ADG.Games.Keezen.PlayerList;
import ADG.Games.Keezen.State.GameStateResponse;
import ADG.Games.Keezen.State.GameStateServiceAsync;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Move.MovingServiceAsync;
import ADG.Games.Keezen.TileId;
import ADG.Games.Keezen.animations.*;
import ADG.Games.Keezen.dto.CardClient;
import ADG.Games.Keezen.dto.CardDTO;
import ADG.Games.Keezen.dto.GameStateClient;
import ADG.Games.Keezen.dto.GameStateDTO;
import ADG.Games.Keezen.dto.TestMoveResponseDTO;
import ADG.Games.Keezen.moving.Move;
import ADG.Games.Keezen.services.ApiClient;
import ADG.Games.Keezen.services.ApiClient.ApiCallback;
import ADG.Games.Keezen.services.PollingService;
import ADG.Games.Keezen.util.Cookie;
import ADG.Games.Keezen.util.MoveRequestJsonBuilder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import java.util.ArrayList;

import static ADG.Games.Keezen.Move.MoveType.FORFEIT;
import static ADG.Games.Keezen.ViewHelpers.ViewDrawing.updatePlayerProfileUI;
import static java.lang.String.valueOf;

public class GameBoardPresenter {

  private CardResponse storedCardResponse;
  private boolean requestInProgress = false;
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
  private final ApiClient apiClient = new ApiClient();
  private long gameStateVersion = 0;
  private final int BOARD_SIZE = 600; // todo: replace with CSS properties

  private MoveResponse storedMoveResponse = new MoveResponse();

  public GameBoardPresenter(GameBoardView gameBoardView, GameStateServiceAsync gameStateService,
      CardsServiceAsync cardsService, MovingServiceAsync movingService,
      PollingService pollingService) {
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
    pollingService.startPolling(600, this::pollServerForUpdates);
  }

  public void stop() {
    // todo: deregister all binders
    pollingService.stopPolling();
  }

  private void bindEventHandlers() {
    view.getSendButton().addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        GWT.log("pawn 1: "+pawnAndCardSelection.getPawn1());
        MoveRequestJsonBuilder builder = new MoveRequestJsonBuilder()
            .withPlayerId(Cookie.getPlayerId())
            .withCardId(pawnAndCardSelection.getCard())
            .withMoveType("move")
            .withPawn1(pawnAndCardSelection.getPawn1())
            .withPawn2(pawnAndCardSelection.getPawn2())
            .withStepsPawn1(pawnAndCardSelection.getNrStepsPawn1())
            .withStepsPawn2(pawnAndCardSelection.getNrStepsPawn2())
            .withTempMessageType("CHECK_MOVE");

        GWT.log("testmove anton: "+builder.build());

//        apiClient.makeMove(Cookie.getSessionID(), Cookie.getPlayerId(), builder.build(), new ApiCallback<TestMoveResponseDTO>() {
//          @Override
//          public void onSuccess(MoveResponseDTO result) {
//          }
//
//          @Override
//          public void onHttpError(int statusCode, String statusText) {
//          }
//
//          @Override
//          public void onFailure(Throwable caught) {
//          }
//        });
      }
    }, ClickEvent.getType());

    view.getForfeitButton().addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        pawnAndCardSelection.reset();
        apiClient.playerForfeits(Cookie.getSessionID(), Cookie.getPlayerId(), new ApiCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
          }

          @Override
          public void onFailure(Throwable caught) {
          }
        });
      }
    }, ClickEvent.getType());

    view.stepsPawn1.addChangeHandler(event -> {
      // validate entry
      pawnAndCardSelection.setNrStepsPawn1ForSplit(view.stepsPawn1.getValue());
      // split entry over the 2 text boxes
      view.stepsPawn1.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn1()));
      view.stepsPawn2.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn2()));

      GWT.log("pawn 1: "+pawnAndCardSelection.getPawn1());
      MoveRequestJsonBuilder builder = new MoveRequestJsonBuilder()
          .withPlayerId(Cookie.getPlayerId())
          .withCardId(pawnAndCardSelection.getCard())
          .withMoveType("move")
          .withPawn1(pawnAndCardSelection.getPawn1())
          .withPawn2(pawnAndCardSelection.getPawn2())
          .withStepsPawn1(pawnAndCardSelection.getNrStepsPawn1())
          .withStepsPawn2(pawnAndCardSelection.getNrStepsPawn2())
          .withTempMessageType("CHECK_MOVE");

      GWT.log("testmove anton: "+builder.build());

      apiClient.checkMove(Cookie.getSessionID(), Cookie.getPlayerId(), builder.build(), new ApiCallback<TestMoveResponseDTO>() {
        @Override
        public void onSuccess(TestMoveResponseDTO result) {
          ArrayList<TileId> tiles = new ArrayList<>();
          for (int i = 0; i < result.getTiles().length(); i++) {
            tiles.add(
                new TileId(
                    result.getTiles().get(i).getPlayerId(),
                    result.getTiles().get(i).getTileNr()));
          }
          GWT.log("tiles = " + tiles);
          StepsAnimation.updateStepsAnimation(tiles);
        }

        @Override
        public void onHttpError(int statusCode, String statusText) {
        }

        @Override
        public void onFailure(Throwable caught) {
        }
      });
    });
  }

  private void pollServerForUpdates() {

    apiClient.getGameState(Cookie.getSessionID(), gameStateVersion,
        new ApiClient.ApiCallback<GameStateDTO>() {
          @Override
          public void onSuccess(GameStateDTO response) {
            GWT.log("Game State Response: " + response.getVersion());
            // first save the gameStateVersion if something were to crash it won't ask for a state it doesn't need
            gameStateVersion = (long) response.getVersion();

            // convert DTO objects to 'real' objects
            GameStateClient gameStateClient = new GameStateClient(response);

            updateGameState(gameStateClient);
//            pollServerForGameState();
            pollServerForCards();
//            pollServerForMove();
          }

          @Override
          public void onHttpError(int statusCode, String statusText) {
            switch (statusCode) {
              case 304:
                GWT.log("ℹ️ Game state not modified — no update needed.");
                break;
              case 404:
                GWT.log("⚠️ Game session not found (404).");
                break;
              case 400:
                GWT.log("🚫 Bad request: " + statusText);
                break;
              default:
                GWT.log("❌ HTTP Error " + statusCode + ": " + statusText);
                break;
            }
          }

          @Override
          public void onFailure(Throwable caught) {

          }
        });
  }

//  private void pollServerForMove() {
//    movingService.getMove(Cookie.getSessionID(), new AsyncCallback<MoveResponse>() {
//      @Override
//      public void onFailure(Throwable throwable) {
//      }
//
//      @Override
//      public void onSuccess(MoveResponse moveResponse) {
//        if (!moveResponse.equals(storedMoveResponse) && !moveResponse.equals(new MoveResponse())) {
//          StepsAnimation.resetStepsAnimation();
//          MoveController.movePawn(moveResponse);
//          draw();
//          GWT.log(moveResponse.toString());
//          storedMoveResponse = moveResponse;
//        }
//      }
//    });
//  }

  private void updateGameState(GameStateClient gameState) {
    GWT.log("Game State initialize board: ");
    if (!Board.isInitialized()) {
      GWT.log("Game State board not yet initialized.");
      initializeBoardState(gameState);
      AnimationSpeed.setSpeed(1);
      view.createPlayerList(gameState.getPlayers());
    }
    updatePlayerProfileUI(gameState.getPlayers());

    GWT.log("set pawns: ");
    Board.setPawns(gameState.getPawns());
    GWT.log("update pawns");
    pawnAndCardSelection.updatePawns(gameState.getPawns());

    // only set the board when empty, e.g.
    // when the browser was refreshed or when you join the game for the first time
    view.enableButtons(currentPlayerIsPlaying(gameState));


  }

  private void pollServerForGameState() {
//    gameStateService.getGameState(Cookie.getSessionID(), new AsyncCallback<GameStateResponse>() {
//      public void onFailure(Throwable caught) {
//        StepsAnimation.resetStepsAnimation();
  }

  public void onSuccess(GameStateResponse result) {
//        if (!Board.isInitialized()) {
//          initializeBoardState(result);
//          AnimationSpeed.setSpeed(result.getAnimationSpeed());
//        }
//
//        if (!gameStateResponseUpdate.equals(result)) {
//          GWT.log(result.toString());
//          gameStateResponseUpdate = result;
//          Board.setPawns(result.getPawns());
//          pawnAndCardSelection.updatePawns(result.getPawns());
//        }
//        // only set the board when empty, e.g.
//        // when the browser was refreshed or when you join the game for the first time
//        updatePlayerList(result);
//        view.enableButtons(currentPlayerIsPlaying(result));
//      }
//    });
  }

  private void initializeBoardState(GameStateClient result) {
    Board board = new Board();
    GWT.log("set pawns");
    Board.setPawns(result.getPawns());
    GWT.log("create board");
    board.createBoard(result.getPlayers(), BOARD_SIZE);
    GWT.log("draw board");
    view.drawBoard(Board.getTiles(), result.getPlayers(), Board.getCellDistance());
    view.createPawns(result.getPawns(), pawnAndCardSelection);
    view.animatePawns();
  }

  private void updatePlayerList(GameStateClient result) {
    updatePlayerProfileUI(result.getPlayers());
//    if (!playerList.isIsUpToDate()) {
//      gameStateService.getPlayers(Cookie.getSessionID(), new AsyncCallback<ArrayList<Player>>() {
//        @Override
//        public void onFailure(Throwable throwable) {
//        }
//
//        @Override
//        public void onSuccess(ArrayList<Player> players) {
//          updatePlayerProfileUI(players);
//        }
//      });
//    }
  }

  private void initializeGame() {
//    gameStateService.startGame(Cookie.getSessionID(), new AsyncCallback<Void>() {
//      @Override
//      public void onFailure(Throwable throwable) {
//        GWT.log("Game is already running");
//        try {
//          fetchAndInitializePlayers();
//        } catch (Exception ignored) {
//        }
//      }
//
//      @Override
//      public void onSuccess(Void o) {
//        fetchAndInitializePlayers();
//      }
//    });
  }

  private void fetchAndInitializePlayers() {
//    gameStateService.getPlayers(Cookie.getSessionID(), new AsyncCallback<ArrayList<Player>>() {
//      @Override
//      public void onFailure(Throwable throwable) {
//      }
//
//      @Override
//      public void onSuccess(ArrayList<Player> players) {
//        GWT.log("players = " + players);
//        view.createPlayerList(players);
//        boardModel = new Board();
//        GWT.log("gameStateService getPlayers board.create");
//
//        boardModel.createBoard(players, BOARD_SIZE);
//      }
//    });
  }

  private void pollServerForCards() {

    if (requestInProgress) {
      GWT.log("Skipped poll — still waiting for previous response.");
      return;
    }
    GWT.log("polling server for cards");
    requestInProgress = true;
    pawnAndCardSelection.setPlayerId(Cookie.getPlayerId());
    apiClient.getPlayerCards(
        Cookie.getSessionID(),
        Cookie.getPlayerId(),
        new ApiClient.ApiCallback<JsArray<CardDTO>>() {
          @Override
          public void onSuccess(JsArray<CardDTO> cards) {
            ArrayList<CardClient> clientCards = new ArrayList();
            for (int i = 0; i < cards.length(); i++) {
              clientCards.add(new CardClient(cards.get(i)));
            }

            GWT.log("Received " + cards.length() + " cards from API:");
            cardsDeck.setCards(clientCards);
            view.drawCards(cardsDeck, pawnAndCardSelection);
            playerList.refresh();
            requestInProgress = false;
          }

          @Override
          public void onFailure(Throwable caught) {
            StepsAnimation.resetStepsAnimation();
            requestInProgress = false;
          }
        });
  }

  public void draw() {

    if (pawnAndCardSelection.getDrawCards()) {
      view.drawCards(
          cardsDeck,
          pawnAndCardSelection);
      pawnAndCardSelection.setCardsAreDrawn();
    }
    view.animatePawns();
  }

  private boolean currentPlayerIsPlaying(GameStateClient result) {
    return result.getCurrentPlayerId().equals(Cookie.getPlayerId());
  }
}
