package adg.keezen;

import com.adg.openapi.model.Card;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * The team card-trade sub-game (game option "step 5"): a player offers one of their cards and asks
 * their teammate for a King or Ace. Extracted from GameState so the rules live in one small,
 * directly testable unit; GameState keeps thin public methods that delegate here. Collaborators
 * (deck, version counter, team/started status, teammate lookup, King/Ace test) are injected so this
 * class has no dependency on the rest of the game state.
 */
class TradeManager {

  private volatile boolean enabled = false;
  private volatile TradeRequest pending = null;

  private final CardsDeckInterface cardsDeck;
  private final AtomicLong version;
  private final BooleanSupplier teamGameStarted;
  private final UnaryOperator<String> teammateResolver;
  private final Predicate<Card> isKingOrAce;

  TradeManager(
      CardsDeckInterface cardsDeck,
      AtomicLong version,
      BooleanSupplier teamGameStarted,
      UnaryOperator<String> teammateResolver,
      Predicate<Card> isKingOrAce) {
    this.cardsDeck = cardsDeck;
    this.version = version;
    this.teamGameStarted = teamGameStarted;
    this.teammateResolver = teammateResolver;
    this.isKingOrAce = isKingOrAce;
  }

  void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  boolean isEnabled() {
    return enabled;
  }

  TradeRequest getPending() {
    return pending;
  }

  /** Clear any pending trade — called from GameState.stop()/reset(). */
  void clearPending() {
    pending = null;
  }

  /**
   * A player offers a card and asks their teammate for a King or Ace. Allowed only in a started team
   * game with the trade sub-option on, when the requester holds the offered card and no trade is
   * already pending. Returns true if the request was recorded.
   */
  boolean request(String requesterId, Card offeredCard) {
    if (!teamGameStarted.getAsBoolean() || !enabled || pending != null) {
      return false;
    }
    String teammate = teammateResolver.apply(requesterId);
    if (teammate == null
        || offeredCard == null
        || !cardsDeck.playerHasCard(requesterId, offeredCard)) {
      return false;
    }
    pending = new TradeRequest(requesterId, teammate, offeredCard);
    version.incrementAndGet();
    return true;
  }

  /**
   * The teammate accepts by handing over a King or Ace: the two cards are swapped and the trade
   * clears. Only the addressed teammate may accept, and only with a King/Ace they actually hold.
   */
  boolean accept(String teammateId, Card kingOrAce) {
    if (pending == null || !pending.getTeammateId().equals(teammateId)) {
      return false;
    }
    if (kingOrAce == null
        || !isKingOrAce.test(kingOrAce)
        || !cardsDeck.playerHasCard(teammateId, kingOrAce)) {
      return false;
    }
    String requesterId = pending.getRequesterId();
    // The requester must still hold the card they offered — if they left or forfeited, that card
    // was moved to the pile, and swapping it would duplicate it (and sink the teammate's card into
    // a discarded hand). Drop the stale trade rather than corrupt the hands.
    if (!cardsDeck.playerHasCard(requesterId, pending.getOfferedCard())) {
      pending = null;
      version.incrementAndGet();
      return false;
    }
    cardsDeck.moveCardBetweenHands(requesterId, teammateId, pending.getOfferedCard());
    cardsDeck.moveCardBetweenHands(teammateId, requesterId, kingOrAce);
    pending = null;
    version.incrementAndGet();
    return true;
  }

  /** The teammate declines (can't or won't); the trade clears with no swap. */
  boolean reject(String teammateId) {
    if (pending == null || !pending.getTeammateId().equals(teammateId)) {
      return false;
    }
    pending = null;
    version.incrementAndGet();
    return true;
  }

  /** The requester withdraws their pending offer. */
  boolean cancel(String requesterId) {
    if (pending == null || !pending.getRequesterId().equals(requesterId)) {
      return false;
    }
    pending = null;
    version.incrementAndGet();
    return true;
  }

  /** Drop a pending trade if this player (leaving or forfeiting) is either party to it. */
  void cancelForDeparture(String playerId) {
    if (pending != null
        && (playerId.equals(pending.getRequesterId())
            || playerId.equals(pending.getTeammateId()))) {
      pending = null;
    }
  }
}
