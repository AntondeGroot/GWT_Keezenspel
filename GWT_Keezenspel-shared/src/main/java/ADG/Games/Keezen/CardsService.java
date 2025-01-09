package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("cards")
public interface CardsService extends RemoteService {
    CardResponse getCards(int playerId) throws IllegalArgumentException;
}
