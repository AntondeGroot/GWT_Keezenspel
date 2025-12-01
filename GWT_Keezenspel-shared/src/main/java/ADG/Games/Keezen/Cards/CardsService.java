package ADG.Games.Keezen.Cards;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("cards")
public interface CardsService extends RemoteService {
  CardResponse getCards(String sessionID, String playerUUID) throws IllegalArgumentException;
}
