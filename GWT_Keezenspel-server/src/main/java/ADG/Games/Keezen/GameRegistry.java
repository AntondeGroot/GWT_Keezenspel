package ADG.Games.Keezen;

import static com.adg.openapi.model.GameInfo.StatusEnum.IN_PROGRESS;
import static com.adg.openapi.model.GameInfo.StatusEnum.WAITING;

import com.adg.openapi.model.GameInfo;
import com.adg.openapi.model.GameInfo.StatusEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameRegistry {

  // All running games (key = gameId)
  private static final Map<String, GameSession> games = new ConcurrentHashMap<>();

  // Create a new game, returns the new game ID
  public static String createNewGame() {
    String sessionId = java.util.UUID.randomUUID().toString();
    games.put(sessionId, new GameSession());
    return sessionId;
  }

  /***
   * For testing purposes
   * @param sessionID
   * @return
   */
  public static String createNewGame(String sessionID) {
    games.put(sessionID, new GameSession(sessionID));
    return sessionID;
  }

  /***
   * For testing purposes
   * @param sessionID
   * @return
   */
  public static String createNewGame(String sessionID, String roomName, Integer maxPlayers) {
    games.put(sessionID, new GameSession(sessionID, roomName, maxPlayers));
    return sessionID;
  }

  public static String createTestGame(String sessionID) {
    int animationSpeed = 100;
    GameSession session = new GameSession(new CardsDeckMock(), animationSpeed);
    games.put(sessionID, session);
    return sessionID;
  }

  // Get an existing game by ID
  public static GameSession getGame(String sessionId) {
    GameSession session = games.get(sessionId);
    if (session == null) {
      return null;
    }
    return session;
  }

  public static List<GameInfo> getAllGames(){
    ArrayList<GameInfo> gameInfos = new ArrayList<>();
    for(GameSession session : games.values()){
      GameState gameState = session.getGameState();
      GameInfo gameInfo = new GameInfo(
          session.getSessionId(),
          session.getRoomName(),
          gameState.getNrPlayers(),
          session.getMaxPlayers(),
          gameState.hasStarted() ? IN_PROGRESS : WAITING
      );
      gameInfos.add(gameInfo);
    }
    return gameInfos;
  }

  public static void removeGame(String sessionID) {
    games.remove(sessionID);
  }
  //
  //  // Optional: List all session IDs (useful for admin/debugging)
  //  public static Map<String, GameSession> getAllGames() {
  //    return games;
  //  }
  //
}