package adg.keezen;

import com.adg.openapi.model.MoveResponse;
import java.util.UUID;

public class GameSession {

  private static final int DEFAULT_MAX_PLAYERS = 8;

  private final GameState gameState;
  private final CardsDeckInterface cardsDeck;
  private final String sessionId;
  private final String roomName;
  private final int maxPlayers;
  private MoveResponse lastMoveResponse;

  /** Primary constructor: every other one delegates here after picking its defaults/deck. */
  private GameSession(
      String sessionId, String roomName, int maxPlayers, CardsDeckInterface cardsDeck) {
    this.sessionId = sessionId;
    this.roomName = roomName;
    this.maxPlayers = maxPlayers;
    this.cardsDeck = cardsDeck;
    this.gameState = new GameState(cardsDeck);
    this.cardsDeck.setGameState(this.gameState);
  }

  public GameSession(String sessionId, String roomName, Integer maxPlayers) {
    this(sessionId, roomName, maxPlayers, new CardsDeck());
  }

  public GameSession() {
    this(UUID.randomUUID().toString(), "", DEFAULT_MAX_PLAYERS, new CardsDeck());
  }

  /** For testing: a real deck under a caller-chosen session id. */
  public GameSession(String sessionId) {
    this(sessionId, "", DEFAULT_MAX_PLAYERS, new CardsDeck());
  }

  public GameSession(CardsDeckInterface cardsDeck) {
    this(UUID.randomUUID().toString(), "", DEFAULT_MAX_PLAYERS, cardsDeck);
  }

  public GameSession(CardsDeckInterface cardsDeck, int animationSpeed) {
    this(UUID.randomUUID().toString(), "", DEFAULT_MAX_PLAYERS, cardsDeck);
    this.gameState.setAnimationSpeed(animationSpeed);
  }

  public GameState getGameState() {
    return gameState;
  }

  public CardsDeckInterface getCardsDeck() {
    return cardsDeck;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getRoomName() {
    return roomName;
  }

  public int getMaxPlayers() {
    return maxPlayers;
  }

  public MoveResponse getLastMoveResponse() {
    return lastMoveResponse;
  }

  public void setLastMoveResponse(MoveResponse lastMoveResponse) {
    this.lastMoveResponse = lastMoveResponse;
  }

  public void reset() {
    this.gameState.reset();
    this.lastMoveResponse = null;
  }
}
