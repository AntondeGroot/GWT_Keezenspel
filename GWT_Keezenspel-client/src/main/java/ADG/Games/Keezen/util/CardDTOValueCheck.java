package ADG.Games.Keezen.util;

import ADG.Games.Keezen.dto.CardDTO;

public class CardDTOValueCheck {

    public static boolean isAce(CardDTO card) {
        return card.getValue() == 1;
    }

    public static boolean isSeven(CardDTO card) {
        return card.getValue() == 7;
    }

    public static boolean isJack(CardDTO card){
        return card.getValue() == 11;
    }

    public static boolean isKing(CardDTO card){
        return card.getValue() == 13;
    }
}
