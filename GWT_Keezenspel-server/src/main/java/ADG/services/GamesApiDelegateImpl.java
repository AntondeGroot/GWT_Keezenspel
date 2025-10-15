package ADG.services;

import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.dto.GameCreatedResponse;
import com.adg.openapi.api.GamesApi;
import com.adg.openapi.api.GamesApiDelegate;
import com.adg.openapi.model.GameInfo;
import com.adg.openapi.model.NewGameRequest;
import com.adg.openapi.model.Player;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

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
  public ResponseEntity<List<Player>> gamesSessionIdPlayersGet(String sessionId) {

    if(!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    //todo: rework this to the new openapi.model.player
    ArrayList<ADG.Games.Keezen.Player.Player> players = gameSession.getGameState().getPlayers();
    ArrayList<Player> playersResponse = new ArrayList<>();
    for (ADG.Games.Keezen.Player.Player player : players) {
      playersResponse.add(
          new Player()
              .id(player.getUUID())
              .name(player.getName())
              .place(player.getPlace())
              .isPlaying(player.isPlaying())
              .isActive(player.isActive()));
    }

    return new ResponseEntity<>(playersResponse,HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> gamesSessionIdPlayersPost(String sessionId,
      Player player) {

    if(!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    //todo replace with openapi player-model
    ADG.Games.Keezen.Player.Player p = new  ADG.Games.Keezen.Player.Player(player.getName(), player.getId());
    gameSession.getGameState().addPlayer(p);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Void> gamesSessionIdPost(
      @Parameter(name = "sessionId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("sessionId") String sessionId
  ) {
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

  /**
   * DELETE /games/{sessionId}/ : Stop a specific game
   *
   * @param sessionId  (required)
   * @return Game was stopped (status code 204)
   *         or Game could not be found in order to stop it (status code 404)
   * @see GamesApi#gamesSessionIdDelete
   */
  @Override
  public ResponseEntity<Void> gamesSessionIdDelete(String sessionId) {
    if(!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)){
      return ResponseEntity.status(404).build();
    }
    gameSession.getGameState().stop();
    GameRegistry.removeGame(sessionId);
    return ResponseEntity.status(204).build();
  }
}
