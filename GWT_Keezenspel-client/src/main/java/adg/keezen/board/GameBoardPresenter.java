package adg.keezen.board;

import static adg.keezen.viewhelpers.ViewDrawing.clearPawnHighlightsExceptPawn1;
import static adg.keezen.viewhelpers.ViewDrawing.updatePlayerProfileUI;
import static java.lang.String.valueOf;

import adg.keezen.CardsDeck;
import adg.keezen.PawnAndCardSelection;
import adg.keezen.PlayerList;
import adg.keezen.TileId;
import adg.keezen.animations.*;
import adg.keezen.animations.AnimationSpeed;
import adg.keezen.animations.PawnAnimation;
import adg.keezen.animations.StepsAnimation;
import adg.keezen.audio.AudioPlayer;
import adg.keezen.dto.CardClient;
import adg.keezen.dto.CardDTO;
import adg.keezen.dto.GameStateClient;
import adg.keezen.dto.GameStateDTO;
import adg.keezen.dto.MoveResponseDTO;
import adg.keezen.dto.PlayerClient;
import adg.keezen.dto.TestMoveResponseDTO;
import adg.keezen.i18n.AppConstants;
import adg.keezen.services.ApiClient;
import adg.keezen.services.ApiClient.ApiCallback;
import adg.keezen.services.PollingService;
import adg.keezen.util.ChatCipher;
import adg.keezen.util.Cookie;
import adg.keezen.util.MoveRequestJsonBuilder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class GameBoardPresenter {

  private static final AppConstants CONSTANTS = GWT.create(AppConstants.class);

  private boolean requestInProgress = false;
  private final GameBoardView view;
  private final PollingService pollingService;
  private final PawnAndCardSelection pawnAndCardSelection;
  private final PlayerList playerList = new PlayerList();
  private final CardsDeck cardsDeck = new CardsDeck();
  private final ApiClient apiClient = new ApiClient();
  private long gameStateVersion = 0;
  private int chatMessageCount = 0;
  private boolean chatOffline = false;
  private String myPlayerName = "";
  private String lastCurrentPlayerId = null;
  private int lastMedalCount = 0;
  private final int BOARD_SIZE = 600; // todo: replace with CSS properties
  private final Queue<MoveResponseDTO> pendingAnimations = new LinkedList<>();

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
                sendChatMessage();
              }
            },
            ClickEvent.getType());

    view.chatInputField.addDomHandler(
        event -> {
          if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            sendChatMessage();
          }
        },
        KeyDownEvent.getType());

    view.getSendButton()
        .addDomHandler(
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                if (PawnAnimation.isAnimating()) {
                  return;
                }
                AudioPlayer.play(AudioPlayer.BUTTON_CLICK);
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
                        GWT.log("make move successful");
                        pawnAndCardSelection.resetSuccesfulMove();
                        StepsAnimation.resetStepsAnimation();
                        String pawn1Color = pawnAndCardSelection.getPawn1() != null ? pawnAndCardSelection.getPawn1().getUri() : null;
                        clearPawnHighlightsExceptPawn1(pawnAndCardSelection.getPawnId1(), pawn1Color);
                        view.setSendButtonAnimating(true);
                        animatePawnsWithAudio(result);
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
                AudioPlayer.play(AudioPlayer.BUTTON_CLICK);
                pawnAndCardSelection.reset();
                pendingAnimations.clear();
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

    view.getLeaveGameButton()
        .addDomHandler(
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                if (com.google.gwt.user.client.Window.confirm(
                    CONSTANTS.confirmLeaveGame())) {
                  apiClient.leaveGame(
                      Cookie.getSessionID(),
                      Cookie.getPlayerId(),
                      new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {}

                        @Override
                        public void onFailure(Throwable caught) {}
                      });
                }
              }
            },
            ClickEvent.getType());

    view.stepsPawn1.addChangeHandler(
        event -> {
          pawnAndCardSelection.setNrStepsPawn1ForSplit(view.stepsPawn1.getValue());
          view.stepsPawn1.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn1()));
          view.stepsPawn2.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn2()));
          checkMove();
        });

    view.stepsPawn2.addChangeHandler(
        event -> {
          pawnAndCardSelection.setNrStepsPawn2ForSplit(view.stepsPawn2.getValue());
          view.stepsPawn1.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn1()));
          view.stepsPawn2.setValue(valueOf(pawnAndCardSelection.getNrStepsPawn2()));
          checkMove();
        });
  }

  private void sendChatMessage() {
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

  private void checkMove() {
    if (pawnAndCardSelection.getCard() == null) {
      return;
    }
    MoveRequestJsonBuilder builder =
        new MoveRequestJsonBuilder()
            .withPlayerId(Cookie.getPlayerId())
            .withCardId(pawnAndCardSelection.getCard())
            .withPawn1(pawnAndCardSelection.getPawn1())
            .withPawn2(pawnAndCardSelection.getPawn2())
            .withStepsPawn1(pawnAndCardSelection.getNrStepsPawn1())
            .withStepsPawn2(pawnAndCardSelection.getNrStepsPawn2())
            .withTempMessageType("CHECK_MOVE");

    apiClient.checkMove(
        Cookie.getSessionID(),
        Cookie.getPlayerId(),
        builder.build(),
        new ApiCallback<TestMoveResponseDTO>() {
          @Override
          public void onSuccess(TestMoveResponseDTO result) {
            ArrayList<TileId> tiles = new ArrayList<>();
            for (int i = 0; i < result.getTiles().length(); i++) {
              tiles.add(
                  new TileId(
                      result.getTiles().get(i).getPlayerId(),
                      result.getTiles().get(i).getTileNr()));
            }
            StepsAnimation.updateStepsAnimation(tiles);
          }

          @Override
          public void onHttpError(int statusCode, String statusText) {}

          @Override
          public void onFailure(Throwable caught) {}
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

  private void animatePawnsWithAudio(MoveResponseDTO moveResponse) {
    if (PawnAnimation.isAnimating()) {
      pendingAnimations.add(moveResponse);
    } else {
      playAnimation(moveResponse);
    }
  }

  private void playAnimation(MoveResponseDTO moveResponse) {
    if ("onBoard".equals(moveResponse.getMoveType())) {
      AudioPlayer.play(AudioPlayer.PAWN_ON_BOARD, 0.1);
    }
    view.animatePawns(moveResponse, () -> {
      if (!pendingAnimations.isEmpty()) {
        playAnimation(pendingAnimations.poll());
      } else {
        view.setSendButtonAnimating(false);
      }
    });
  }

  private void updateGameState(GameStateClient gameState) {
    GWT.log("Game State initialize board: ");
    if (!Board.isInitialized()) {
      GWT.log("Game State board not yet initialized.");
      initializeBoardState(gameState);
      lastCurrentPlayerId = gameState.getCurrentPlayerId();
      lastMedalCount = (int) gameState.getPlayers().stream()
          .filter(p -> p.getPlace() > -1)
          .count();
      AnimationSpeed.setSpeed(1);
      view.createPlayerList(gameState.getPlayers());
      for (PlayerClient p : gameState.getPlayers()) {
        if (p.getId().equals(Cookie.getPlayerId())) {
          myPlayerName = p.getName();
          break;
        }
      }
    } else {
      String currentPlayerId = gameState.getCurrentPlayerId();
      if (!currentPlayerId.equals(lastCurrentPlayerId)) {
        AudioPlayer.play(AudioPlayer.TURN_CHANGE);
        lastCurrentPlayerId = currentPlayerId;
      }
      int medalCount = (int) gameState.getPlayers().stream()
          .filter(p -> p.getPlace() > -1)
          .count();
      if (medalCount > lastMedalCount) {
        AudioPlayer.play(AudioPlayer.MEDAL_AWARDED);
        lastMedalCount = medalCount;
      }
      if (gameState.getLastMoveResponse() != null) {
        pawnAndCardSelection.resetSuccesfulMove();
        StepsAnimation.resetStepsAnimation();
        String pawn1Color = pawnAndCardSelection.getPawn1() != null ? pawnAndCardSelection.getPawn1().getUri() : null;
        clearPawnHighlightsExceptPawn1(pawnAndCardSelection.getPawnId1(), pawn1Color);
      }
      if (gameState.getLastMoveResponse() != null) {
        MoveResponseDTO response = gameState.getLastMoveResponse();
        String myId = Cookie.getPlayerId();
        boolean isOwnMove = (response.getPawn1() != null && myId.equals(response.getPawn1().getPlayerId()))
            || (response.getPawn2() != null && myId.equals(response.getPawn2().getPlayerId()));
        if (!isOwnMove) {
          animatePawnsWithAudio(response);
        }
        // own moves are already animated via makeMove.onSuccess
      } else if (!PawnAnimation.isAnimating()) {
        view.animatePawnsToPositions(gameState.getPawns());
      }
    }
    updatePlayerProfileUI(gameState.getPlayers());

    GWT.log("set pawns: ");
    Board.setPawns(gameState.getPawns());
    GWT.log("update pawns");
    pawnAndCardSelection.updatePawns(gameState.getPawns());

    view.enableButtons(currentPlayerIsPlaying(gameState));

    if (pawnAndCardSelection.getCard() != null) {
      checkMove();
    }
  }

  private void pollServerForChat() {
    apiClient.getChatMessages(Cookie.getSessionID(), new ApiCallback<JSONArray>() {
      @Override
      public void onSuccess(JSONArray messages) {
        if (chatOffline) {
          chatOffline = false;
          view.setChatInputRowVisible(true);
        }
        if (messages.size() == chatMessageCount) return;
        chatMessageCount = messages.size();
        String key = Cookie.getSessionID();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
          JSONObject m = messages.get(i).isObject();
          if (m == null) continue;
          String timestamp = convertUTCToLocal(m.get("timestampUTC").isString().stringValue());
          String sender    = m.get("sender").isString().stringValue();
          String encrypted = m.get("message").isString().stringValue();
          sb.append(timestamp).append(" ").append(sender).append(": ")
            .append(ChatCipher.decrypt(encrypted, key)).append("\n");
        }
        view.refreshChat(sb.toString());
      }
      @Override public void onFailure(Throwable caught) {
        if (!chatOffline) {
          chatOffline = true;
          view.setChatInputRowVisible(false);
        }
      }
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
    view.createPawns(result.getPawns(), pawnAndCardSelection, this::checkMove);
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

  /** Converts a UTC "HH:mm" string to the browser's local timezone. */
  private static String convertUTCToLocal(String utcTime) {
    String[] parts = utcTime.split(":");
    int utcHours   = Integer.parseInt(parts[0]);
    int utcMinutes = Integer.parseInt(parts[1]);
    // getTimezoneOffset() returns (UTC − local) in minutes
    int total = utcHours * 60 + utcMinutes - getTimezoneOffset();
    total = ((total % 1440) + 1440) % 1440;
    int h = total / 60, m = total % 60;
    return (h < 10 ? "0" : "") + h + ":" + (m < 10 ? "0" : "") + m;
  }

  private static native int getTimezoneOffset() /*-{
    return new Date().getTimezoneOffset();
  }-*/;
}