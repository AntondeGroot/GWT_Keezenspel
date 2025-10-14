package ADG.services;

import ADG.Games.Keezen.GameRegistry;
import ADG.dto.GameCreatedResponse;
import com.adg.openapi.api.GamesApiDelegate;
import com.adg.openapi.model.GameInfo;
import com.adg.openapi.model.NewGameRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
public class GamesApiDelegateImpl implements GamesApiDelegate {

  @Override
  public ResponseEntity<List<GameInfo>> gamesGet() {
    List<GameInfo> gameInfos = GameRegistry.getAllGames();

    return new ResponseEntity<>(gameInfos, HttpStatus.OK);

  }

  @Override
  public ResponseEntity<Object> gamesPost(NewGameRequest newGameRequest) {
    String roomName = newGameRequest.getRoomName();

    if(roomName == null || roomName.isEmpty()){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    //todo: generate sessionId based on roomname
    String sessionID = roomName;

    if(GameRegistry.getGame(sessionID) != null){
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    Integer maxPlayers = newGameRequest.getMaxPlayers();
    GameRegistry.createNewGame(sessionID, roomName, maxPlayers);

    GameCreatedResponse response = new GameCreatedResponse(sessionID);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }
}
