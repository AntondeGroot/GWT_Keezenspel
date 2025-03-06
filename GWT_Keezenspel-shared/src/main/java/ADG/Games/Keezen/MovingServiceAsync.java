package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MovingServiceAsync {
    void makeMove(MoveMessage input, AsyncCallback<MoveResponse> callback);
    void getMove(AsyncCallback<MoveResponse> callback);
}
