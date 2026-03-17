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

  public CardClient(int suit, int value) {
    this.suit = suit;
    this.value = value;
  }

  public int getSuit() {
    return suit;
  }

  public int getValue() {
    return value;
  }

  /** Alias for getValue(), for compatibility with old Card.getCardValue(). */
  public int getCardValue() {
    return value;
  }

  public int getUuid() {
    return uuid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CardClient)) return false;
    CardClient other = (CardClient) o;
    return suit == other.suit && value == other.value && uuid == other.uuid;
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(suit, value, uuid);
  }

  public String toString() {
    return "card_" + this.suit + "_" + this.value;
  }
}
