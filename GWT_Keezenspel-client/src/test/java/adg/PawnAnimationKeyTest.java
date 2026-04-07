package adg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import adg.keezen.TileId;
import adg.keezen.dto.PawnClient;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests the pawn ID key format used in the animation element lookup.
 *
 * <p>The full CSS animation (DivElement transitions, GWT Timer) runs only in a browser and cannot
 * be unit-tested. But the regression that broke animation — using a GWT JavaScriptObject
 * (PawnIdDTO) as a HashMap key instead of a plain String — can be verified here.
 *
 * <p>PawnAnimation.animateSequence() looks up the DOM element for a pawn via:
 * pawnElements.get(pawnClient.getPawnId()) The map is populated with keys in the format
 * "playerId_pawnNr" (e.g. "2_0"). These tests verify that PawnClient produces the correct String
 * key so the lookup always succeeds.
 */
public class PawnAnimationKeyTest {

  @Test
  public void pawnId_hasCorrectFormat() {
    PawnClient pawn = new PawnClient("2", 0, new TileId("2", 5));

    assertEquals("2_0", pawn.getPawnId());
  }

  @Test
  public void pawnId_usesPlayerIdAndPawnNr() {
    PawnClient pawn = new PawnClient("3", 2, new TileId("3", 1));

    assertEquals("3_2", pawn.getPawnId());
  }

  @Test
  public void pawnElementLookup_succeedsWithStringKey() {
    // Simulate how pawnElements map is populated by the UI (keyed with "playerId_pawnNr")
    Map<String, String> pawnElements = new HashMap<>();
    pawnElements.put("1_0", "element-for-player1-pawn0");
    pawnElements.put("2_1", "element-for-player2-pawn1");

    PawnClient pawn = new PawnClient("1", 0, new TileId("1", 3));

    // This is the exact lookup in PawnAnimation.animateSequence() — must not return null
    String element = pawnElements.get(pawn.getPawnId());

    assertNotNull(element, "Element lookup must succeed; returning null means no animation runs");
    assertEquals("element-for-player1-pawn0", element);
  }

  @Test
  public void pawnElementLookup_returnsNull_whenPawnNotInMap() {
    Map<String, String> pawnElements = new HashMap<>();
    pawnElements.put("1_0", "element-for-player1-pawn0");

    PawnClient unknownPawn = new PawnClient("4", 3, new TileId("4", 0));

    assertNull(pawnElements.get(unknownPawn.getPawnId()));
  }
}