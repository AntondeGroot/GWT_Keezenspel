package ADG.Games.Keezen;

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
        games.put(sessionID, new GameSession());
        return sessionID;
      }

    public static String createTestGame(String sessionID) {
        games.put(sessionID, new GameSession(new CardsDeckMock()));
        return sessionID;
    }

      // Get an existing game by ID
      public static GameSession getGame(String sessionId) {
        GameSession session = games.get(sessionId);
        if (session == null) {
          throw new IllegalArgumentException("No game found with ID: " + sessionId);
        }
        return session;
      }

    //
    //  // Optional: Remove a game (cleanup)
    //  public static void removeGame(String sessionID) {
    //    games.remove(sessionID);
    //  }
    //
    //  // Optional: List all session IDs (useful for admin/debugging)
    //  public static Map<String, GameSession> getAllGames() {
    //    return games;
    //  }
    //
}