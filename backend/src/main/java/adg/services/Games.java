package adg.services;

import adg.keezen.GameRegistry;
import adg.keezen.GameSession;

/** Web-layer session lookups that turn a missing game into an HTTP 404. */
final class Games {

  private Games() {}

  /** The session for this id, or throws {@link GameNotFoundException} (→ 404) when none exists. */
  static GameSession require(String sessionId) {
    GameSession session = GameRegistry.getGame(sessionId);
    if (session == null) {
      throw new GameNotFoundException(sessionId);
    }
    return session;
  }
}
