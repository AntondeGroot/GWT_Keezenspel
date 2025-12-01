package ADG.services;

import ADG.Games.Keezen.Player.Player;
import ADG.Games.Keezen.State.GameStateResponse;
import ADG.Games.Keezen.State.GameStateService;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;
import java.util.ArrayList;

@SuppressWarnings("serial")
@WebServlet("/app/gamestate")
public class GameStateServiceImpl extends RemoteServiceServlet implements GameStateService {

  @Override
  public GameStateResponse getGameState(String sessionID) throws IllegalArgumentException {
    //        if (sessionID == null) {
    //            return new GameStateResponse();
    //        }
    GameStateResponse response = new GameStateResponse();
    //        GameSession session = GameRegistry.getGame(sessionID);
    //        if (session == null) {
    //            return new GameStateResponse();
    //        }
    //        GameState gameState = session.getGameState();
    //
    //        response.setPlayerColors(gameState.getPlayerColors());
    //        response.setPlayers(gameState.getPlayers());
    //        response.setNrPlayers(gameState.getNrPlayers());
    //        response.setPawns(gameState.getPawns());
    //        response.setPlayerIdTurn(gameState.getPlayerIdTurn());
    //        response.setActivePlayers(gameState.getActivePlayers());
    //        response.setWinners(gameState.getWinners());
    //        response.setAnimationSpeed(gameState.getAnimationSpeed());
    return response;
  }

  @Override
  public ArrayList<Player> getPlayers(String sessionID) {
    //        GameSession session = GameRegistry.getGame(sessionID);
    //        GameState gameState = session.getGameState();
    //        return gameState.getPlayers();
    return new ArrayList<>();
  }

  public synchronized Player addPlayer(String sessionID, Player player) {
    //        GameSession session = GameRegistry.getGame(sessionID);
    //        GameState gameState = session.getGameState();
    //        gameState.addPlayer(player);
    return player;
  }

  @Override
  public void startGame(String sessionID) throws IllegalStateException {
    //        //todo: remove testdata
    //        int NrPlayers = 3;
    //        GameSession session = GameRegistry.getGame(sessionID);
    //        GameState gameState = session.getGameState();
    //
    //        if(gameState.getPawns().isEmpty()){
    //            for (int i = 0; i < NrPlayers; i++) {
    //                Player player = new Player("player"+i,String.valueOf(i));
    //                if(i==0){
    //                    player.setIsPlaying(true);
    //                }
    //                gameState.addPlayer(player);
    //            }
    //        }
    //        //todo: remove testdata
    //        //todo: replace with isRunning method
    //        if(gameState.getPawns().isEmpty()){
    //            gameState.start();
    //        }
  }
}
