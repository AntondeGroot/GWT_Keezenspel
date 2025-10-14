package ADG.services;

import ADG.Games.Keezen.GameRegistry;
import ADG.dto.GameCreatedResponse;
import com.adg.openapi.api.GamesApiDelegate;
import com.adg.openapi.model.NewGameRequest;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
public class GamesApiDelegateImpl implements GamesApiDelegate {

  @Override
  public ResponseEntity<Object> gamesPost(NewGameRequest newGameRequest) {
    if(newGameRequest.getRoomName() == null || newGameRequest.getRoomName().isEmpty()){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    //todo: generate sessionId based on roomname
    String sessionID = newGameRequest.getRoomName();

    if(GameRegistry.getGame(sessionID) != null){
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    GameRegistry.createNewGame(sessionID);
    GameCreatedResponse response = new GameCreatedResponse(sessionID);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }
}
