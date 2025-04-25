package ADG.Games.Keezen.State;

import ADG.Games.Keezen.Player.Player;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

public interface GameStateServiceAsync {
    void getGameState(String sessionID, AsyncCallback<GameStateResponse> callback);
    void getPlayers(String sessionID, AsyncCallback<ArrayList<Player>> callback);
    void addPlayer(String sessionID, Player player, AsyncCallback<Player> callback);
    void startGame(String sessionID, AsyncCallback<Void> callback) throws IllegalStateException;
}
