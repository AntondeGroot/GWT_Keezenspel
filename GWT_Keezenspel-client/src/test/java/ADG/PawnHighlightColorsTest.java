package ADG;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.Player.PawnHighlightColors;
import org.junit.jupiter.api.Test;

class PawnHighlightColorsTest {

  // ── computeHue ────────────────────────────────────────────────────────────

  @Test
  void computeHue_pureRed_returns0() {
    double hue = PawnHighlightColors.computeHue(new int[]{255, 0, 0});
    assertEquals(0.0, hue, 1.0);
  }

  @Test
  void computeHue_pureGreen_returns120() {
    double hue = PawnHighlightColors.computeHue(new int[]{0, 255, 0});
    assertEquals(120.0, hue, 1.0);
  }

  @Test
  void computeHue_pureBlue_returns240() {
    double hue = PawnHighlightColors.computeHue(new int[]{0, 0, 255});
    assertEquals(240.0, hue, 1.0);
  }

  @Test
  void computeHue_achromatic_returns0() {
    // r == g == b → delta == 0
    double hue = PawnHighlightColors.computeHue(new int[]{128, 128, 128});
    assertEquals(0.0, hue, 0.0);
  }

  // ── colorsClash ───────────────────────────────────────────────────────────

  @Test
  void colorsClash_nullPawnColor_returnsFalse() {
    assertFalse(PawnHighlightColors.colorsClash(null, PawnHighlightColors.RED));
  }

  @Test
  void colorsClash_sameHue_returnsTrue() {
    // Both are clearly red — hue diff ≈ 0 < 40
    assertFalse(PawnHighlightColors.colorsClash("#ff0000", "#00ff00")); // red vs green: ~120° apart
    assertTrue(PawnHighlightColors.colorsClash("#ff0000", "#ff2200"));  // red vs orange-red: ~5° apart
  }

  @Test
  void colorsClash_redPawnVsRedHighlight_clashes() {
    // Pure red pawn vs RED highlight (#ef5350 ≈ hue 1°) — same hue, should clash
    assertTrue(PawnHighlightColors.colorsClash("#ff0000", PawnHighlightColors.RED));
  }

  @Test
  void colorsClash_greenPawnVsGreenHighlight_clashes() {
    // Pure green pawn vs GREEN highlight (#66bb6a ≈ hue 123°) — same hue, should clash
    assertTrue(PawnHighlightColors.colorsClash("#00ff00", PawnHighlightColors.GREEN));
  }

  @Test
  void colorsClash_bluePawnVsRedHighlight_doesNotClash() {
    // Pure blue (240°) vs RED (~1°) — diff ≈ 121° > 40, no clash
    assertFalse(PawnHighlightColors.colorsClash("#0000ff", PawnHighlightColors.RED));
  }

  @Test
  void colorsClash_orangeRedPawnVsRedHighlight_clashes() {
    // #ffaa00 is orange-red, hue ≈ 40°; diff from RED (≈1°) is ~39° < 40 → clashes
    assertTrue(PawnHighlightColors.colorsClash("#ffaa00", PawnHighlightColors.RED));
  }

  @Test
  void colorsClash_yellowPawnVsRedHighlight_doesNotClash() {
    // Pure yellow #ffff00, hue = 60°; diff from RED (≈1°) is ~59° > 40 → no clash
    assertFalse(PawnHighlightColors.colorsClash("#ffff00", PawnHighlightColors.RED));
  }

  @Test
  void colorsClash_wrapAroundHue_detected() {
    // Red hue is near 0°/360°. A pawn at 340° (red-purple) is only 20° from 0° via wrap-around.
    // #cc0044 is around hue 340°
    assertTrue(PawnHighlightColors.colorsClash("#cc0044", PawnHighlightColors.RED));
  }

  // ── forPawn1 ──────────────────────────────────────────────────────────────

  @Test
  void forPawn1_redPawn_returnsBlue() {
    // Red pawn clashes with RED highlight → fallback to BLUE
    assertEquals(PawnHighlightColors.BLUE, PawnHighlightColors.forPawn1("#ff0000"));
  }

  @Test
  void forPawn1_bluePawn_returnsRed() {
    // Blue pawn does not clash with RED → prefers RED
    assertEquals(PawnHighlightColors.RED, PawnHighlightColors.forPawn1("#0000ff"));
  }

  @Test
  void forPawn1_greenPawn_returnsRed() {
    // Green pawn does not clash with RED (hue diff ~119°) → prefers RED
    assertEquals(PawnHighlightColors.RED, PawnHighlightColors.forPawn1("#00ff00"));
  }

  // ── forPawn2 ──────────────────────────────────────────────────────────────

  @Test
  void forPawn2_greenPawn_returnsBlue() {
    // Green pawn clashes with GREEN highlight → fallback to BLUE
    assertEquals(PawnHighlightColors.BLUE, PawnHighlightColors.forPawn2("#00ff00"));
  }

  @Test
  void forPawn2_redPawn_returnsGreen() {
    // Red pawn does not clash with GREEN (hue diff ~119°) → prefers GREEN
    assertEquals(PawnHighlightColors.GREEN, PawnHighlightColors.forPawn2("#ff0000"));
  }

  @Test
  void forPawn2_bluePawn_returnsGreen() {
    // Blue pawn does not clash with GREEN (hue diff ~117°) → prefers GREEN
    assertEquals(PawnHighlightColors.GREEN, PawnHighlightColors.forPawn2("#0000ff"));
  }

  // ── player colors from the game ───────────────────────────────────────────

  @Test
  void forPawn1_brownPlayerColor_returnsRed() {
    // #A52A2A (player 0, brown-red) — hue ≈ 0°, clashes with RED → BLUE
    assertEquals(PawnHighlightColors.BLUE, PawnHighlightColors.forPawn1("#A52A2A"));
  }

  @Test
  void forPawn1_darkBluePlayerColor_returnsRed() {
    // #0000A5 (player 1, dark blue) — hue ≈ 240°, no clash with RED → RED
    assertEquals(PawnHighlightColors.RED, PawnHighlightColors.forPawn1("#0000A5"));
  }

  @Test
  void forPawn2_darkGreenPlayerColor_returnsBlue() {
    // #008000 (player 2, dark green) — hue ≈ 120°, clashes with GREEN → BLUE
    assertEquals(PawnHighlightColors.BLUE, PawnHighlightColors.forPawn2("#008000"));
  }

  @Test
  void forPawn2_darkBluePlayerColor_returnsGreen() {
    // #0000A5 (player 1, dark blue) — hue ≈ 240°, no clash with GREEN → GREEN
    assertEquals(PawnHighlightColors.GREEN, PawnHighlightColors.forPawn2("#0000A5"));
  }
}