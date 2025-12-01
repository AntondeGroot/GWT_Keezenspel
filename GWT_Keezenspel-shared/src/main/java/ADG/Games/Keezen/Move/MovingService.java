package ADG.Games.Keezen.Move;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("move")
public interface MovingService extends RemoteService {
  MoveResponse makeMove(String sessionID, MoveMessage name);

  MoveResponse getMove(String sessionID);
}
