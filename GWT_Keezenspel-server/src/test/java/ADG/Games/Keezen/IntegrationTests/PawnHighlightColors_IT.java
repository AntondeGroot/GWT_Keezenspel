package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getPawnLabelColor;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getStepBoxBorderColor;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.hexToRgbCss;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.pawnIsSelected;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.Player.PawnHighlightColors;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Verifies that after selecting a pawn:
 *  - the pawn SVG highlight color is non-clashing with the pawn's own color
 *  - the TextBoxForPawnSteps border color matches that same highlight
 *  - the pawnLabel text color matches that same highlight
 *
 * Player colors (from PlayerColors.java index order):
 *   player 0 → #A52A2A (brown-red, hue ≈ 0°)  — clashes with RED  → pawn1 highlight: BLUE
 *   player 1 → #0000A5 (dark blue,  hue ≈ 240°) — no clash with RED → pawn1 highlight: RED
 *   player 2 → #008000 (dark green, hue ≈ 120°) — no clash with RED → pawn1 highlight: RED
 *                                                  clashes with GREEN → pawn2 highlight: BLUE
 */
@TestMethodOrder(OrderAnnotation.class)
public class PawnHighlightColors_IT extends BaseIntegrationTest {

  static WebDriver driver;
  static String sessionId;
  static String player0Id;
  static String player1Id;
  static String player2Id;

  @BeforeAll
  static void setUp() {
    sessionId = ApiUtil.createStandardGame();
    driver = getDriver(sessionId);
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    player1Id = ApiUtil.getPlayerid(sessionId, 1);
    player2Id = ApiUtil.getPlayerid(sessionId, 2);
    setPlayerIdPlaying(driver, player0Id);
  }

  @AfterAll
  static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
  }

  // ── pawn1: clashing color falls back to BLUE ──────────────────────────────

  @Test
  @Order(1)
  void brownRedPawn_pawn1HighlightIsBlue_stepBox1AndLabelMatchBlue() {
    // Player 0: #A52A2A (brown-red) clashes with RED → highlight falls back to BLUE
    PawnId pawnId = new PawnId(player0Id, 0);
    driver.findElement(By.id(pawnId.toString())).click();

    String expectedRgb = hexToRgbCss(PawnHighlightColors.BLUE);

    assertTrue(pawnIsSelected(driver, pawnId),                   "SVG highlight opacity=1");
    assertEquals(expectedRgb, getStepBoxBorderColor(driver, 1), "TextBoxForPawnSteps1 border");
    assertEquals(expectedRgb, getPawnLabelColor(driver, 1),      "pawn1Label color");
  }

  // ── pawn1: non-clashing color keeps RED ───────────────────────────────────

  @Test
  @Order(2)
  void darkBluePawn_pawn1HighlightIsRed_stepBox1AndLabelMatchRed() {
    // Player 1: #0000A5 (dark blue) does not clash with RED → highlight stays RED
    setPlayerIdPlaying(driver, player1Id);
    PawnId pawnId = new PawnId(player1Id, 0);
    driver.findElement(By.id(pawnId.toString())).click();

    String expectedRgb = hexToRgbCss(PawnHighlightColors.RED);

    assertTrue(pawnIsSelected(driver, pawnId),                   "SVG highlight opacity=1");
    assertEquals(expectedRgb, getStepBoxBorderColor(driver, 1), "TextBoxForPawnSteps1 border");
    assertEquals(expectedRgb, getPawnLabelColor(driver, 1),      "pawn1Label color");
  }

  // ── pawn2: no pawn2 selected → defaults to GREEN ──────────────────────────

  @Test
  @Order(3)
  void noPawn2Selected_stepBox2DefaultsToGreen() {
    // pawn2Color = null → forPawn2(null) → no clash → returns GREEN
    setPlayerIdPlaying(driver, player0Id);
    driver.findElement(By.id(new PawnId(player0Id, 0).toString())).click();

    String expectedRgb = hexToRgbCss(PawnHighlightColors.GREEN);
    assertEquals(expectedRgb, getStepBoxBorderColor(driver, 2), "TextBoxForPawnSteps2 border");
    assertEquals(expectedRgb, getPawnLabelColor(driver, 2),      "pawn2Label color");
  }

  // ── card 7 split: both pawn1 and pawn2 highlights verified at once ─────────

  @Test
  @Order(4)
  void card7Split_darkGreenPawns_pawn1IsRed_pawn2IsBlue_stepBoxesMatch() {
    // Player 2: #008000 (dark green)
    //   as pawn1 → no clash with RED  → highlight: RED  → step box 1: RED
    //   as pawn2 → clashes with GREEN → highlight: BLUE → step box 2: BLUE
    //
    // Place both pawns directly on the start tile via API.
    ApiUtil.setPawnPosition(sessionId, player2Id, 0, player2Id, 1);
    ApiUtil.setPawnPosition(sessionId, player2Id, 1, player2Id, 2);

    ApiUtil.setCardForPlayer(sessionId, player2Id, 7);
    setPlayerIdPlaying(driver, player2Id); // sets cookie + refreshes

    PawnId pawnA = new PawnId(player2Id, 0);
    PawnId pawnB = new PawnId(player2Id, 1);

    driver.findElement(By.id("card_0_7")).click();          // select card 7
    driver.findElement(By.id(pawnA.toString())).click();    // pawnA → pawn1
    driver.findElement(By.id(pawnB.toString())).click();    // pawnB → pawn2

    String expectedPawn1Rgb = hexToRgbCss(PawnHighlightColors.RED);
    String expectedPawn2Rgb = hexToRgbCss(PawnHighlightColors.BLUE);

    assertTrue(pawnIsSelected(driver, pawnA),                        "pawn1 SVG highlight");
    assertTrue(pawnIsSelected(driver, pawnB),                        "pawn2 SVG highlight");
    assertEquals(expectedPawn1Rgb, getStepBoxBorderColor(driver, 1), "TextBoxForPawnSteps1 border");
    assertEquals(expectedPawn1Rgb, getPawnLabelColor(driver, 1),      "pawn1Label color");
    assertEquals(expectedPawn2Rgb, getStepBoxBorderColor(driver, 2), "TextBoxForPawnSteps2 border");
    assertEquals(expectedPawn2Rgb, getPawnLabelColor(driver, 2),      "pawn2Label color");
  }

  // ── deselecting card does not remove pawn highlight ───────────────────────

  @Test
  @Order(5)
  void deselectCard_doesNotRemovePawnHighlight() {
    // GIVEN: player 0's pawn on the board, with a non-special card
    ApiUtil.setPawnPosition(sessionId, player0Id, 0, player0Id, 1);
    ApiUtil.setCardForPlayer(sessionId, player0Id, 5);
    setPlayerIdPlaying(driver, player0Id);
    waitUntilCardsAreLoaded(driver);

    PawnId pawnId = new PawnId(player0Id, 0);
    driver.findElement(By.id(pawnId.toString())).click(); // select pawn
    driver.findElement(By.id("card_0_5")).click();        // select card

    // WHEN: deselect card by clicking it again
    driver.findElement(By.id("card_0_5")).click();

    // THEN: pawn highlight must still be visible
    assertTrue(pawnIsSelected(driver, pawnId), "Pawn highlight must remain after card is deselected");
  }

}