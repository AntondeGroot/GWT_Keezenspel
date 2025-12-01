package ADG.Games.Keezen.dto;

public class CardClient {

  private int suit;
  private int value;
  private int uuid;

  public CardClient(CardDTO card) {
    this.suit = card.getSuit();
    this.value = card.getValue();
    this.uuid = card.getUuid();
  }

  public int getSuit() {
    return suit;
  }

  public int getValue() {
    return value;
  }

  public int getUuid() {
    return uuid;
  }

  public String toString() {
    return "card_" + this.suit + "_" + this.value;
  }
}
