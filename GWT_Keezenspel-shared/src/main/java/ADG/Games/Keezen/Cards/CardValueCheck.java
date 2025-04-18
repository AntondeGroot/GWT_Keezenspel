package ADG.Games.Keezen.Cards;

public class CardValueCheck {

    public static boolean isAce(Card card) {
        return card.getCardValue() == 1;
    }

    public static boolean isSeven(Card card) {
        return card.getCardValue() == 7;
    }

    public static boolean isJack(Card card){
        return card.getCardValue() == 11;
    }

    public static boolean isKing(Card card){
        return card.getCardValue() == 13;
    }
}
