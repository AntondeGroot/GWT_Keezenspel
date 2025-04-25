package ADG.Games.Keezen.State;

import ADG.Games.Keezen.Player.Player;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.ArrayList;

@RemoteServiceRelativePath("gamestate")
public interface GameStateService extends RemoteService {
    GameStateResponse getGameState(String sessionID);
    ArrayList<Player> getPlayers(String sessionID);
    Player addPlayer(String sessionID, Player player);
    void startGame(String sessionID) throws IllegalStateException;
}
