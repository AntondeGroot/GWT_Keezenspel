package adg.keezen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Direct unit tests for the extracted board-reachability rules. The only collaborator is a
 * tile→pawn lookup, supplied here by a plain map, so every branch is reachable without a game.
 */
class TileReachabilityTest {

  private final Map<PositionKey, Pawn> board = new HashMap<>();
  private TileReachability rules;

  private static final Pawn SELF = pawn("0", 1);

  @BeforeEach
  void setUp() {
    board.clear();
    rules = new TileReachability(board::get, pid -> pid);
  }

  private static Pawn pawn(String playerId, int pawnNr) {
    PositionKey nest = new PositionKey(playerId, -1);
    return new Pawn(playerId, new PawnId(playerId, pawnNr), nest, nest);
  }

  private void place(Pawn p, PositionKey tile) {
    board.put(tile, p);
  }

  // ── canMoveToTile ───────────────────────────────────────────────────────────
  @Test
  void cannotMovePastTile19() {
    assertFalse(rules.canMoveToTile(SELF, new PositionKey("0", 20)));
  }

  @Test
  void canMoveOntoAnEmptyTile() {
    assertTrue(rules.canMoveToTile(SELF, new PositionKey("0", 5)));
  }

  @Test
  void canMoveOntoOwnCurrentPosition() {
    PositionKey tile = new PositionKey("0", 5);
    place(SELF, tile); // the selected pawn itself sits there
    assertTrue(rules.canMoveToTile(SELF, tile));
  }

  @Test
  void cannotMoveOntoAnotherOwnPawn() {
    PositionKey tile = new PositionKey("0", 5);
    place(pawn("0", 2), tile); // same player, different pawn
    assertFalse(rules.canMoveToTile(SELF, tile));
  }

  @Test
  void cannotMoveOntoAnOpponentBlockadeOnItsStartTile() {
    PositionKey start = new PositionKey("1", 0);
    place(pawn("1", 0), start); // opponent on its own start tile 0
    assertFalse(rules.canMoveToTile(SELF, start));
  }

  @Test
  void canCaptureAnOpponentThatIsNotOnItsStartTile() {
    PositionKey tile = new PositionKey("0", 5);
    place(pawn("1", 0), tile); // opponent sitting on a normal tile
    assertTrue(rules.canMoveToTile(SELF, tile));
  }

  // ── cannotMoveToTileBecauseSamePlayer ───────────────────────────────────────
  @Test
  void notBlockedBySamePlayerWhenTileIsEmpty() {
    assertFalse(rules.cannotMoveToTileBecauseSamePlayer(SELF, new PositionKey("0", 5)));
  }

  @Test
  void blockedBySamePlayersOtherPawn() {
    PositionKey tile = new PositionKey("0", 5);
    place(pawn("0", 2), tile);
    assertTrue(rules.cannotMoveToTileBecauseSamePlayer(SELF, tile));
  }

  @Test
  void notBlockedBySamePlayerBySelf() {
    PositionKey tile = new PositionKey("0", 5);
    place(SELF, tile);
    assertFalse(rules.cannotMoveToTileBecauseSamePlayer(SELF, tile));
  }

  @Test
  void notBlockedBySamePlayerByAnOpponent() {
    PositionKey tile = new PositionKey("0", 5);
    place(pawn("1", 0), tile);
    assertFalse(rules.cannotMoveToTileBecauseSamePlayer(SELF, tile));
  }

  // ── canPassStartTile ────────────────────────────────────────────────────────
  @Test
  void canPassAnEmptyStartTile() {
    assertTrue(rules.canPassStartTile(SELF, new PositionKey("1", 0)));
  }

  @Test
  void canPassAStartTileOccupiedBySelf() {
    PositionKey tile = new PositionKey("0", 0);
    place(SELF, tile);
    assertTrue(rules.canPassStartTile(SELF, tile));
  }

  @Test
  void cannotPassAStartTileHeldByItsOwner() {
    PositionKey start = new PositionKey("1", 0);
    place(pawn("1", 0), start); // owner sits on its own start
    assertFalse(rules.canPassStartTile(SELF, start));
  }

  @Test
  void canPassAStartTileHeldByANonOwner() {
    PositionKey start = new PositionKey("1", 0);
    place(pawn("2", 0), start); // some other player's pawn parked there
    assertTrue(rules.canPassStartTile(SELF, start));
  }

  // ── tileIsABlockade ─────────────────────────────────────────────────────────
  @Test
  void emptyTileIsNotABlockade() {
    assertFalse(rules.tileIsABlockade(new PositionKey("1", 0)));
  }

  @Test
  void ownersPawnOnItsStartIsABlockade() {
    PositionKey start = new PositionKey("1", 0);
    place(pawn("1", 0), start);
    assertTrue(rules.tileIsABlockade(start));
  }

  @Test
  void anotherPlayersPawnOnAStartIsNotABlockade() {
    PositionKey start = new PositionKey("1", 0);
    place(pawn("2", 0), start);
    assertFalse(rules.tileIsABlockade(start));
  }

  // ── isPawnLooselyClosedIn ───────────────────────────────────────────────────
  @Test
  void notLooselyClosedInBelowTheFinish() {
    assertFalse(rules.isPawnLooselyClosedIn(SELF, new PositionKey("0", 16)));
  }

  @Test
  void notLooselyClosedInWhenTheFinishPathIsClear() {
    assertFalse(rules.isPawnLooselyClosedIn(SELF, new PositionKey("0", 18)));
  }

  @Test
  void looselyClosedInWhenBlockedInsideTheFinish() {
    place(pawn("0", 2), new PositionKey("0", 17)); // own pawn blocks tile 17
    assertTrue(rules.isPawnLooselyClosedIn(SELF, new PositionKey("0", 18)));
  }

  // ── isPawnTightlyClosedIn ───────────────────────────────────────────────────
  @Test
  void tightlyClosedInOn19WhenTile18IsBlocked() {
    place(pawn("0", 2), new PositionKey("0", 18));
    assertTrue(rules.isPawnTightlyClosedIn(SELF, new PositionKey("0", 19)));
  }

  @Test
  void tightlyClosedInOn18WhenBothNeighboursAreBlocked() {
    place(pawn("0", 2), new PositionKey("0", 19));
    place(pawn("0", 3), new PositionKey("0", 17));
    assertTrue(rules.isPawnTightlyClosedIn(SELF, new PositionKey("0", 18)));
  }

  @Test
  void tightlyClosedInOn17WhenBothNeighboursAreBlocked() {
    place(pawn("0", 2), new PositionKey("0", 18));
    place(pawn("0", 3), new PositionKey("0", 16));
    assertTrue(rules.isPawnTightlyClosedIn(SELF, new PositionKey("0", 17)));
  }

  @Test
  void notTightlyClosedInWhenNeighboursAreFree() {
    assertFalse(rules.isPawnTightlyClosedIn(SELF, new PositionKey("0", 18)));
  }

  // ── checkHighestTileNrYouCanMoveTo ──────────────────────────────────────────
  @Test
  void advancesAllStepsWhenThePathIsClear() {
    assertEquals(9, rules.checkHighestTileNrYouCanMoveTo(SELF, new PositionKey("0", 5), 4));
  }

  @Test
  void stopsOneBeforeABlockedTile() {
    place(pawn("0", 2), new PositionKey("0", 8)); // blocks tile 8
    assertEquals(7, rules.checkHighestTileNrYouCanMoveTo(SELF, new PositionKey("0", 5), 5));
  }

  @Test
  void movesBackwardForNegativeSteps() {
    assertEquals(7, rules.checkHighestTileNrYouCanMoveTo(SELF, new PositionKey("0", 10), -3));
  }

  // ── finish-lane bounce (pingpongMove / moveAndCheckEveryTile) ────────────────
  private static PositionKey tile(int nr) {
    return new PositionKey("0", nr);
  }

  @Test
  void moveAndCheckEveryTile_overshootingTheFinishEndBouncesBack() {
    // From tile 18, +3 would reach 21; it bounces off the finish end (past 19): 18→19→(bounce)→17.
    assertEquals(tile(17), rules.moveAndCheckEveryTile(SELF, tile(18), 3));
  }

  @Test
  void moveAndCheckEveryTile_onlyBouncesInsideTheFinish_notOnBoardTiles() {
    // A blocked BOARD tile (15) must not cause a bounce — only finish tiles (16-19) do. So a pawn
    // on 13 moving +3 passes straight through the occupied 15 and lands on 16.
    place(pawn("0", 2), tile(15));
    assertEquals(tile(16), rules.moveAndCheckEveryTile(SELF, tile(13), 3));
  }

  @Test
  void pingpongMove_zeroSteps_returnsJustTheCurrentTileWithoutDuplicate() {
    List<PositionKey> path = rules.pingpongMove(SELF, tile(17), 0);
    assertEquals(List.of(tile(17)), path);
  }

  @Test
  void pingpongMove_overshootingTheFinishEndBounces() {
    // From 18, +3 climbs to 19, bounces off the end, and comes back to 17.
    List<PositionKey> path = rules.pingpongMove(SELF, tile(18), 3);
    assertEquals(List.of(18, 19, 17), path.stream().map(PositionKey::getTileNr).toList());
  }
}
