package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardResponse implements IsSerializable {
    // serializable messages cannot contain List but must use a concrete implementation like ArrayList or LinkedList
    String playerUUID;
    ArrayList<Card> cards;
    HashMap<String, Integer> nrOfCardsPerPlayer;

    public CardResponse() {
    }



    public String getPlayerId() {
        return playerUUID;
    }

    public void setPlayerId(String playerId) {
        this.playerUUID = playerId;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public HashMap<String, Integer> getNrOfCardsPerPlayer() {
        return nrOfCardsPerPlayer;
    }

    public void setNrOfCardsPerPlayer(HashMap<String, Integer> nrOfCardsPerPlayer) {
        this.nrOfCardsPerPlayer = nrOfCardsPerPlayer;
    }

    @Override
    public String toString() {
        return "CardResponse{" +
                "playerId=" + playerUUID +
                ", cards=" + cards +
                ", nrOfCardsPerPlayer=" + nrOfCardsPerPlayer +
                '}';
    }
}
