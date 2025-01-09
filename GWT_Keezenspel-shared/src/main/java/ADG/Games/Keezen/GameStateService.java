package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.ArrayList;

@RemoteServiceRelativePath("gamestate")
public interface GameStateService extends RemoteService {
    GameStateResponse getGameState();
    ArrayList<Player> getPlayers();
    Player addPlayer(Player player);
    void startGame() throws IllegalStateException;
}
