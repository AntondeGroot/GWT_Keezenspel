package ADG;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

public interface GameStateServiceAsync {
    void getGameState(AsyncCallback<GameStateResponse> callback);
    void getPlayers(AsyncCallback<ArrayList<Player>> callback);
    void addPlayer(Player player, AsyncCallback<Player> callback);
    void startGame(AsyncCallback<Void> callback) throws IllegalStateException;
}
