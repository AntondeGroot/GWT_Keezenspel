package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("move")
public interface MovingService extends RemoteService {
    MoveResponse makeMove(MoveMessage name) throws IllegalArgumentException;
}
