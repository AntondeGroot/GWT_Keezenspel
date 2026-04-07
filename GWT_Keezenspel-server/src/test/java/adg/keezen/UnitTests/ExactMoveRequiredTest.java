package adg.keezen.UnitTests;

import static adg.keezen.UnitTests.GameStateUtil.*;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static org.junit.jupiter.api.Assertions.*;

import adg.keezen.CardsDeckInterface;
import adg.keezen.GameSession;
import adg.keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that when exactMoveRequired=true, any move that would cause a
 * direction reversal is rejected with CANNOT_MAKE_MOVE. Covers all four
 * reversal sites in ProcessOnMove.
 *
 * <p>Board layout (8 players, playerInt = index):
 *   Section "0" tiles 0-15  →  "1" tiles 0-15  →  ... →  "7" tiles 0-15
 *   Player "1" finish tiles: (1,16)-(1,19)
 *   Last section before player "1"'s finish: section "0"
 */
class ExactMoveRequiredTest {

  private MoveRequest moveMessage;
  private MoveResponse moveResponse;

  private GameSession engine;
  private GameState gameState;
  private CardsDeckInterface cardsDeck;

  @BeforeEach
  void setUp() {
    engine = new GameSession();
    gameState = engine.getGameState();
    cardsDeck = engine.getCardsDeck();
    createGame_With_NPlayers(gameState, 8);
    gameState.setExactMoveRequired(true);
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

  // ── Case 1: forward move blocked by occupied start tile ───────────────────
  // Without exactMoveRequired: pawn hits the blockade and reverses backward.
  // With exactMoveRequired: CANNOT_MAKE_MOVE.

  @Test
  void forwardMove_BlockedByOccupiedStartTile_CannotMove() {
    // pawn at (0,15) + 2 steps → would cross into section 1, but (1,0) is blocked
    // → without setting: reverses to (0,13)
    // GIVEN
    Card card = givePlayerCard(cardsDeck, 0, 2);
    Pawn pawn1 = placePawnOnNest(gameState, "0", new PositionKey("0", 15));
    Pawn blocker = placePawnOnNest(gameState, "1", new PositionKey("1", 0));

    // WHEN
    createMoveRequest(moveMessage, pawn1, card);
    gameState.processOnMove(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertEquals(new PositionKey("0", 15), gameState.getPawn(pawn1).getCurrentTileId());
  }

  @Test
  void forwardMove_BlockedByOccupiedStartTile_LongerDistance_CannotMove() {
    // pawn at (0,14) + 5 steps → would cross into section 1, but (1,0) is blocked
    // → without setting: reverses to (0,11)
    // GIVEN
    Card card = givePlayerCard(cardsDeck, 0, 5);
    Pawn pawn1 = placePawnOnNest(gameState, "0", new PositionKey("0", 14));
    Pawn blocker = placePawnOnNest(gameState, "1", new PositionKey("1", 0));

    // WHEN
    createMoveRequest(moveMessage, pawn1, card);
    gameState.processOnMove(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertEquals(new PositionKey("0", 14), gameState.getPawn(pawn1).getCurrentTileId());
  }

  // ── Case 2: backward move blocked by occupied start tile ─────────────────
  // Without exactMoveRequired: pawn hits the blockade and reverses forward.
  // With exactMoveRequired: CANNOT_MAKE_MOVE.

  @Test
  void backwardMove_BlockedByOccupiedStartTile_CannotMove() {
    // pawn at (1,2) - 4 steps → wants to cross back through (1,0), but it is blocked
    // → without setting: reverses to (1,4)
    // GIVEN
    Card card = givePlayerCard(cardsDeck, 0, -4);
    Pawn pawn1 = placePawnOnNest(gameState, "0", new PositionKey("1", 2));
    Pawn blocker = placePawnOnNest(gameState, "1", new PositionKey("1", 0));

    // WHEN
    createMoveRequest(moveMessage, pawn1, card);
    gameState.processOnMove(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertEquals(new PositionKey("1", 2), gameState.getPawn(pawn1).getCurrentTileId());
  }

  @Test
  void backwardMove_BlockedByOwnOccupiedStartTile_CannotMove() {
    // pawn at (0,3) - 5 steps → wants to cross through (0,0), but player0's own pawn blocks it
    // → without setting: reverses to (0,4)
    // GIVEN
    Card card = givePlayerCard(cardsDeck, 1, -5);
    Pawn blocker = placePawnOnNest(gameState, "0", new PositionKey("0", 0));
    Pawn pawn1 = placePawnOnNest(gameState, "1", new PositionKey("0", 3));

    // WHEN
    createMoveRequest(moveMessage, pawn1, card);
    gameState.processOnMove(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertEquals(new PositionKey("0", 3), gameState.getPawn(pawn1).getCurrentTileId());
  }

  // ── Case 3: entering finish from last section, overshoots and would bounce ─
  // Without exactMoveRequired: pawn bounces off tile 19 and lands somewhere lower.
  // With exactMoveRequired: CANNOT_MAKE_MOVE.

  @Test
  void enteringFinish_Overshoots_CannotMove() {
    // pawn "1" at (0,14) + 8 steps → enters finish but overshoots tile 19
    // → without setting: bounces to (1,16)
    // GIVEN
    Card card = givePlayerCard(cardsDeck, 1, 8);
    Pawn pawn1 = placePawnOnNest(gameState, "1", new PositionKey("0", 14));

    // WHEN
    createMoveRequest(moveMessage, pawn1, card);
    gameState.processOnMove(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertEquals(new PositionKey("0", 14), gameState.getPawn(pawn1).getCurrentTileId());
  }

  @Test
  void enteringFinish_ExactlyAtTile19_IsAllowed() {
    // sanity check: a move that lands exactly on tile 19 has no reversal → still allowed
    // GIVEN
    Card card = givePlayerCard(cardsDeck, 1, 5);
    Pawn pawn1 = placePawnOnNest(gameState, "1", new PositionKey("0", 14));

    // WHEN
    createMoveRequest(moveMessage, pawn1, card);
    gameState.processOnMove(moveMessage, moveResponse);

    // THEN — 14 + 5 = 19, no bounce
    assertNotEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertEquals(new PositionKey("1", 19), gameState.getPawn(pawn1).getCurrentTileId());
  }

  // ── Case 4: pawn already on finish, loosely closed in → ping-pong needed ──
  // Without exactMoveRequired: pawn bounces back and forth between own pawns.
  // With exactMoveRequired: CANNOT_MAKE_MOVE.

  @Test
  void alreadyOnFinish_LooselyClosedIn_PingPongNeeded_CannotMove() {
    // pawn at (1,19) with own pawn at (1,16), -5 steps
    // → without setting: ping-pongs to (1,18)
    // GIVEN
    Card card = givePlayerCard(cardsDeck, 1, -5);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("1", 0), new PositionKey("1", 19));
    Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("1", 1), new PositionKey("1", 16));

    // WHEN
    createMoveRequest(moveMessage, pawn1, card);
    gameState.processOnMove(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertEquals(new PositionKey("1", 19), gameState.getPawn(pawn1).getCurrentTileId());
  }

  @Test
  void alreadyOnFinish_LooselyClosedIn_ForwardPingPongNeeded_CannotMove() {
    // pawn at (1,19) with own pawn at (1,16), +5 steps
    // → without setting: ping-pongs to (1,18)
    // GIVEN
    Card card = givePlayerCard(cardsDeck, 1, 5);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("1", 0), new PositionKey("1", 19));
    Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("1", 1), new PositionKey("1", 16));

    // WHEN
    createMoveRequest(moveMessage, pawn1, card);
    gameState.processOnMove(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertEquals(new PositionKey("1", 19), gameState.getPawn(pawn1).getCurrentTileId());
  }

  // ── Case 5: pawn already on finish, overshoots cap at tile 19 ───────────
  // Without exactMoveRequired: pawn bounces off tile 19 and lands lower.
  // With exactMoveRequired: CANNOT_MAKE_MOVE.

  @Test
  void alreadyOnFinish_OvershootsCap_ForwardMove_CannotMove() {
    // pawn at (1,18) + 3 steps → reaches 19 then bounces to 17
    // → without setting: lands at (1,17)
    // GIVEN
    Card card = givePlayerCard(cardsDeck, 1, 3);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("1", 0), new PositionKey("1", 18));

    // WHEN
    createMoveRequest(moveMessage, pawn1, card);
    gameState.processOnMove(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertEquals(new PositionKey("1", 18), gameState.getPawn(pawn1).getCurrentTileId());
  }

  @Test
  void alreadyOnFinish_OvershootsCap_ExitsFinish_CannotMove() {
    // pawn at (1,19) + 4 steps → bounces off cap and exits finish to (0,15)
    // → without setting: lands at (0,15)
    // GIVEN
    Card card = givePlayerCard(cardsDeck, 1, 4);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("1", 0), new PositionKey("1", 19));

    // WHEN
    createMoveRequest(moveMessage, pawn1, card);
    gameState.processOnMove(moveMessage, moveResponse);

    // THEN
    assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertEquals(new PositionKey("1", 19), gameState.getPawn(pawn1).getCurrentTileId());
  }
}