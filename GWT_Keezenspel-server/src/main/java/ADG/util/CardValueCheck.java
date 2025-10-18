package ADG.util;

import com.adg.openapi.model.Card;

public class CardValueCheck {

    public static boolean isAce(Card card) {
        return card.getValue() == 1;
    }

    public static boolean isSeven(Card card) {
        return card.getValue() == 7;
    }

    public static boolean isJack(Card card){
        return card.getValue() == 11;
    }

    public static boolean isKing(Card card){
        return card.getValue() == 13;
    }
}
