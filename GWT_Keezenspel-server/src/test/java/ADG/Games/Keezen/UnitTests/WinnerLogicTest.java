package ADG.Games.Keezen.UnitTests;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WinnerLogicTest {

  ArrayList<String> winners = new ArrayList<>();

  private GameState gameState;
  private CardsDeckInterface cardsDeck;

  @BeforeEach
  void setup() {
    GameSession session = new GameSession();
    gameState = session.getGameState();
    cardsDeck = session.getCardsDeck();
    createGame_With_NPlayers(gameState, 3);
  }

  @AfterEach
  void tearDown() {
    gameState.tearDown();
    winners.clear();
  }

  @Test
  void testOneWinner() {
    // GIVEN
    place4PawnsOnFinish(gameState, "0");

    // WHEN
    gameState.checkForWinners(winners);

    // THEN
    assertTrue(winners.contains("0"));
  }

  @Test
  void whenSomeoneWins_HandGetsEmptied_NextPlayerPLays() {
    // GIVEN 3 pawns already on finish, pawn 3 one step away on the preceding player's section
    placePawnOnBoard(gameState, new PawnId("0", 0), new PositionKey("0", 17));
    placePawnOnBoard(gameState, new PawnId("0", 1), new PositionKey("0", 18));
    placePawnOnBoard(gameState, new PawnId("0", 2), new PositionKey("0", 19));
    Pawn pawn3 = placePawnOnBoard(gameState, new PawnId("0", 3), new PositionKey("2", 15));
    Card ace = givePlayerCard(cardsDeck, 0, 1);

    // WHEN the last card (Ace) is played to move pawn 3 into the finish lane
    MoveRequest moveRequest = new MoveRequest();
    createMoveRequest(moveRequest, pawn3, ace);
    gameState.processOnMove(moveRequest, new MoveResponse());

    // THEN player "0" won, hand is emptied, next player plays
    assertTrue(gameState.getWinners().contains("0"));
    assertEquals(0, cardsDeck.getCardsForPlayer("0").size());
    assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void whenPlayer2Wins_ItsPlayer1sTurn_WhenHeWins_ItsPlayers0sTurn() {
    // GIVEN player 0 and player 1 forfeit their round cards
    gameState.processOnForfeit("0");
    gameState.processOnForfeit("1");

    // GIVEN player 2 has 3 pawns on finish and pawn 3 one step away (preceding section is "1")
    placePawnOnBoard(gameState, new PawnId("2", 0), new PositionKey("2", 19));
    placePawnOnBoard(gameState, new PawnId("2", 1), new PositionKey("2", 18));
    placePawnOnBoard(gameState, new PawnId("2", 2), new PositionKey("2", 17));
    Pawn pawn2_3 = placePawnOnBoard(gameState, new PawnId("2", 3), new PositionKey("1", 15));
    Card ace2 = givePlayerAce(cardsDeck, 2);

    // WHEN player 2 wins by playing Ace
    MoveRequest req2 = new MoveRequest();
    createMoveRequest(req2, pawn2_3, ace2);
    gameState.processOnMove(req2, new MoveResponse());

    // THEN a new round started; player 1 has the turn (next after player 0 who started round 1)
    assertTrue(gameState.getWinners().contains("2"));
    assertEquals("1", gameState.getPlayerIdTurn());

    // GIVEN player 1 has 3 pawns on finish and pawn 3 one step away (preceding section is "0")
    placePawnOnBoard(gameState, new PawnId("1", 0), new PositionKey("1", 19));
    placePawnOnBoard(gameState, new PawnId("1", 1), new PositionKey("1", 18));
    placePawnOnBoard(gameState, new PawnId("1", 2), new PositionKey("1", 17));
    Pawn pawn1_3 = placePawnOnBoard(gameState, new PawnId("1", 3), new PositionKey("0", 15));
    Card ace1 = givePlayerAce(cardsDeck, 1);

    // WHEN player 1 wins by playing Ace
    MoveRequest req1 = new MoveRequest();
    createMoveRequest(req1, pawn1_3, ace1);
    gameState.processOnMove(req1, new MoveResponse());

    // THEN it's player 0's turn and no winner is shown as playing
    assertTrue(gameState.getWinners().contains("1"));
    assertEquals("0", gameState.getPlayerIdTurn());
    assertFalse(gameState.getWinners().stream().anyMatch(winnerId ->
        gameState.getPlayers().stream()
            .anyMatch(p -> p.getId().equals(winnerId) && Boolean.TRUE.equals(p.getIsPlaying()))));
  }

  @Test
  void testPlayer2Wins_ThenPlayer1Wins() {
    // GIVEN
    place4PawnsOnFinish(gameState, "2");
    gameState.checkForWinners(winners);

    // WHEN
    place4PawnsOnFinish(gameState, "1");
    gameState.checkForWinners(winners);

    // THEN
    assertEquals(stringsToList(new String[] {"2", "1"}), winners);
  }

  @Test
  void whenPlayer1WinsInTwoPlayerGame_Player1GetsFirstMedal_NotPlayer0() {
    // Re-setup with 2 players: player "0" (red) and player "1" (blue)
    createGame_With_NPlayers(gameState, 2);

    // GIVEN player "0" forfeits this round so player "1" has the turn
    gameState.processOnForfeit("0");

    // GIVEN player "1" has 3 pawns on their finish and pawn 3 one step away in player "0"'s section
    placePawnOnBoard(gameState, new PawnId("1", 0), new PositionKey("1", 17));
    placePawnOnBoard(gameState, new PawnId("1", 1), new PositionKey("1", 18));
    placePawnOnBoard(gameState, new PawnId("1", 2), new PositionKey("1", 19));
    Pawn pawn1_3 = placePawnOnBoard(gameState, new PawnId("1", 3), new PositionKey("0", 15));
    Card ace = givePlayerAce(cardsDeck, 1);

    // WHEN player "1" plays the Ace to move their last pawn into the finish
    MoveRequest moveRequest = new MoveRequest();
    createMoveRequest(moveRequest, pawn1_3, ace);
    gameState.processOnMove(moveRequest, new MoveResponse());

    // THEN player "1" wins with place 1, and player "0" does NOT receive a medal
    assertTrue(gameState.getWinners().contains("1"), "Player 1 (blue) should be in the winners list");
    assertEquals(1,
        gameState.getPlayers().stream().filter(p -> p.getId().equals("1")).findFirst().get().getPlace(),
        "Player 1 (blue) should have place 1 — the first medal");
    assertEquals(-1,
        gameState.getPlayers().stream().filter(p -> p.getId().equals("0")).findFirst().get().getPlace(),
        "Player 0 (red) should NOT have a medal — place should remain -1");
  }

  @Test
  void testPlayer2Wins_Player0Wins_ThenPlayer1Wins() {
    // GIVEN
    place4PawnsOnFinish(gameState, "2");
    gameState.checkForWinners(winners);
    place4PawnsOnFinish(gameState, "0");
    gameState.checkForWinners(winners);

    // WHEN
    place4PawnsOnFinish(gameState, "1");
    gameState.checkForWinners(winners);

    // THEN
    assertEquals(stringsToList(new String[] {"2", "0", "1"}), winners);
  }
}
