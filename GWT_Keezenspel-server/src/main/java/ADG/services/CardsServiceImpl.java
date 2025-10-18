package ADG.services;

import ADG.Games.Keezen.Cards.CardResponse;
import ADG.Games.Keezen.Cards.CardsService;
import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameRegistry;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet("/app/cards")
public class CardsServiceImpl extends RemoteServiceServlet implements CardsService {

    @Override
    public CardResponse getCards(String sessionID, String playerUUID) throws IllegalArgumentException {
        if (sessionID == null || playerUUID == null) {
            return new CardResponse();
        }

        GameSession session = GameRegistry.getGame(sessionID);
        if (session == null) {
            return new CardResponse();
        }
        CardsDeckInterface cardsDeck = session.getCardsDeck();

        CardResponse response = new CardResponse();
//        response.setPlayerId(playerUUID);
//        response.setCards(cardsDeck.getCardsForPlayer(playerUUID));
//        response.setPlayedCards(cardsDeck.getPlayedCards());
//        response.setNrOfCardsPerPlayer(cardsDeck.getNrOfCardsPerPlayer());
        return response;
    }
}
