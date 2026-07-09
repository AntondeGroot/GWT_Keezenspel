package adg.services;

import java.util.UUID;
import adg.keezen.GameRegistry;
import adg.keezen.GameSession;
import adg.keezen.GameState;
import adg.keezen.KeezenGameOptions;
import com.adg.openapi.api.GamesApiDelegate;
import com.adg.openapi.model.GameCreatedResponse;
import com.adg.openapi.model.GameInfo;
import com.adg.openapi.model.NewGameRequest;
import com.adg.openapi.model.Player;
import com.adg.openapi.model.PlayerAddedResponse;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GamesApiDelegateImpl implements GamesApiDelegate {

  private static final Logger log = LoggerFactory.getLogger(GamesApiDelegateImpl.class);

  @Autowired
  private SseEmitterService sseEmitterService;

  @Override
  public ResponseEntity<List<GameInfo>> getAllGames() {
    return new ResponseEntity<>(GameRegistry.getAllGames(), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<GameCreatedResponse> createNewGame(NewGameRequest newGameRequest) {
    String roomName = newGameRequest.getRoomName();

    if (roomName == null || roomName.isBlank() || roomName.trim().length() < 3) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    String sessionID = UUID.randomUUID().toString();

    if (GameRegistry.getGame(sessionID) != null) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    Integer maxPlayers = newGameRequest.getMaxPlayers();
    GameRegistry.createNewGame(sessionID, roomName, maxPlayers);
    KeezenGameOptions.apply(
        GameRegistry.getGameOrThrow(sessionID).getGameState(), newGameRequest.getGameOptions());

    log.info("Game created: sessionId={} room='{}' maxPlayers={}", sessionID, roomName, maxPlayers);
    GameCreatedResponse response = new GameCreatedResponse(sessionID);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<Player>> getAllPlayersInGame(String sessionId) {

    GameSession gameSession = GameRegistry.getGame(sessionId);
    if (gameSession == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    ArrayList<Player> players = gameSession.getGameState().getPlayers();

    return new ResponseEntity<>(players, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<PlayerAddedResponse> addPlayerToGame(String sessionId, Player player) {

    GameSession gameSession = GameRegistry.getGame(sessionId);
    if (gameSession == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    GameState gameState = gameSession.getGameState();
    if (gameState.getNrPlayers() >= gameSession.getMaxPlayers()) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    if (gameState.hasStarted()) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    gameSession.getGameState().addPlayer(player);
    log.info("Player joined: sessionId={} playerId={} name='{}'", sessionId, player.getId(), player.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(new PlayerAddedResponse(player.getId()));
  }

  @Override
  public ResponseEntity<Void> startGame(String sessionId) {
    GameSession gameSession = GameRegistry.getGame(sessionId);
    if (gameSession == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    GameState gameState = gameSession.getGameState();
    if (gameState.hasStarted()) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    gameState.start();
    log.info("Game started: sessionId={} players={}", sessionId, gameState.getNrPlayers());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> leaveGame(String sessionId, String playerId) {
    GameSession gameSession = GameRegistry.getGame(sessionId);
    if (gameSession == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    GameState gameState = gameSession.getGameState();
    gameState.processLeaveGame(playerId);
    gameSession.setLastMoveResponse(null);
    if (gameState.allPlayersHaveLeft()) {
      GameRegistry.removeGame(sessionId);
      log.info("Game removed (all players left): sessionId={}", sessionId);
    } else {
      sseEmitterService.push(sessionId, gameSession);
      log.info("Player left: sessionId={} playerId={}", sessionId, playerId);
    }
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Override
  public ResponseEntity<Void> stopGame(String sessionId) {
    GameSession gameSession = GameRegistry.getGame(sessionId);
    if (gameSession == null) {
      return ResponseEntity.status(404).build();
    }
    gameSession.getGameState().stop();
    GameRegistry.removeGame(sessionId);
    return ResponseEntity.status(204).build();
  }

  @Override
  public ResponseEntity<GameInfo> getGame(String sessionId) {
    GameSession gameSession = GameRegistry.getGame(sessionId);
    if (gameSession == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    GameState gameState = gameSession.getGameState();
    GameInfo gameInfo =
        new GameInfo(
            gameSession.getSessionId(),
            gameSession.getRoomName(),
            gameState.getNrPlayers(),
            gameSession.getMaxPlayers(),
            gameState.hasStarted() ? GameInfo.StatusEnum.IN_PROGRESS : GameInfo.StatusEnum.WAITING);
    return ResponseEntity.ok(gameInfo);
  }
}
