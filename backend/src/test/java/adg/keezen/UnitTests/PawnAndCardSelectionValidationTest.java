package adg.keezen.UnitTests;

import static org.junit.jupiter.api.Assertions.*;

import adg.util.PawnAndCardSelectionValidation;
import adg.util.SelectionValidation;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveType;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import org.junit.jupiter.api.Test;

class PawnAndCardSelectionValidationTest {

  // ── Helpers ──────────────────────────────────────────────────────────────

  private static Pawn pawnInNest(String playerId) {
    return new Pawn(playerId, new PawnId(), new PositionKey(playerId, -1), new PositionKey(playerId, -1));
  }

  private static Pawn pawnOnBoard(String playerId, int tileNr) {
    return new Pawn(playerId, new PawnId(), new PositionKey(playerId, tileNr), new PositionKey(playerId, -1));
  }

  /** A pawn owned by {@code playerId} but standing on {@code tileOwner}'s section tile. */
  private static Pawn pawnOnTileOf(String playerId, String tileOwner, int tileNr) {
    return new Pawn(playerId, new PawnId(), new PositionKey(tileOwner, tileNr), new PositionKey(playerId, -1));
  }

  private static Card card(int value) {
    return new Card(0, value, 0);
  }

  // ── Null guards ───────────────────────────────────────────────────────────

  @Test
  void bothPawnsNull_isInvalid() {
    assertFalse(PawnAndCardSelectionValidation.validate(null, null, card(5)).isValid());
  }

  @Test
  void cardNull_isInvalid() {
    assertFalse(PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 3), null, null).isValid());
  }

  // ── ON_BOARD ──────────────────────────────────────────────────────────────

  @Test
  void pawn1InNest_kingCard_isOnBoard() {
    SelectionValidation result = PawnAndCardSelectionValidation.validate(pawnInNest("p1"), null, card(13));
    assertTrue(result.isValid());
    assertEquals(MoveType.ON_BOARD, result.getMoveType());
  }

  @Test
  void pawn1InNest_aceCard_isOnBoard() {
    SelectionValidation result = PawnAndCardSelectionValidation.validate(pawnInNest("p1"), null, card(1));
    assertTrue(result.isValid());
    assertEquals(MoveType.ON_BOARD, result.getMoveType());
  }

  @Test
  void pawn1InNest_regularCard_isInvalid() {
    assertFalse(PawnAndCardSelectionValidation.validate(pawnInNest("p1"), null, card(5)).isValid());
  }

  @Test
  void pawn1InNest_sevenCard_noSecondPawn_isInvalid() {
    assertFalse(PawnAndCardSelectionValidation.validate(pawnInNest("p1"), null, card(7)).isValid());
  }

  // ── SPLIT ─────────────────────────────────────────────────────────────────

  @Test
  void bothPawnsOnBoard_samePlayer_sevenCard_isSplit() {
    Pawn p1 = pawnOnBoard("p1", 3);
    Pawn p2 = pawnOnBoard("p1", 8);
    SelectionValidation result = PawnAndCardSelectionValidation.validate(p1, p2, card(7));
    assertTrue(result.isValid());
    assertEquals(MoveType.SPLIT, result.getMoveType());
  }

  @Test
  void bothPawnsOnBoard_differentPlayers_sevenCard_isInvalid() {
    // split requires same player; two pawns selected with no valid split/switch is always invalid
    Pawn p1 = pawnOnBoard("p1", 3);
    Pawn p2 = pawnOnBoard("p2", 8);
    assertFalse(PawnAndCardSelectionValidation.validate(p1, p2, card(7)).isValid());
  }

  // ── SWITCH ────────────────────────────────────────────────────────────────

  @Test
  void bothPawnsOnNormalBoard_jackCard_isSwitch() {
    Pawn p1 = pawnOnBoard("p1", 3);
    Pawn p2 = pawnOnBoard("p2", 8);
    SelectionValidation result = PawnAndCardSelectionValidation.validate(p1, p2, card(11));
    assertTrue(result.isValid());
    assertEquals(MoveType.SWITCH, result.getMoveType());
  }

  @Test
  void pawn2OnItsOwnStartTile_jackCard_isClassifiedAsSwitch() {
    // The gate only classifies the selection structurally. A pawn on its OWN start tile is still
    // a switch-shaped selection; the protection rule ("can't take an opponent off its own start")
    // is enforced downstream in ProcessOnSwitch (see MovingOnSwitchTest.cantTakeOtherPawnFromStart),
    // which rejects it with a specific reason rather than a bare 400.
    Pawn p1 = pawnOnBoard("p1", 3);
    Pawn p2 = pawnOnBoard("p2", 0); // tile owner == pawn owner == p2
    SelectionValidation result = PawnAndCardSelectionValidation.validate(p1, p2, card(11));
    assertTrue(result.isValid());
    assertEquals(MoveType.SWITCH, result.getMoveType());
  }

  @Test
  void pawn2OnAnotherPlayersStartTile_jackCard_isSwitch() {
    // a pawn sitting on a DIFFERENT player's start tile (also tile 0) is not protected:
    // e.g. red on blue's start point can be switched. Regression test.
    Pawn p1 = pawnOnBoard("p1", 3);
    Pawn p2 = pawnOnTileOf("p2", "p3", 0); // p2's pawn standing on p3's start tile
    SelectionValidation result = PawnAndCardSelectionValidation.validate(p1, p2, card(11));
    assertTrue(result.isValid());
    assertEquals(MoveType.SWITCH, result.getMoveType());
  }

  @Test
  void pawn2OnFinishTile_jackCard_isInvalid() {
    // finish tiles are > 15, switch only works on normal board (1-15)
    Pawn p1 = pawnOnBoard("p1", 3);
    Pawn p2 = pawnOnBoard("p2", 16);
    assertFalse(PawnAndCardSelectionValidation.validate(p1, p2, card(11)).isValid());
  }

  @Test
  void pawn1OnFinishTile_jackCard_isInvalid() {
    Pawn p1 = pawnOnBoard("p1", 16);
    Pawn p2 = pawnOnBoard("p2", 5);
    assertFalse(PawnAndCardSelectionValidation.validate(p1, p2, card(11)).isValid());
  }

  // ── pawn1 null (only pawn2 provided) ─────────────────────────────────────

  @Test
  void pawn1Null_pawn2OnBoard_isInvalid() {
    assertFalse(PawnAndCardSelectionValidation.validate(null, pawnOnBoard("p1", 5), card(5)).isValid());
  }

  // ── MOVE ──────────────────────────────────────────────────────────────────

  @Test
  void pawn1OnBoard_regularCard_isMove() {
    SelectionValidation result = PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 5), null, card(5));
    assertTrue(result.isValid());
    assertEquals(MoveType.MOVE, result.getMoveType());
  }

  @Test
  void pawn1OnBoard_twoCard_isMove() {
    SelectionValidation result = PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 5), null, card(2));
    assertTrue(result.isValid());
    assertEquals(MoveType.MOVE, result.getMoveType());
  }

  // ── fallthrough INVALID ───────────────────────────────────────────────────

  @Test
  void pawn1OnBoard_kingCard_noPawn2_isInvalid() {
    // king is only valid when the pawn is in the nest
    assertFalse(PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 5), null, card(13)).isValid());
  }

  @Test
  void pawn1OnBoard_jackCard_noPawn2_isInvalid() {
    // jack requires a second pawn to switch with
    assertFalse(PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 5), null, card(11)).isValid());
  }

  // ── boundary cases: pin the exact tile thresholds (nest = -1, board = 0..15) ──

  @Test
  void pawn1OnStartTile0_kingCard_isInvalid_notOnBoard() {
    // tile 0 is on the board, not the nest, so King is not an "onto the board" move
    assertFalse(PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 0), null, card(13)).isValid());
  }

  @Test
  void split_pawn1OnStartTile0_isSplit() {
    SelectionValidation r =
        PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 0), pawnOnBoard("p1", 5), card(7));
    assertEquals(MoveType.SPLIT, r.getMoveType());
  }

  @Test
  void split_pawn2OnStartTile0_isSplit() {
    SelectionValidation r =
        PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 5), pawnOnBoard("p1", 0), card(7));
    assertEquals(MoveType.SPLIT, r.getMoveType());
  }

  @Test
  void switch_pawn1OnStartTile0_isSwitch() {
    SelectionValidation r =
        PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 0), pawnOnBoard("p2", 5), card(11));
    assertEquals(MoveType.SWITCH, r.getMoveType());
  }

  @Test
  void switch_pawn1OnLastBoardTile15_isSwitch() {
    SelectionValidation r =
        PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 15), pawnOnBoard("p2", 5), card(11));
    assertEquals(MoveType.SWITCH, r.getMoveType());
  }

  @Test
  void switch_pawn2OnStartTile0_isSwitch() {
    SelectionValidation r =
        PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 5), pawnOnBoard("p2", 0), card(11));
    assertEquals(MoveType.SWITCH, r.getMoveType());
  }

  @Test
  void switch_pawn2OnLastBoardTile15_isSwitch() {
    SelectionValidation r =
        PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 5), pawnOnBoard("p2", 15), card(11));
    assertEquals(MoveType.SWITCH, r.getMoveType());
  }

  @Test
  void move_pawn1OnStartTile0_isMove() {
    SelectionValidation r =
        PawnAndCardSelectionValidation.validate(pawnOnBoard("p1", 0), null, card(5));
    assertEquals(MoveType.MOVE, r.getMoveType());
  }
}