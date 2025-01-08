package ADG.services;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import ADG.CardsDeck;
import ADG.CardResponse;
import ADG.CardsService;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet("/app/cards")
public class CardsServiceImpl extends RemoteServiceServlet implements CardsService {

    @Override
    public CardResponse getCards(int playerId) throws IllegalArgumentException {
        CardResponse response = new CardResponse();
        response.setPlayerId(playerId);
        response.setCards(CardsDeck.getCardsForPlayer(playerId));
        response.setNrOfCardsPerPlayer(CardsDeck.getNrOfCardsForAllPlayers());
        return response;
    }
}
