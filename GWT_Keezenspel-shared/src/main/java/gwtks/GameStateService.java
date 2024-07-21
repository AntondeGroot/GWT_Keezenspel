package gwtks;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("gamestate")
public interface GameStateService extends RemoteService {
    GameStateResponse getGameState() throws IllegalArgumentException;
}
