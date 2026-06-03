package adg.keezen.IntegrationTests;

import static adg.keezen.IntegrationTests.Utils.TestUtils.getMobileDriver;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.utils.BaseIntegrationTest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Verifies that on the mobile layout the three key UI regions —
 * the board canvas, the player list, and the action buttons —
 * are all fully visible and do not overlap one another.
 *
 * <p>Runs once for each player count from 2 to 8 players.
 */
class MobileLayoutCheck_IT extends BaseIntegrationTest {

  private WebDriver driver;

  @BeforeEach
  void setUp() {
    driver = null;
  }

  @AfterEach
  void tearDown() {
    if (driver != null) driver.quit();
  }

  @ParameterizedTest(name = "{0} players")
  @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8})
  void canvasPlayerListAndButtons_noOverlapAndFullyVisible(int nPlayers) {
    String sessionId = ApiUtil.createNPlayerGame(nPlayers);
    String playerId  = ApiUtil.getPlayerid(sessionId, 0);

    driver = getMobileDriver(sessionId, playerId);
    waitUntilCardsAreLoaded(driver);
    waitUntilMobileGridReady(driver);

    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) ((JavascriptExecutor) driver)
        .executeScript(
            "return window.testLayout([" +
            "  'canvasCards2'," +
            "  '.playerListContainer'," +
            "  'mobile-button-bar'" +
            "]);");

    Boolean pass = (Boolean) result.get("pass");
    @SuppressWarnings("unchecked")
    List<String> violations = (List<String>) result.get("violations");

    assertTrue(pass,
        nPlayers + "-player mobile layout violations:\n  " +
        String.join("\n  ", violations));
  }

  /**
   * With 8 players the player list should overflow its container (scrollable),
   * so the button bar must be shifted left to avoid overlapping it.
   *
   * <p>Sequence verified:
   * <ol>
   *   <li>Wait for cards (GWT board rendered)</li>
   *   <li>Wait for player list rows to appear (SSE push received)</li>
   *   <li>Allow one animation frame for positionButtonContainer() to run</li>
   *   <li>Assert bar right edge is to the left of the player list left edge</li>
   *   <li>Assert bar is not at the screen's far left (is reasonably centred)</li>
   * </ol>
   */
  @Test
  void buttonBar_shiftedLeft_withEightPlayersAndNoOverlap() throws InterruptedException {
    String sessionId = ApiUtil.createNPlayerGame(8);
    String playerId  = ApiUtil.getPlayerid(sessionId, 0);

    driver = getMobileDriver(sessionId, playerId);
    waitUntilCardsAreLoaded(driver);

    /* Position is set by CSS (left:384px), no timing dependency.
       Still wait for the player list so the layout is fully rendered. */
    new WebDriverWait(driver, Duration.ofSeconds(10))
        .until(d -> (Boolean) ((JavascriptExecutor) d).executeScript(
            "var pl = document.querySelector('.playerListContainer');" +
            "return pl != null && pl.querySelectorAll('tr').length > 0;"));

    @SuppressWarnings("unchecked")
    Map<String, Object> pos = (Map<String, Object>) ((JavascriptExecutor) driver)
        .executeScript(
            "var bar = document.getElementById('mobile-button-bar');" +
            "var pl  = document.querySelector('.playerListContainer');" +
            "if (!bar || !pl) return null;" +
            "var b = bar.getBoundingClientRect();" +
            "var p = pl.getBoundingClientRect();" +
            "return { barLeft: b.left, barRight: b.right, plLeft: p.left };");

    assertNotNull(pos, "#mobile-button-bar or .playerListContainer not found");

    double barLeft  = ((Number) pos.get("barLeft")).doubleValue();
    double barRight = ((Number) pos.get("barRight")).doubleValue();
    double plLeft   = ((Number) pos.get("plLeft")).doubleValue();

    assertTrue(barRight <= plLeft,
        "Button bar right (" + (int) barRight + " px) must not overlap " +
        "player list left (" + (int) plLeft + " px)");
    assertTrue(barLeft > 20,
        "Button bar must be centred in the left area, not at the screen edge " +
        "(left=" + (int) barLeft + " px)");
  }

  /**
   * Waits until both initMobileGrid() and initMobileButtonBar() have run,
   * confirming the full mobile layout is in place before checking bounds.
   */
  private static void waitUntilMobileGridReady(WebDriver driver) {
    try {
      new WebDriverWait(driver, Duration.ofSeconds(5))
          .until(d -> (Boolean) ((JavascriptExecutor) d).executeScript(
              "return !!document.getElementById('mobile-content-grid')" +
              "    && !!document.getElementById('mobile-button-bar');"));
    } catch (Exception ignored) {
      // testLayout will report any MISSING element if init never completed
    }
  }
}