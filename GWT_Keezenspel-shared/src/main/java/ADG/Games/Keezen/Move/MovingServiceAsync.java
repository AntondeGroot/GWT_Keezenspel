package ADG.Games.Keezen.Move;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MovingServiceAsync {
    void makeMove(MoveMessage input, AsyncCallback<MoveResponse> callback);
    void getMove(AsyncCallback<MoveResponse> callback);
}
