package ADG.Games.Keezen.Move;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MovingServiceAsync {
  void makeMove(String sessionID, MoveMessage input, AsyncCallback<MoveResponse> callback);

  void getMove(String sessionID, AsyncCallback<MoveResponse> callback);
}
