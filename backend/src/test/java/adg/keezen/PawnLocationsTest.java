package adg.keezen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Direct unit tests for the extracted pawn lookups over a live pawns list. */
class PawnLocationsTest {

  private final List<Pawn> pawns = new ArrayList<>();
  private PawnLocations locations;

  @BeforeEach
  void setUp() {
    pawns.clear();
    locations = new PawnLocations(() -> pawns);
  }

  private Pawn add(String playerId, int pawnNr, int tileNr) {
    PositionKey tile = new PositionKey(playerId, tileNr);
    Pawn pawn = new Pawn(playerId, new PawnId(playerId, pawnNr), tile, tile);
    pawns.add(pawn);
    return pawn;
  }

  // ── withId ──────────────────────────────────────────────────────────────────
  @Test
  void findsAPawnById() {
    Pawn p = add("0", 1, 5);
    assertSame(p, locations.withId(new PawnId("0", 1)));
  }

  @Test
  void returnsNullWhenNoPawnHasThatId() {
    add("0", 1, 5);
    assertNull(locations.withId(new PawnId("0", 2)));
  }

  // ── atTile ──────────────────────────────────────────────────────────────────
  @Test
  void findsThePawnSittingOnATile() {
    Pawn p = add("0", 1, 5);
    assertSame(p, locations.atTile(new PositionKey("0", 5)));
  }

  @Test
  void returnsNullWhenTheTileIsEmpty() {
    add("0", 1, 5);
    assertNull(locations.atTile(new PositionKey("0", 9)));
  }

  // ── moveTo ──────────────────────────────────────────────────────────────────
  @Test
  void movesTheMatchingPawnToTheGivenTile() {
    Pawn onBoard = add("0", 1, 5);
    Pawn move = new Pawn("0", new PawnId("0", 1), new PositionKey("0", 8), new PositionKey("0", -1));
    locations.moveTo(move);
    assertEquals(8, onBoard.getCurrentTileId().getTileNr());
  }

  @Test
  void moveToIsANoOpWhenNoPawnMatches() {
    Pawn onBoard = add("0", 1, 5);
    Pawn ghost = new Pawn("0", new PawnId("0", 9), new PositionKey("0", -1), new PositionKey("0", 8));
    locations.moveTo(ghost); // must not throw
    assertEquals(5, onBoard.getCurrentTileId().getTileNr()); // unchanged
  }
}
