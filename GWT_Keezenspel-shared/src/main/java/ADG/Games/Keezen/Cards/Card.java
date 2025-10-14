package ADG.Games.Keezen.Cards;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Objects;

@SuppressWarnings("serial")
public class Card implements IsSerializable {
    private int suit;
    private int cardValue;
    private int uniqueCardNumber;

    public Card() {
    }

    /**
     * @param suit , suit
     * @param cardValue , value between 1 and 13 (ace,2,...,king)
     * @param uniqueCardNumber , to distinguish between 2 otherwise identical cards like 8♣, this happens when more than 4 players play
     */
    public Card(int suit, int cardValue, int uniqueCardNumber) {
        this.suit = suit;
        this.cardValue = cardValue;
        this.uniqueCardNumber = uniqueCardNumber;
    }

    /**
     * @param suit , suit
     * @param cardValue , value between 1 and 13 (ace,2,...,king)
     */
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

    public int getUniqueCardNumber(){
        return uniqueCardNumber;
    }

    public void setCardValue(int cardValue) {
        this.cardValue = cardValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return suit == card.suit && cardValue == card.cardValue && uniqueCardNumber == card.uniqueCardNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, cardValue, uniqueCardNumber);
    }

    @Override
    public String toString() {
        String cardDescription;
        switch (cardValue){
            case 1: cardDescription = "Ace"; break;
            case 11: cardDescription = "Jack"; break;
            case 12: cardDescription = "Queen"; break;
            case 13: cardDescription = "King"; break;
            default: cardDescription = String.valueOf(cardValue);
        }

        return "Card_" +
                "suit_" + suit +
                "card_" + cardDescription;
    }
}
