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
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
public class GamesApiDelegateImpl implements GamesApiDelegate {

  @Override
  public ResponseEntity<List<GameInfo>> getAllGames() {
    return new ResponseEntity<>(GameRegistry.getAllGames(), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Object> createNewGame(NewGameRequest newGameRequest) {
    String roomName = newGameRequest.getRoomName();

    if(roomName == null || roomName.isEmpty()){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    //todo: generate sessionId based on roomName
    String sessionID = roomName;

    if(GameRegistry.getGame(sessionID) != null){
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    Integer maxPlayers = newGameRequest.getMaxPlayers();
    GameRegistry.createNewGame(sessionID, roomName, maxPlayers);

    GameCreatedResponse response = new GameCreatedResponse(sessionID);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<Player>> getAllPlayersInGame(String sessionId) {

    if(!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    ArrayList<Player> players = gameSession.getGameState().getPlayers();

    return new ResponseEntity<>(players,HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> addPlayerToGame(String sessionId,
      Player player) {

    if(!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    gameSession.getGameState().addPlayer(player);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Void> startGame(String sessionId) {
    if(!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)){
      return new ResponseEntity<>(HttpStatus.NOT_FOUND) ;
    }

    GameState gameState = gameSession.getGameState();
    if(gameState.hasStarted()){
      return new ResponseEntity<>(HttpStatus.CONFLICT) ;
    }

    gameState.start();
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> stopGame(String sessionId) {
    if(!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)){
      return ResponseEntity.status(404).build();
    }
    gameSession.getGameState().stop();
    GameRegistry.removeGame(sessionId);
    return ResponseEntity.status(204).build();
  }
}
