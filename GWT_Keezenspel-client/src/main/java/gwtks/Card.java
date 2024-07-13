package gwtks;

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
}
