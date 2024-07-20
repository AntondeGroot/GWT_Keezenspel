package gwtks;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private int playerId;
    private ArrayList<Card> hand;

    public Player(int playerId) {
        this.playerId = playerId;
        hand = new ArrayList<Card>();
    }

    public boolean hasCard(Card card) {
        return hand.stream().anyMatch(c -> c.equals(card));
    }

    public int getPlayerId() {
        return playerId;
    }

    public void dropCards(){
        hand = new ArrayList<>();
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void setCard(Card card) {
        this.hand.add(card);
    }
}