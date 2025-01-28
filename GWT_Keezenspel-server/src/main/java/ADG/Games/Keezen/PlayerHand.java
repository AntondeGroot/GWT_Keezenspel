package ADG.Games.Keezen;

import java.util.ArrayList;

public class PlayerHand {
    private ArrayList<Card> hand;

    public PlayerHand() {
        hand = new ArrayList<>();
    }

    public boolean hasCard(Card card) {
        return hand.stream().anyMatch(c -> c.equals(card));
    }

    public void dropCards(){
        hand = new ArrayList<>();
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void addCard(Card card) {
        hand.add(card);
    }
}
