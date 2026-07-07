package adg.keezen;

import com.adg.openapi.model.Card;

/**
 * A pending team card-trade (step 5). The requester offers one card from their hand and asks
 * their teammate for a King or Ace; the teammate accepts by handing over a King/Ace (the two
 * cards are swapped) or rejects. Only one trade is pending at a time per game.
 */
public class TradeRequest {

  private final String requesterId;
  private final String teammateId;
  private final Card offeredCard;

  public TradeRequest(String requesterId, String teammateId, Card offeredCard) {
    this.requesterId = requesterId;
    this.teammateId = teammateId;
    this.offeredCard = offeredCard;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public String getTeammateId() {
    return teammateId;
  }

  public Card getOfferedCard() {
    return offeredCard;
  }
}
