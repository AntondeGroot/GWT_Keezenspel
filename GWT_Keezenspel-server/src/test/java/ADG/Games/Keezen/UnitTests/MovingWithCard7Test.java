package ADG.Games.Keezen.UnitTests;

import com.adg.openapi.model.Card;
import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;

import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import java.util.LinkedList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.TempMessageType.CHECK_MOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MovingWithCard7Test {

  private MoveRequest moveMessage = new MoveRequest();
  private MoveResponse moveResponse = new MoveResponse();

  private GameSession engine;
  private GameState gameState;
  private CardsDeckInterface cardsDeck;

  @BeforeEach
  void setUp() {
    engine = new GameSession();
    gameState = engine.getGameState();
    cardsDeck = engine.getCardsDeck();

    createGame_With_NPlayers(gameState, 8);
    moveMessage = new MoveRequest();
    moveResponse = new MoveResponse();
  }

  @AfterEach
  void tearDown() {
    gameState.tearDown();
    moveMessage = null;
    moveResponse = null;
    cardsDeck.reset();
  }

  @Test
  void test_moveTwoPawns_correct() {
    // when the pawn is on position 15 and takes two steps it will end up at position 13

    // GIVEN
    Card card = givePlayerSeven(cardsDeck, 0);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0", 1), new PositionKey("0", 0));
    Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0", 2), new PositionKey("0", 10));

    // WHEN
    createSplitMessage(moveMessage, pawn1, 4, pawn2, 3, card);
    gameState.processOnSplit(moveMessage, moveResponse);

    // THEN response is correct
    assertEquals(new PositionKey("0", 4), gameState.getPawn(pawn1).getCurrentTileId());
    assertEquals(new PositionKey("0", 4),
        moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile

    assertEquals(new PositionKey("0", 13), gameState.getPawn(pawn2).getCurrentTileId());
    assertEquals(new PositionKey("0", 13),
        moveResponse.getMovePawn2().getLast());  // moves the pawn to the correct tile
  }

  @Test
  void test_moveTwoPawns_TestSplitAll7TilesForBothPawns() {
    // GIVEN
    Card card = givePlayerSeven(cardsDeck, 0);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0", 1), new PositionKey("0", 0));
    Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0", 2), new PositionKey("0", 5));

    // WHEN no decision was made how to split the 7 among the two pawns
    createSplitMessage(moveMessage, pawn1, 7, pawn2, 7, card);
    moveMessage.setTempMessageType(CHECK_MOVE);
    gameState.processOnSplit(moveMessage, moveResponse);

    LinkedList<PositionKey> expectedTilesPawn1 = new LinkedList<>();
    expectedTilesPawn1.add(new PositionKey("0", 0));
    expectedTilesPawn1.add(new PositionKey("0", 1));
    expectedTilesPawn1.add(new PositionKey("0", 7));

    LinkedList<PositionKey> expectedTilesPawn2 = new LinkedList<>();
    expectedTilesPawn2.add(new PositionKey("0", 5));
    expectedTilesPawn2.add(new PositionKey("0", 7));
    expectedTilesPawn2.add(new PositionKey("0", 12));

    // THEN
    assertEquals(expectedTilesPawn1, moveResponse.getMovePawn1());
    assertEquals(expectedTilesPawn2, moveResponse.getMovePawn2());
  }

  @Test
  void test_moveTwoPawns_TestSplitForBothPawns_OneGoesToNextSegment() {
    // GIVEN
    Card card = givePlayerSeven(cardsDeck, 0);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0", 1), new PositionKey("0", 0));
    Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0", 2), new PositionKey("0", 14));

    // WHEN no decision was made how to split the 7 among the two pawns
    createSplitMessage(moveMessage, pawn1, 3, pawn2, 4,
        card); // the second is null because no choice was made
    moveMessage.setTempMessageType(CHECK_MOVE);
    gameState.processOnSplit(moveMessage, moveResponse);

    LinkedList<PositionKey> expectedTilesPawn1 = new LinkedList<>();
    expectedTilesPawn1.add(new PositionKey("0", 0));
    expectedTilesPawn1.add(new PositionKey("0", 1));
    expectedTilesPawn1.add(new PositionKey("0", 3));

    LinkedList<PositionKey> expectedTilesPawn2 = new LinkedList<>();
    expectedTilesPawn2.add(new PositionKey("0", 14));
    expectedTilesPawn2.add(new PositionKey("0", 15));
    expectedTilesPawn2.add(new PositionKey("1", 1));
    expectedTilesPawn2.add(new PositionKey("1", 2));

    // THEN
    assertEquals(expectedTilesPawn1, moveResponse.getMovePawn1());
    assertEquals(expectedTilesPawn2, moveResponse.getMovePawn2());
  }

  @Test
  void test_moveTwoPawns_Pawn1WasOnFinish_OldPositionDoesNotBlockTestMovePawn2() {
    // bugfix, test move did not show it correctly, however when playing the card it did place the
    // pawns correctly. Assume the first pawn selected moves first

    // GIVEN
    Card card = givePlayerSeven(cardsDeck, 0);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0", 1), new PositionKey("0", 16));
    Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0", 2), new PositionKey("7", 14));

    // WHEN
    createSplitMessage(moveMessage, pawn1, 3, pawn2, 4, card);
    moveMessage.setTempMessageType(CHECK_MOVE);
    gameState.processOnSplit(moveMessage, moveResponse);

    // THEN
    assertEquals(new PositionKey("0", 19), moveResponse.getMovePawn1().getLast());
    assertEquals(new PositionKey("0", 18), moveResponse.getMovePawn2().getLast());
  }

  @Test
  void CheckMove_moveTwoPawns_EndOnSameTile_CannotMove() {
    // GIVEN
    Card card = givePlayerSeven(cardsDeck, 0);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0", 1), new PositionKey("0", 5));
    Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0", 2), new PositionKey("0", 2));

    // WHEN
    createSplitMessage(moveMessage, pawn1, 2, pawn2, 5, card);
    moveMessage.setTempMessageType(CHECK_MOVE);
    gameState.processOnSplit(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
  }

  @Test
  void MakeMove_moveTwoPawns_EndOnSameTile_CannotMove() {
    // GIVEN
    Card card = givePlayerSeven(cardsDeck, 0);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0", 1), new PositionKey("0", 5));
    Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0", 2), new PositionKey("0", 2));

    // WHEN
    createSplitMessage(moveMessage, pawn1, 2, pawn2, 5, card);
    gameState.processOnSplit(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertEquals(new PositionKey("0", 5), gameState.getPawn(pawn1).getCurrentTileId());
    assertEquals(new PositionKey("0", 2), gameState.getPawn(pawn2).getCurrentTileId());
  }
}
