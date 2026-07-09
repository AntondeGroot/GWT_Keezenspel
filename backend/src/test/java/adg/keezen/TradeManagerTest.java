package adg.keezen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adg.openapi.model.Card;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Direct unit tests for the extracted team-trade rules. Collaborators are injected, so every branch
 * is reachable without building a full GameState.
 */
class TradeManagerTest {

  private static final Card OFFERED = new Card().suit(0).value(5).uuid(500);
  private static final Card KING = new Card().suit(0).value(13).uuid(513);
  private static final Card ACE = new Card().suit(0).value(1).uuid(501);
  private static final Card NINE = new Card().suit(0).value(9).uuid(509);

  private CardsDeckInterface deck;
  private AtomicLong version;
  private TradeManager trade;

  private TradeManager build(boolean teamGameStarted) {
    BooleanSupplier started = () -> teamGameStarted;
    // "R" (requester) is partnered with "T" (teammate); anyone else has no teammate.
    return new TradeManager(
        deck,
        version,
        started,
        id -> "R".equals(id) ? "T" : null,
        card -> card.getValue() == 13 || card.getValue() == 1);
  }

  @BeforeEach
  void setUp() {
    deck = mock(CardsDeckInterface.class);
    version = new AtomicLong(0);
    trade = build(true);
    trade.setEnabled(true);
  }

  /** Stub the deck and record a pending R→T offer of OFFERED, returning after asserting success. */
  private void givenPendingOffer() {
    when(deck.playerHasCard("R", OFFERED)).thenReturn(true);
    assertTrue(trade.request("R", OFFERED));
    assertEquals(1, version.get());
  }

  // ── enabled flag ──────────────────────────────────────────────────────────
  @Test
  void enabledFlagRoundTrips() {
    trade.setEnabled(false);
    assertFalse(trade.isEnabled());
    trade.setEnabled(true);
    assertTrue(trade.isEnabled());
  }

  // ── request ───────────────────────────────────────────────────────────────
  @Test
  void requestRejectedWhenGameNotStarted() {
    trade = build(false);
    trade.setEnabled(true);
    when(deck.playerHasCard("R", OFFERED)).thenReturn(true);
    assertFalse(trade.request("R", OFFERED));
    assertNull(trade.getPending());
    assertEquals(0, version.get());
  }

  @Test
  void requestRejectedWhenTradingDisabled() {
    trade.setEnabled(false);
    assertFalse(trade.request("R", OFFERED));
    assertNull(trade.getPending());
  }

  @Test
  void requestRejectedWhenTradeAlreadyPending() {
    givenPendingOffer();
    assertFalse(trade.request("R", OFFERED));
    assertEquals(1, version.get()); // unchanged by the second request
  }

  @Test
  void requestRejectedWhenPlayerHasNoTeammate() {
    when(deck.playerHasCard("X", OFFERED)).thenReturn(true);
    assertFalse(trade.request("X", OFFERED)); // "X" resolves to no teammate
    assertNull(trade.getPending());
  }

  @Test
  void requestRejectedWhenOfferedCardIsNull() {
    assertFalse(trade.request("R", null));
    assertNull(trade.getPending());
  }

  @Test
  void requestRejectedWhenRequesterDoesNotHoldOfferedCard() {
    when(deck.playerHasCard("R", OFFERED)).thenReturn(false);
    assertFalse(trade.request("R", OFFERED));
    assertNull(trade.getPending());
  }

  @Test
  void requestRecordsPendingOfferAndBumpsVersion() {
    givenPendingOffer();
    TradeRequest pending = trade.getPending();
    assertEquals("R", pending.getRequesterId());
    assertEquals("T", pending.getTeammateId());
    assertSame(OFFERED, pending.getOfferedCard());
  }

  // ── accept ────────────────────────────────────────────────────────────────
  @Test
  void acceptRejectedWhenNoPendingTrade() {
    assertFalse(trade.accept("T", KING));
  }

  @Test
  void acceptRejectedWhenNotTheAddressedTeammate() {
    givenPendingOffer();
    assertFalse(trade.accept("someone-else", KING));
    assertEquals("T", trade.getPending().getTeammateId()); // still pending
  }

  @Test
  void acceptRejectedWhenCardIsNull() {
    givenPendingOffer();
    assertFalse(trade.accept("T", null));
  }

  @Test
  void acceptRejectedWhenCardIsNotKingOrAce() {
    givenPendingOffer();
    when(deck.playerHasCard("T", NINE)).thenReturn(true);
    assertFalse(trade.accept("T", NINE));
  }

  @Test
  void acceptRejectedWhenTeammateDoesNotHoldKingOrAce() {
    givenPendingOffer();
    when(deck.playerHasCard("T", KING)).thenReturn(false);
    assertFalse(trade.accept("T", KING));
  }

  @Test
  void acceptDropsStaleTradeWhenRequesterNoLongerHoldsOfferedCard() {
    givenPendingOffer();
    when(deck.playerHasCard("T", KING)).thenReturn(true);
    when(deck.playerHasCard("R", OFFERED)).thenReturn(false); // requester left/forfeited
    assertFalse(trade.accept("T", KING));
    assertNull(trade.getPending()); // stale trade dropped
    assertEquals(2, version.get()); // dropped bumps version
    verify(deck, never()).moveCardBetweenHands(org.mockito.ArgumentMatchers.anyString(),
        org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void acceptWithAceSwapsBothCardsClearsTradeAndBumpsVersion() {
    givenPendingOffer();
    when(deck.playerHasCard("T", ACE)).thenReturn(true);
    when(deck.playerHasCard("R", OFFERED)).thenReturn(true);
    assertTrue(trade.accept("T", ACE));
    verify(deck).moveCardBetweenHands("R", "T", OFFERED);
    verify(deck).moveCardBetweenHands("T", "R", ACE);
    assertNull(trade.getPending());
    assertEquals(2, version.get());
  }

  // ── reject ────────────────────────────────────────────────────────────────
  @Test
  void rejectRejectedWhenNoPendingTrade() {
    assertFalse(trade.reject("T"));
  }

  @Test
  void rejectRejectedWhenNotTheAddressedTeammate() {
    givenPendingOffer();
    assertFalse(trade.reject("someone-else"));
    assertEquals("T", trade.getPending().getTeammateId());
  }

  @Test
  void rejectClearsPendingTrade() {
    givenPendingOffer();
    assertTrue(trade.reject("T"));
    assertNull(trade.getPending());
    assertEquals(2, version.get());
  }

  // ── cancel ────────────────────────────────────────────────────────────────
  @Test
  void cancelRejectedWhenNoPendingTrade() {
    assertFalse(trade.cancel("R"));
  }

  @Test
  void cancelRejectedWhenNotTheRequester() {
    givenPendingOffer();
    assertFalse(trade.cancel("T")); // the teammate can't cancel
    assertEquals("R", trade.getPending().getRequesterId());
  }

  @Test
  void cancelClearsPendingTrade() {
    givenPendingOffer();
    assertTrue(trade.cancel("R"));
    assertNull(trade.getPending());
    assertEquals(2, version.get());
  }

  // ── cancelForDeparture ──────────────────────────────────────────────────────
  @Test
  void departureIsNoOpWhenNoPendingTrade() {
    trade.cancelForDeparture("R"); // must not throw
    assertNull(trade.getPending());
  }

  @Test
  void departureOfRequesterDropsTrade() {
    givenPendingOffer();
    trade.cancelForDeparture("R");
    assertNull(trade.getPending());
  }

  @Test
  void departureOfTeammateDropsTrade() {
    givenPendingOffer();
    trade.cancelForDeparture("T");
    assertNull(trade.getPending());
  }

  @Test
  void departureOfUninvolvedPlayerKeepsTrade() {
    givenPendingOffer();
    trade.cancelForDeparture("someone-else");
    assertEquals("R", trade.getPending().getRequesterId());
  }

  // ── clearPending ────────────────────────────────────────────────────────────
  @Test
  void clearPendingRemovesPendingTrade() {
    givenPendingOffer();
    trade.clearPending();
    assertNull(trade.getPending());
  }
}
