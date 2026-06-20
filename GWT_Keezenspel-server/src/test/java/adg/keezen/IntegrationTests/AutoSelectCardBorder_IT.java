package adg.keezen.IntegrationTests;

import static adg.keezen.IntegrationTests.Utils.TestUtils.clickPawn;
import static adg.keezen.IntegrationTests.Utils.TestUtils.findCardByIndex;
import static adg.keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static adg.keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.Assert.assertEquals;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.Point;
import adg.keezen.player.PawnId;
import adg.keezen.utils.BaseIntegrationTest;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Auto-selecting a card from a pawn click must give that card the same red selection border a
 * manual card click does. The model already selected the card, but the hand has to be repainted
 * for the border to show (see GameBoardView.refreshHandCardSelection).
 */
public class AutoSelectCardBorder_IT extends BaseIntegrationTest {

  static WebDriver driver;
  static String sessionId;
  static String player0Id;
  static PawnId pawnId00;

  @BeforeAll
  static void setUp() {
    sessionId = ApiUtil.createStandardGame();
    driver = getDriver(sessionId);
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    pawnId00 = new PawnId(player0Id, 0);
    setPlayerIdPlaying(driver, player0Id);
  }

  @AfterAll
  public static void tearDownAll() {
    // needed for skipping the selenium tests in CI
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  public void clickNestPawn_withOnlyKing_cardGetsRedBorder() {
    // GIVEN player 0 holds only a King and has a pawn on the nest
    ApiUtil.setOnlyCardForPlayer(sessionId, player0Id, 13);
    setPlayerIdPlaying(driver, player0Id);
    waitUntilCardsAreLoaded(driver);

    // WHEN they click the nest pawn (King is the unambiguous come-on-board card)
    clickPawn(driver, pawnId00);

    // THEN the King is auto-selected and shown with the red selection border
    waitUntilBorderWidth(driver, "3px");
    WebElement king = findCardByIndex(driver, "cardDiv", 0);
    assertEquals("3px", king.getCssValue("border-width"));
    assertEquals("rgb(255, 0, 0)", king.getCssValue("border-color"));
  }

  @Test
  public void deselectingNestPawn_removesAutoSelectedCardBorder() {
    // GIVEN the King was auto-selected by clicking the nest pawn
    ApiUtil.setOnlyCardForPlayer(sessionId, player0Id, 13);
    setPlayerIdPlaying(driver, player0Id);
    waitUntilCardsAreLoaded(driver);
    clickPawn(driver, pawnId00);
    waitUntilBorderWidth(driver, "3px");

    // WHEN the pawn is deselected, leaving the auto-selected card without its justifying pawn
    WebElement pawn = driver.findElement(org.openqa.selenium.By.id(pawnId00.toString()));
    pawn.click();

    // THEN the border is cleared again
    waitUntilBorderWidth(driver, "0px");
    WebElement king = findCardByIndex(driver, "cardDiv", 0);
    assertEquals("0px", king.getCssValue("border-width"));
  }

  private static void waitUntilBorderWidth(WebDriver driver, String expected) {
    new WebDriverWait(driver, Duration.ofSeconds(3))
        .until(d -> expected.equals(findCardByIndex(d, "cardDiv", 0).getCssValue("border-width")));
  }
}