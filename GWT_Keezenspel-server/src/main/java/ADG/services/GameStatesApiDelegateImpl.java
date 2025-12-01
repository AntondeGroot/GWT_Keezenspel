package ADG.services;

import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import com.adg.openapi.api.GamestatesApiDelegate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GameStatesApiDelegateImpl implements GamestatesApiDelegate {

  @Override
  public ResponseEntity<com.adg.openapi.model.GameState> getGameStateForGame(
      String sessionId, Long stateVersion) {

    GameSession session = GameRegistry.getGame(sessionId);
    if (session == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    GameState gameState = session.getGameState();
    if (stateVersion != null && stateVersion.equals(Long.valueOf(gameState.getVersion()))) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }

    com.adg.openapi.model.GameState gameStateResponse = new com.adg.openapi.model.GameState();
    gameStateResponse.setCurrentPlayerId(gameState.getPlayerIdTurn());
    gameStateResponse.setPawns(gameState.getPawns());
    gameStateResponse.setPlayers(gameState.getPlayers());
    gameStateResponse.setWinners(gameState.getWinners());
    gameStateResponse.setVersion(gameState.getVersion());

    return ResponseEntity.ok(gameStateResponse);
  }
}
