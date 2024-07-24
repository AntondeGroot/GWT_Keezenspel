package gwtks.services;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import gwtks.CardsDeck;
import gwtks.GameState;
import gwtks.CardResponse;
import gwtks.CardsService;
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
