package ADG;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Objects;

@SuppressWarnings("serial")
public class Card implements IsSerializable {
    private int suit;
    private int cardValue;

    public Card() {
    }

    public Card(int suit, int cardValue) {
        this.suit = suit;
        this.cardValue = cardValue;
    }

    public int getSuit() {
        return suit;
    }

    public void setSuit(int suit) {
        this.suit = suit;
    }

    public int getCardValue() {
        return cardValue;
    }

    public void setCardValue(int cardValue) {
        this.cardValue = cardValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card1 = (Card) o;
        return suit == card1.suit && cardValue == card1.cardValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, cardValue);
    }

    @Override
    public String toString() {
        return "Card{" +
                "suit=" + suit +
                ", card=" + cardValue +
                '}';
    }
}
