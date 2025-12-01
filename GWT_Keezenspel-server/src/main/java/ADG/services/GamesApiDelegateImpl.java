package ADG.services;

import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.dto.GameCreatedResponse;
import com.adg.openapi.api.GamesApiDelegate;
import com.adg.openapi.model.GameInfo;
import com.adg.openapi.model.NewGameRequest;
import com.adg.openapi.model.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GamesApiDelegateImpl implements GamesApiDelegate {

  @Override
  public ResponseEntity<List<GameInfo>> getAllGames() {
    return new ResponseEntity<>(GameRegistry.getAllGames(), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Object> createNewGame(NewGameRequest newGameRequest) {
    String roomName = newGameRequest.getRoomName();

    if (roomName == null
        || roomName.isEmpty()
        || roomName.isBlank()
        || roomName.replace(" ", "").length() < 3) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    String sessionID = UUID.randomUUID().toString();

    if (GameRegistry.getGame(sessionID) != null) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    Integer maxPlayers = newGameRequest.getMaxPlayers();
    GameRegistry.createNewGame(sessionID, roomName, maxPlayers);

    GameCreatedResponse response = new GameCreatedResponse(sessionID);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<Player>> getAllPlayersInGame(String sessionId) {

    if (!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    ArrayList<Player> players = gameSession.getGameState().getPlayers();

    return new ResponseEntity<>(players, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Object> addPlayerToGame(String sessionId, Player player) {

    if (!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    GameState gameState = gameSession.getGameState();
    if (gameState.getNrPlayers() >= gameSession.getMaxPlayers()) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    if (gameState.hasStarted()) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    String playerId = UUID.randomUUID().toString();
    player.setId(playerId);

    gameSession.getGameState().addPlayer(player);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("playerId", playerId));
  }

  @Override
  public ResponseEntity<Void> startGame(String sessionId) {
    if (!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    GameState gameState = gameSession.getGameState();
    if (gameState.hasStarted()) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    gameState.start();
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> stopGame(String sessionId) {
    if (!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)) {
      return ResponseEntity.status(404).build();
    }
    gameSession.getGameState().stop();
    GameRegistry.removeGame(sessionId);
    return ResponseEntity.status(204).build();
  }
}
