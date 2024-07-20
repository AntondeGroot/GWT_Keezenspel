package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Objects;

@SuppressWarnings("serial")
public class Card implements IsSerializable {
    private int suit;
    private int card;

    public Card() {
    }

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

    @Override
    public String toString() {
        return "Card{" +
                "suit=" + suit +
                ", card=" + card +
                '}';
    }
}
