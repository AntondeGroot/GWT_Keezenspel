package ADG;

import ADG.Games.Keezen.dto.CardClient;

public enum CardEnum {
  ACE(0, 1),
  JACK(0, 11),
  KING(0, 13),
  NORMALCARD(0, 5);

  private final CardClient card;

  CardEnum(int value1, int value2) {
    this.card = new CardClient(value1, value2);
  }

  public CardClient get() {
    return card;
  }
}