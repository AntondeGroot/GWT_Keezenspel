package adg.keezen.board;

import static adg.keezen.viewhelpers.ViewDrawing.clearPawnHighlightsExceptPawn1;
import static adg.keezen.viewhelpers.ViewDrawing.updatePlayerProfileUI;
import static com.google.gwt.user.client.Window.Location.getHostName;
import static com.google.gwt.user.client.Window.Location.getProtocol;
import static com.google.gwt.user.client.Window.Location.replace;
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
import adg.keezen.services.SseService;
import adg.keezen.util.ChatCipher;
import adg.keezen.util.Cookie;
import adg.keezen.util.MoveRequestJsonBuilder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class GameBoardPresenter {

  private static final AppConstants CONSTANTS = GWT.create(AppConstants.class);

  private final GameBoardView view;
  private final PawnAndCardSelection pawnAndCardSelection;
  private final PlayerList playerList = new PlayerList();
  private final CardsDeck cardsDeck = new CardsDeck();
  private final ApiClient apiClient = new ApiClient();
  private final SseService gameStateSse = new SseService();
  private final SseService chatSse = new SseService();
  private long gameStateVersion = 0;
  private int chatMessageCount = 0;
  private boolean chatOffline = false;
  private String myPlayerName = "";
  private String lastCurrentPlayerId = null;
  private int lastMedalCount = 0;
  private static final int BOARD_SIZE = 600; // todo: replace with CSS properties
  private final Queue<MoveResponseDTO> pendingAnimations = new LinkedList<>();
  private Timer mustPlayForfeitTimer;
  private static final int MUST_PLAY_TIMEOUT_MS = 3 * 60 * 1000;
  /** Previous card counts per player — used to detect when a card was played. */
  private final HashMap<String, Integer> prevNrCards = new HashMap<>();

  /** Card snapshot captured at click time — used to animate on confirmed success only. */
  private adg.keezen.dto.CardClient snapshotCard = null;
  private int snapshotPileSize = 0;

  public GameBoardPresenter(GameBoardView gameBoardView) {
    this.view = gameBoardView;
    pawnAndCardSelection = new PawnAndCardSelection();
  }

  public void start() {
    bindEventHandlers();
    connectToServer();
  }

  private void connectToServer() {
    // SSE opens immediately so it is ready before the first move is made.
    connectGameStateSse();
    connectChatSse();
    // REST call bootstraps the initial render without waiting for a push.
    apiClient.getGameState(
        Cookie.getSessionID(),
        null,
        new ApiCallback<GameStateDTO>() {
          @Override
          public void onSuccess(GameStateDTO response) {
            gameStateVersion = (long) response.getVersion();
            updateGameState(new GameStateClient(response));
            // Card data arrives via SSE; no REST poll needed.
          }

          @Override
          public void onHttpError(int statusCode, String statusText) {
            GWT.log("Initial game state HTTP error " + statusCode);
          }

          @Override
          public void onFailure(Throwable caught) {
            GWT.log("Initial game state error: " + caught.getMessage());
          }
        });
  }

  private void connectGameStateSse() {
    String url = com.google.gwt.core.client.GWT.getHostPageBaseURL().replaceAll("/$", "")
        + "/sse/gamestates/" + Cookie.getSessionID() + "/" + Cookie.getPlayerId();
    gameStateSse.connect(url, "gamestate", new SseService.Callback() {
      @Override
      public void onMessage(String data) {
        GameStateDTO response = com.google.gwt.core.client.JsonUtils.safeEval(data);
        GWT.log("SSE game state: " + response.getVersion());
        gameStateVersion = (long) response.getVersion();
        updateGameState(new GameStateClient(response));
        updateCardsFromPush(response);
      }

      @Override
      public void onError() {
        GWT.log("SSE game state connection error");
      }
    });
  }

  private void updateCardsFromPush(GameStateDTO push) {
    pawnAndCardSelection.setPlayerId(Cookie.getPlayerId());

    // Played cards pile
    com.google.gwt.core.client.JsArrayString playedCardsJs = push.getPlayedCards();
    if (playedCardsJs != null) {
      ArrayList<CardClient> playedCards = new ArrayList<>();
      for (int i = 0; i < playedCardsJs.length(); i++) {
        String[] parts = playedCardsJs.get(i).split("_");
        playedCards.add(new CardClient(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
      }
      cardsDeck.setPlayedCards(playedCards);
    }

    // Card counts per player
    com.google.gwt.core.client.JavaScriptObject nrCardsJs = push.getNrOfCardsPerPlayer();
    if (nrCardsJs != null) {
      JSONObject nrCardsJson = new JSONObject(nrCardsJs);
      HashMap<String, Integer> nrCardsMap = new HashMap<>();
      for (String key : nrCardsJson.keySet()) {
        nrCardsMap.put(key, (int) nrCardsJson.get(key).isNumber().doubleValue());
      }

      // Detect which players just played a card and tell the view so it
      // can animate the face-down card flying to the pile before redrawing.
      StringBuilder playedIds = new StringBuilder();
      for (HashMap.Entry<String, Integer> e : nrCardsMap.entrySet()) {
        String pid = e.getKey();
        int oldN = prevNrCards.containsKey(pid) ? prevNrCards.get(pid) : e.getValue();
        if (e.getValue() < oldN && !pid.equals(Cookie.getPlayerId())) {
          if (playedIds.length() > 0) playedIds.append(',');
          playedIds.append(pid);
        }
      }
      if (playedIds.length() > 0) {
        view.setPlayersWhoJustPlayed(playedIds.toString());
      }
      prevNrCards.clear();
      prevNrCards.putAll(nrCardsMap);
      cardsDeck.setNrCardsPerPlayer(nrCardsMap);
    }

    // Private hand
    com.google.gwt.core.client.JsArray<CardDTO> handJs = push.getPlayerCards();
    if (handJs != null) {
      ArrayList<CardClient> hand = new ArrayList<>();
      for (int i = 0; i < handJs.length(); i++) {
        hand.add(new CardClient(handJs.get(i)));
      }
      cardsDeck.setCards(hand);
    }

    view.drawCards(cardsDeck, pawnAndCardSelection);
    playerList.refresh();

    boolean isMyTurn = Cookie.getPlayerId().equals(push.getCurrentPlayerId());
    if (!push.isCanForfeit() && isMyTurn) {
      view.getForfeitButton().setEnabled(false);
      if (mustPlayForfeitTimer == null) {
        mustPlayForfeitTimer = new Timer() {
          @Override
          public void run() {
            view.getForfeitButton().setEnabled(true);
            mustPlayForfeitTimer = null;
          }
        };
        mustPlayForfeitTimer.schedule(MUST_PLAY_TIMEOUT_MS);
      }
    } else {
      if (mustPlayForfeitTimer != null) {
        mustPlayForfeitTimer.cancel();
        mustPlayForfeitTimer = null;
      }
    }
  }

  private void connectChatSse() {
    String url = com.google.gwt.core.client.GWT.getHostPageBaseURL().replaceAll("/$", "")
        + "/chat/" + Cookie.getSessionID() + "/stream";
    chatSse.connect(url, new SseService.Callback() {
      @Override
      public void onMessage(String data) {
        if (chatOffline) {
          chatOffline = false;
          view.setChatVisible(true);
        }
        JSONArray messages =
            com.google.gwt.json.client.JSONParser.parseStrict(data).isArray();
        if (messages == null) return;
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

      @Override
      public void onError() {
        if (!chatOffline) {
          chatOffline = true;
          view.setChatVisible(false);
        }
      }
    });
  }

  public void stop() {
    // todo: deregister all binders
    gameStateSse.disconnect();
    chatSse.disconnect();
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

                // Snapshot the card position now — the element is in the DOM at click time.
                // Animation and removal are deferred to onSuccess so they only happen
                // when the server confirms the move is valid.
                final adg.keezen.dto.CardClient cardToPlay = pawnAndCardSelection.getCard();
                if (cardToPlay != null) {
                  com.google.gwt.dom.client.Element cardEl =
                      com.google.gwt.dom.client.Document.get().getElementById(cardToPlay.toString());
                  if (cardEl != null) {
                    snapshotCard = cardToPlay;
                    snapshotPileSize = cardsDeck.getPlayedCards().size() + 1;
                    view.captureCardStartPos(cardEl);
                  }
                }

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

                apiClient.makeMove(
                    Cookie.getSessionID(),
                    Cookie.getPlayerId(),
                    builder.build(),
                    new ApiCallback<MoveResponseDTO>() {
                      @Override
                      public void onSuccess(MoveResponseDTO result) {
                        GWT.log("make move successful");
                        if (snapshotCard != null) {
                          view.animateOwnPlayedCardFromSnapshot(snapshotPileSize,
                              snapshotCard.getValue(), snapshotCard.getSuit());
                          com.google.gwt.dom.client.Element cardEl =
                              com.google.gwt.dom.client.Document.get().getElementById(snapshotCard.toString());
                          if (cardEl != null) {
                            cardEl.removeFromParent();
                          }
                          snapshotCard = null;
                        }
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
                        snapshotCard = null;
                        StepsAnimation.resetStepsAnimation();
                        redirectToLobbyOnServerError(statusCode);
                      }

                      @Override
                      public void onFailure(Throwable caught) {
                        GWT.log("make move Failure" + caught.getMessage());
                        snapshotCard = null;
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
                        public void onSuccess(Void result) {
                          String lobbyUrl = getProtocol() + "//" + getHostName();
                          replace(lobbyUrl);
                        }

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
        boolean isOwnMove = response.getPawn1() != null && myId.equals(response.getPawn1().getPlayerId());
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


  private void initializeBoardState(GameStateClient result) {
    Board board = new Board();
    GWT.log("set pawns");
    Board.setPawns(result.getPawns());
    GWT.log("create board");
    board.createBoard(result.getPlayers(), BOARD_SIZE);
    GWT.log("draw board");
    view.drawBoard(Board.getTiles(), result.getPlayers(), Board.getCellDistance());
    view.createPawns(result.getPawns(), pawnAndCardSelection, () -> {
      view.stepsPawn1.setValue(String.valueOf(pawnAndCardSelection.getNrStepsPawn1()));
      view.stepsPawn2.setValue(String.valueOf(pawnAndCardSelection.getNrStepsPawn2()));
      checkMove();
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

  private void redirectToLobbyOnServerError(int statusCode) {
    if (statusCode >= 500) {
      stop();
      replace(getProtocol() + "//" + getHostName());
    }
  }

  private static native int getTimezoneOffset() /*-{
    return new Date().getTimezoneOffset();
  }-*/;
}