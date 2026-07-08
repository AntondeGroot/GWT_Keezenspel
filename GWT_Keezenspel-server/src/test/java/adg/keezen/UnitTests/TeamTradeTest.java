package adg.keezen.UnitTests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adg.keezen.CardsDeckInterface;
import adg.keezen.CardsDeckMock;
import adg.keezen.GameSession;
import adg.keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Team play, step 5: the two-way King/Ace card trade. The requester offers a card and asks their
 * teammate for a King or Ace; the teammate accepts (the two cards swap) or declines. Only allowed
 * in a team game with the sub-option on, one trade at a time. (Teams are pairs: 4 players → team 0
 * = players 0 &amp; 2.)
 */
class TeamTradeTest {

  private GameState gs;
  private CardsDeckInterface deck;

  private static final Card OFFERED = new Card().suit(0).value(5).uuid(500); // a plain card A gives
  private static final Card KING = new Card().suit(0).value(13).uuid(513);
  private static final Card ACE = new Card().suit(0).value(1).uuid(501);
  private static final Card NOT_KING_OR_ACE = new Card().suit(0).value(9).uuid(509);

  @BeforeEach
  void setUp() {
    GameSession engine = new GameSession(new CardsDeckMock());
    gs = engine.getGameState();
    deck = engine.getCardsDeck();
    gs.setTeamPlay(true);
    gs.setTeamCardTrade(true);
    for (int i = 0; i < 4; i++) {
      gs.addPlayer(new Player(String.valueOf(i), String.valueOf(i)));
    }
    gs.start(false); // seat == insertion order; team 0 = players 0 & 2
  }

  /** Player 0 offers OFFERED and its teammate (player 2) holds a King. */
  private void armStandardTrade() {
    deck.giveCardToPlayerForTesting("0", OFFERED);
    deck.giveCardToPlayerForTesting("2", KING);
  }

  @Test
  void acceptedTrade_swapsTheTwoCards() {
    armStandardTrade();
    assertTrue(gs.requestTrade("0", OFFERED));
    assertTrue(gs.acceptTrade("2", KING));

    assertTrue(deck.playerHasCard("0", KING), "requester received the King");
    assertTrue(deck.playerHasCard("2", OFFERED), "teammate received the offered card");
    assertFalse(deck.playerHasCard("0", OFFERED));
    assertFalse(deck.playerHasCard("2", KING));
    assertNull(gs.getPendingTrade(), "trade cleared after it completes");
  }

  @Test
  void pendingTrade_clearedWhenRequesterLeaves() {
    armStandardTrade();
    assertTrue(gs.requestTrade("0", OFFERED));
    gs.processLeaveGame("0"); // requester leaves — their offered card is forfeited to the pile

    assertNull(gs.getPendingTrade(), "the trade must not survive the requester leaving");
    assertFalse(gs.acceptTrade("2", KING), "a stale trade cannot be accepted");
    // Hands stay consistent: the teammate keeps their King and gets no duplicate of the forfeited card.
    assertTrue(deck.playerHasCard("2", KING), "teammate still holds their King");
    assertFalse(deck.playerHasCard("2", OFFERED), "no duplicate of the forfeited offered card");
  }

  @Test
  void pendingTrade_clearedWhenTeammateLeaves() {
    armStandardTrade();
    assertTrue(gs.requestTrade("0", OFFERED));
    gs.processLeaveGame("2"); // the addressed teammate leaves

    assertNull(gs.getPendingTrade(), "the trade must not survive the teammate leaving");
    assertFalse(gs.acceptTrade("2", KING));
  }

  @Test
  void aceIsAlsoAcceptable() {
    deck.giveCardToPlayerForTesting("0", OFFERED);
    deck.giveCardToPlayerForTesting("2", ACE);
    assertTrue(gs.requestTrade("0", OFFERED));
    assertTrue(gs.acceptTrade("2", ACE));
    assertTrue(deck.playerHasCard("0", ACE));
  }

  @Test
  void request_isBlockedWhenTheSubOptionIsOff() {
    gs.setTeamCardTrade(false);
    armStandardTrade();
    assertFalse(gs.requestTrade("0", OFFERED));
    assertNull(gs.getPendingTrade());
  }

  @Test
  void request_isBlockedWhenYouDoNotHoldTheOfferedCard() {
    // never gave player 0 the OFFERED card
    assertFalse(gs.requestTrade("0", OFFERED));
  }

  @Test
  void onlyOneTradeAtATime() {
    armStandardTrade();
    assertTrue(gs.requestTrade("0", OFFERED));
    assertFalse(gs.requestTrade("0", OFFERED), "a second request is refused while one is pending");
  }

  @Test
  void accept_isRefusedForACardThatIsNotAKingOrAce() {
    deck.giveCardToPlayerForTesting("0", OFFERED);
    deck.giveCardToPlayerForTesting("2", NOT_KING_OR_ACE);
    assertTrue(gs.requestTrade("0", OFFERED));
    assertFalse(gs.acceptTrade("2", NOT_KING_OR_ACE));
    assertFalse(deck.playerHasCard("0", NOT_KING_OR_ACE), "no swap happened");
    assertTrue(gs.getPendingTrade() != null, "a refused accept leaves the trade pending");
  }

  @Test
  void accept_isRefusedFromANonTeammate() {
    armStandardTrade();
    deck.giveCardToPlayerForTesting("1", KING); // opponent also holds a King
    assertTrue(gs.requestTrade("0", OFFERED));
    assertFalse(gs.acceptTrade("1", KING), "an opponent can't accept a trade meant for the teammate");
  }

  @Test
  void reject_clearsTheTradeWithoutSwapping() {
    armStandardTrade();
    assertTrue(gs.requestTrade("0", OFFERED));
    assertTrue(gs.rejectTrade("2"));
    assertNull(gs.getPendingTrade());
    assertTrue(deck.playerHasCard("0", OFFERED), "offered card stays with the requester");
    assertTrue(deck.playerHasCard("2", KING), "teammate keeps their King");
  }

  @Test
  void requester_canCancelTheirPendingOffer() {
    armStandardTrade();
    assertTrue(gs.requestTrade("0", OFFERED));
    assertTrue(gs.cancelTrade("0"));
    assertNull(gs.getPendingTrade());
  }
}
