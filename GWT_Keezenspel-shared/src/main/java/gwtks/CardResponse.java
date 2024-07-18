package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

@SuppressWarnings("serial")
public class CardResponse implements IsSerializable {
    private int playerId;
    private List<Card> cards;
    private List<Integer> nrOfCardsPerPlayer;

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public List<Integer> getNrOfCardsPerPlayer() {
        return nrOfCardsPerPlayer;
    }

    public void setNrOfCardsPerPlayer(List<Integer> nrOfCardsPerPlayer) {
        this.nrOfCardsPerPlayer = nrOfCardsPerPlayer;
    }

    @Override
    public String toString() {
        return "CardResponse{" +
                "playerId=" + playerId +
                ", cards=" + cards +
                '}';
    }
}
