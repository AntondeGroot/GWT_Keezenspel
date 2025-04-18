package ADG.services;

import ADG.Games.Keezen.Cards.CardResponse;
import ADG.Games.Keezen.CardsDeck;
import ADG.Games.Keezen.Cards.CardsService;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet("/app/cards")
public class CardsServiceImpl extends RemoteServiceServlet implements CardsService {

    @Override
    public CardResponse getCards(String playerUUID) throws IllegalArgumentException {
        CardResponse response = new CardResponse();
        response.setPlayerId(playerUUID);
        response.setCards(CardsDeck.getCardsForPlayer(playerUUID));
        response.setPlayedCards(CardsDeck.getPlayedCards());
        response.setNrOfCardsPerPlayer(CardsDeck.getNrOfCardsForAllPlayers());
        return response;
    }
}
