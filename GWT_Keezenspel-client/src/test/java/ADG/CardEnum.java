package ADG;

import ADG.Games.Keezen.Cards.Card;
import com.google.gwt.user.client.rpc.IsSerializable;

public enum CardEnum implements IsSerializable {
  ACE(0, 1),
  JACK(0, 11),
  KING(0, 13),
  NORMALCARD(0, 5);

  private final Card card;

  CardEnum(int value1, int value2) {
    this.card = new Card(value1, value2);
  }

  public Card get() {
    return card;
  }
}
