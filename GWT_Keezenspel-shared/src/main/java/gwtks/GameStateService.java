package gwtks;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.ArrayList;

@RemoteServiceRelativePath("gamestate")
public interface GameStateService extends RemoteService {
    GameStateResponse getGameState() throws IllegalArgumentException;
    ArrayList<Player> getPlayers();
    Player addPlayer(Player player);
}
