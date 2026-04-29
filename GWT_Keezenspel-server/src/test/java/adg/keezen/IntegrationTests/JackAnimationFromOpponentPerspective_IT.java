package adg.keezen.IntegrationTests;

import static adg.keezen.IntegrationTests.Utils.TestUtils.assertPointsEqual;
import static adg.keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static adg.keezen.IntegrationTests.Utils.TestUtils.getPawnLocation;
import static adg.keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilPawnStopsMoving;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.IntegrationTests.Utils.TestUtils;
import adg.keezen.Point;
import adg.keezen.player.PawnId;
import adg.keezen.utils.ApiCallsHelper;
import adg.keezen.utils.BaseIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

/**
 * Verifies that the jack (switch) animation plays correctly from the perspective of the
 * non-acting player.
 *
 * <p>Scenario:
 *
 * <ul>
 *   <li>Player 1's pawn 0 is on tile 4 of player 1's board section.
 *   <li>Player 0's pawn 0 is on tile 2 of player 1's board section (2 tiles behind on the path).
 *   <li>It is player 0's turn.
 *   <li>The Selenium browser is viewing the game as player 1.
 *   <li>Player 0 plays the jack card via a direct API call (not through the browser UI).
 *   <li>The browser receives the state change via GWT's polling cycle and plays the animation.
 *   <li>After both pawns stop moving, their screen positions must be swapped.
 * </ul>
 */
public class JackAnimationFromOpponentPerspective_IT extends BaseIntegrationTest {

  private static WebDriver driver;
  private static String sessionId;
  private static String player0Id;
  private static String player1Id;
  private static PawnId pawn0;
  private static PawnId pawn1;

  private static final ApiCallsHelper apiHelper = new ApiCallsHelper();

  @BeforeAll
  static void setUp() {
    sessionId = ApiUtil.createStandardGame();
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    player1Id = ApiUtil.getPlayerid(sessionId, 1);
    pawn0 = new PawnId(player0Id, 0);
    pawn1 = new PawnId(player1Id, 0);

    // Place both pawns on the main board so the jack switch is valid.
    ApiUtil.setPawnPosition(sessionId, player1Id, 0, player1Id, 4);
    ApiUtil.setPawnPosition(sessionId, player0Id, 0, player1Id, 2);

    // Give player 0 a jack card. The test API sets uuid == cardValue, so cardId == 11.
    ApiUtil.setCardForPlayer(sessionId, player0Id, 11);

    // Open the browser as player 1 — the observer, not the acting player.
    driver = getDriver(sessionId);
    setPlayerIdPlaying(driver, player1Id);
  }

  @AfterAll
  public static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  void jackSwitch_asObserver_pawnPositionsAreSwappedAfterAnimation() {
    // GIVEN: the page is loaded and both pawn starting positions are recorded
    waitUntilCardsAreLoaded(driver);
    Point initialPawn0Position = getPawnLocation(driver, pawn0);
    Point initialPawn1Position = getPawnLocation(driver, pawn1);

    // WHEN: player 0 plays the jack card via API (switch move — not through the browser UI).
    // The server validates the card and pawns automatically and determines the SWITCH move type.
    Map<String, Object> switchRequest = Map.of(
        "playerId", player0Id,
        "cardId", 11,
        "pawn1Id", Map.of("playerId", player0Id, "pawnNr", 0),
        "pawn2Id", Map.of("playerId", player1Id, "pawnNr", 0)
    );
    apiHelper.makeMove(sessionId, player0Id, switchRequest);

    // Allow the GWT client's polling cycle (~1-2 s) to pick up the updated game state
    // and begin the animation before we start waiting for it to stop.
    TestUtils.wait(2000);

    // Wait for both pawns to finish animating (or time out after 6 s each).
    waitUntilPawnStopsMoving(driver, pawn0);
    waitUntilPawnStopsMoving(driver, pawn1);

    // THEN: each pawn must occupy the other's original screen position.
    Point finalPawn0Position = getPawnLocation(driver, pawn0);
    Point finalPawn1Position = getPawnLocation(driver, pawn1);

    assertPointsEqual(
        "Player 0's pawn should have animated to player 1's original tile",
        initialPawn1Position, finalPawn0Position);
    assertPointsEqual(
        "Player 1's pawn should have animated to player 0's original tile",
        initialPawn0Position, finalPawn1Position);
  }
}