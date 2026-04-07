package adg.keezen.util;

import adg.keezen.dto.CardClient;

public class CardValueCheck {

  public static boolean isAce(CardClient card) {
    return card.getValue() == 1;
  }

  public static boolean isSeven(CardClient card) {
    return card.getValue() == 7;
  }

  public static boolean isJack(CardClient card) {
    return card.getValue() == 11;
  }

  public static boolean isKing(CardClient card) {
    return card.getValue() == 13;
  }
}
