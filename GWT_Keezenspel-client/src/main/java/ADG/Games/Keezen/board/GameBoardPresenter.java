package ADG.Games.Keezen.board;

import static ADG.Games.Keezen.ViewHelpers.ViewDrawing.updatePlayerProfileUI;
import static java.lang.String.valueOf;

import ADG.Games.Keezen.CardsDeck;
import ADG.Games.Keezen.PawnAndCardSelection;
import ADG.Games.Keezen.PlayerList;
import ADG.Games.Keezen.TileId;
import ADG.Games.Keezen.animations.*;
import ADG.Games.Keezen.dto.CardClient;
import ADG.Games.Keezen.dto.CardDTO;
import ADG.Games.Keezen.dto.GameStateClient;
import ADG.Games.Keezen.dto.GameStateDTO;
import ADG.Games.Keezen.dto.MoveResponseDTO;
import ADG.Games.Keezen.dto.PlayerClient;
import ADG.Games.Keezen.dto.TestMoveResponseDTO;
import ADG.Games.Keezen.services.ApiClient;
import ADG.Games.Keezen.services.ApiClient.ApiCallback;
import ADG.Games.Keezen.services.PollingService;
import ADG.Games.Keezen.util.ChatCipher;
import ADG.Games.Keezen.util.Cookie;
import ADG.Games.Keezen.util.MoveRequestJsonBuilder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import java.util.ArrayList;
import java.util.HashMap;

public class GameBoardPresenter {

  private boolean requestInProgress = false;
  private Board boardModel;
  private final GameBoardView view;
  private final PollingService pollingService;
  private final PawnAndCardSelection pawnAndCardSelection;
  private final PlayerList playerList = new PlayerList();
  private final CardsDeck cardsDeck = new CardsDeck();
  private final ApiClient apiClient = new ApiClient();
  private long gameStateVersion = 0;
  private int chatMessageCount = 0;
  private String myPlayerName = "";
  private final int BOARD_SIZE = 600; // todo: replace with CSS properties

  public GameBoardPresenter(GameBoardView gameBoardView, PollingService pollingService) {
    this.view = gameBoardView;
    this.pollingService = pollingService;
    pawnAndCardSelection = new PawnAndCardSelection();
  }

  public void start() {
    bindEventHandlers();
    startPollingServer();
  }

  private void startPollingServer() {
    pollingService.startPolling(600, this::pollServerForUpdates);
  }

  public void stop() {
    // todo: deregister all binders
    pollingService.stopPolling();
  }

  private void bindEventHandlers() {
    view.getChatSendButton()
        .addDomHandler(
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                String text = view.getChatInput().trim();
                if (text.isEmpty()) return;
                view.clearChatInput();
                JSONObject msg = new JSONObject();
                msg.put("sender", new JSONString(myPlayerName));
                msg.put("message", new JSONString(ChatCipher.encrypt(text, Cookie.getSessionID())));
                apiClient.sendChatMessage(Cookie.getSessionID(), msg, new ApiCallback<Void>() {
                  @Override public void onSuccess(Void result) {}
                  @Override public void onHttpError(int statusCode, String statusText) {}
                  @Override public void onFailure(Throwable caught) {}
                });
              }
            },
            ClickEvent.getType());

    view.getSendButton()
        .addDomHandler(
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                GWT.log("pawn 1: " + pawnAndCardSelection.getPawn1());
                MoveRequestJsonBuilder builder =
                    new MoveRequestJsonBuilder()
                        .withPlayerId(Cookie.getPlayerId())
                        .withCardId(pawnAndCardSelection.getCard())
                        .withPawn1(pawnAndCardSelection.getPawn1())
                        .withPawn2(pawnAndCardSelection.getPawn2())
                        .withStepsPawn1(pawnAndCardSelection.getNrStepsPawn1())
                        .withStepsPawn2(pawnAndCardSelection.getNrStepsPawn2())
                        .withTempMessageType("MAKE_MOVE");

                GWT.log("pawn 1: " + pawnAndCardSelection.getPawn1());
                apiClient.makeMove(
                    Cookie.getSessionID(),
                    Cookie.getPlayerId(),
                    builder.build(),
                    new ApiCallback<MoveResponseDTO>() {
                      @Override
                      public void onSuccess(MoveResponseDTO result) {
                        // todo: improve animation
                        GWT.log("make move successful");
                        view.animatePawns(result);
                      }

                      @Override
                      public void onHttpError(int statusCode, String statusText) {
                        GWT.log("make move HTTP error" + statusCode + ":" + statusText);
                        StepsAnimation.resetStepsAnimation();
                      }

                      @Override
                      public void onFailure(Throwable caught) {
                        GWT.log("make move Failure" + caught.getMessage());
                        StepsAnimation.resetStepsAnimation();
                      }
                    });
                GWT.log("testmove: " + builder.build());
              }
            },
            ClickEvent.getType());

    view.getForfeitButton()
        .addDomHandler(
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                pawnAndCardSelection.reset();
                apiClient.playerForfeits(
                    Cookie.getSessionID(),
                    Cookie.getPlayerId(),
                    new ApiCallback<Void>() {
                      @Override
                      public void onSuccess(Void result) {}

                      @Override
                      public void onFailure(Throwable caught) {}
                    });
              }
            },
            ClickEvent.getType());

    view.stepsPawn1.addChangeHandler(
        event -> {
          // validate entry
          pawnAndCardSelection.setNrStepsPawn1ForSplit(view.stepsPawn1.getValue());
          // split entry over the 2 text boxes
          view.stepsPawn1.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn1()));
          view.stepsPawn2.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn2()));

          GWT.log("pawn 1: " + pawnAndCardSelection.getPawn1());
          MoveRequestJsonBuilder builder =
              new MoveRequestJsonBuilder()
                  .withPlayerId(Cookie.getPlayerId())
                  .withCardId(pawnAndCardSelection.getCard())
                  .withPawn1(pawnAndCardSelection.getPawn1())
                  .withPawn2(pawnAndCardSelection.getPawn2())
                  .withStepsPawn1(pawnAndCardSelection.getNrStepsPawn1())
                  .withStepsPawn2(pawnAndCardSelection.getNrStepsPawn2())
                  .withTempMessageType("CHECK_MOVE");

          GWT.log("testmove anton: " + builder.build());

          apiClient.checkMove(
              Cookie.getSessionID(),
              Cookie.getPlayerId(),
              builder.build(),
              new ApiCallback<TestMoveResponseDTO>() {
                @Override
                public void onSuccess(TestMoveResponseDTO result) {
                  ArrayList<TileId> tiles = new ArrayList<>();
                  GWT.log("testmove was successful presenter YYYYYY" + result.toString());
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
                public void onHttpError(int statusCode, String statusText) {}

                @Override
                public void onFailure(Throwable caught) {}
              });
        });
  }

  private void pollServerForUpdates() {
    pollServerForChat();

    apiClient.getGameState(
        Cookie.getSessionID(),
        gameStateVersion,
        new ApiClient.ApiCallback<GameStateDTO>() {
          @Override
          public void onSuccess(GameStateDTO response) {
            GWT.log("Game State Response: " + response.getVersion());
            // first save the gameStateVersion if something were to crash it won't ask for a state
            // it doesn't need
            gameStateVersion = (long) response.getVersion();

            // convert DTO objects to 'real' objects
            GameStateClient gameStateClient = new GameStateClient(response);

            updateGameState(gameStateClient);
            pollServerForCards();
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
          public void onFailure(Throwable caught) {}
        });
  }

  private void updateGameState(GameStateClient gameState) {
    GWT.log("Game State initialize board: ");
    if (!Board.isInitialized()) {
      GWT.log("Game State board not yet initialized.");
      initializeBoardState(gameState);
      AnimationSpeed.setSpeed(1);
      view.createPlayerList(gameState.getPlayers());
      for (PlayerClient p : gameState.getPlayers()) {
        if (p.getId().equals(Cookie.getPlayerId())) {
          myPlayerName = p.getName();
          break;
        }
      }
    }
    updatePlayerProfileUI(gameState.getPlayers());

    GWT.log("set pawns: ");
    Board.setPawns(gameState.getPawns());
    GWT.log("update pawns");
    pawnAndCardSelection.updatePawns(gameState.getPawns());

    view.enableButtons(currentPlayerIsPlaying(gameState));
  }

  private void pollServerForChat() {
    apiClient.getChatMessages(Cookie.getSessionID(), new ApiCallback<JSONArray>() {
      @Override
      public void onSuccess(JSONArray messages) {
        if (messages.size() == chatMessageCount) return;
        chatMessageCount = messages.size();
        String key = Cookie.getSessionID();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
          JSONObject m = messages.get(i).isObject();
          if (m == null) continue;
          String timestamp = m.get("timestamp").isString().stringValue();
          String sender    = m.get("sender").isString().stringValue();
          String encrypted = m.get("message").isString().stringValue();
          sb.append(timestamp).append(" ").append(sender).append(": ")
            .append(ChatCipher.decrypt(encrypted, key)).append("\n");
        }
        view.refreshChat(sb.toString());
      }
      @Override public void onHttpError(int statusCode, String statusText) {}
      @Override public void onFailure(Throwable caught) {}
    });
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
  }

  private void pollServerForCards() {

    if (requestInProgress) {
      GWT.log("Skipped poll — still waiting for previous response.");
      return;
    }
    GWT.log("polling server for cards");
    requestInProgress = true;
    pawnAndCardSelection.setPlayerId(Cookie.getPlayerId());

    apiClient.getPubliclyAvailableCardInformation(
        Cookie.getSessionID(),
        new ApiClient.ApiCallback<JSONObject>() {
          @Override
          public void onSuccess(JSONObject publicCardInfo) {
            // parse playedCards from List<String> where each entry is "suit_value"
            ArrayList<CardClient> playedCards = new ArrayList<>();
            JSONArray playedCardsJson =
                publicCardInfo.get("playedCards").isArray();
            if (playedCardsJson != null) {
              for (int i = 0; i < playedCardsJson.size(); i++) {
                String cardId = playedCardsJson.get(i).isString().stringValue();
                String[] parts = cardId.split("_");
                playedCards.add(
                    new CardClient(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
              }
            }
            cardsDeck.setPlayedCards(playedCards);

            // parse nrOfCardsPerPlayer
            JSONObject nrCardsJson =
                publicCardInfo.get("nrOfCardsPerPlayer").isObject();
            HashMap<String, Integer> nrCardsMap = new HashMap<>();
            if (nrCardsJson != null) {
              for (String key : nrCardsJson.keySet()) {
                nrCardsMap.put(
                    key, (int) nrCardsJson.get(key).isNumber().doubleValue());
              }
            }
            cardsDeck.setNrCardsPerPlayer(nrCardsMap);

            // now fetch the player's own hand
            apiClient.getPlayerCards(
                Cookie.getSessionID(),
                Cookie.getPlayerId(),
                new ApiClient.ApiCallback<JsArray<CardDTO>>() {
                  @Override
                  public void onSuccess(JsArray<CardDTO> cards) {
                    ArrayList<CardClient> clientCards = new ArrayList<>();
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

          @Override
          public void onFailure(Throwable caught) {
            StepsAnimation.resetStepsAnimation();
            requestInProgress = false;
          }
        });
  }

  private boolean currentPlayerIsPlaying(GameStateClient result) {
    return result.getCurrentPlayerId().equals(Cookie.getPlayerId());
  }
}