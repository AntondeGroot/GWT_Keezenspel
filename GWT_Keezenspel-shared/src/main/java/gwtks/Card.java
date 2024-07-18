package gwtks;

import java.util.Objects;

public class Card {
    private int suit;
    private int card;

    public Card(int suit, int card) {
        this.suit = suit;
        this.card = card;
    }

    public int getSuit() {
        return suit;
    }

    public void setSuit(int suit) {
        this.suit = suit;
    }

    public int getCard() {
        return card;
    }

    public void setCard(int card) {
        this.card = card;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card1 = (Card) o;
        return suit == card1.suit && card == card1.card;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, card);
    }
}
