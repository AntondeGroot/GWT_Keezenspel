package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.List;

public class CardResponse implements IsSerializable {
    // serializable messages cannot contain List but must use a concrete implementation like ArrayList or LinkedList
    private int playerId;
    private ArrayList<Card> cards;
    private ArrayList<Integer> nrOfCardsPerPlayer;

    public CardResponse() {
    }



    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public List<Integer> getNrOfCardsPerPlayer() {
        return nrOfCardsPerPlayer;
    }

    public void setNrOfCardsPerPlayer(ArrayList<Integer> nrOfCardsPerPlayer) {
        this.nrOfCardsPerPlayer = nrOfCardsPerPlayer;
    }

    @Override
    public String toString() {
        return "CardResponse{" +
                "playerId=" + playerId +
                ", cards=" + cards +
                ", nrOfCardsPerPlayer=" + nrOfCardsPerPlayer +
                '}';
    }
}
