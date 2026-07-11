package adg.services;

import adg.keezen.CardsDeckInterface;
import com.adg.openapi.model.Card;
import java.util.List;

/** Public (client-visible) representations of cards, shared by the SSE push and the cards API. */
final class PublicCards {

  private PublicCards() {}

  /** A card's public id ("suit_value") — the client uses it for resource lookup. */
  static String id(Card card) {
    return card.getSuit() + "_" + card.getValue();
  }

  /** The public ids of every card played so far, in play order. */
  static List<String> playedIds(CardsDeckInterface deck) {
    return deck.getPlayedCards().stream().map(PublicCards::id).toList();
  }
}
