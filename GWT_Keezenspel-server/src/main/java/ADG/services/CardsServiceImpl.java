package ADG.services;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import ADG.Games.Keezen.CardsDeck;
import ADG.Games.Keezen.CardResponse;
import ADG.Games.Keezen.CardsService;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet("/app/cards")
public class CardsServiceImpl extends RemoteServiceServlet implements CardsService {

    @Override
    public CardResponse getCards(String playerUUID) throws IllegalArgumentException {
        CardResponse response = new CardResponse();
        response.setPlayerId(playerUUID);
        response.setCards(CardsDeck.getCardsForPlayer(playerUUID));
        response.setNrOfCardsPerPlayer(CardsDeck.getNrOfCardsForAllPlayers());
        return response;
    }
}
