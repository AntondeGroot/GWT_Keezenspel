package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CardResponse implements IsSerializable {
    // serializable messages cannot contain List but must use a concrete implementation like ArrayList or LinkedList
    String playerUUID;
    ArrayList<Card> cards;
    ArrayList<Card> playedCards;
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

    public ArrayList<Card> getPlayedCards() {
        return playedCards;
    }

    public void setPlayedCards(ArrayList<Card> playedCards) {
        this.playedCards = playedCards;
    }

    public HashMap<String, Integer> getNrOfCardsPerPlayer() {
        return nrOfCardsPerPlayer;
    }

    public void setNrOfCardsPerPlayer(HashMap<String, Integer> nrOfCardsPerPlayer) {
        this.nrOfCardsPerPlayer = nrOfCardsPerPlayer;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        CardResponse that = (CardResponse) o;
        return Objects.equals(playerUUID, that.playerUUID) && Objects.equals(cards, that.cards) && Objects.equals(playedCards, that.playedCards) && Objects.equals(nrOfCardsPerPlayer, that.nrOfCardsPerPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID, cards, playedCards, nrOfCardsPerPlayer);
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
