package gwtks;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GameStateServiceAsync {
    void getGameState(AsyncCallback<GameStateResponse> callback);

    void startGame(AsyncCallback<Void> callback);
}
