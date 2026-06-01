package adg.keezen.IntegrationTests;

import static adg.keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static adg.keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilDOMElementUpdates;
import static org.junit.Assert.assertEquals;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.player.PawnId;
import adg.keezen.utils.BaseIntegrationTest;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CardSevenSplit_IT extends BaseIntegrationTest {

  static WebDriver driver;
  static String sessionId;
  static String player0Id;

  @BeforeAll
  static void setUp() {
    sessionId = ApiUtil.createStandardGame();
    driver = getDriver(sessionId);
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    setPlayerIdPlaying(driver, player0Id);
  }

  @AfterAll
  static void tearDownAll() {
    if (driver != null) driver.quit();
  }

  /**
   * Regression test for the step-box/model desync bug: after the user sets the split steps
   * to a non-default value, re-selecting the 7-card must reset the step boxes to the model
   * defaults (0 / 7), not leave them showing the stale user-entered values.
   *
   * <p>Before the fix, clicking card 7 a second time called handleSeven() internally (resetting
   * nrStepsPawn1=0, nrStepsPawn2=7) but never pushed those values back to the TextBox elements,
   * so the boxes still showed the old user input while the model had 0/7. The send button then
   * used the model (0/7) instead of what was displayed (e.g. 3/4), causing wrong moves.
   */
  @Test
  void card7Reselect_stepBoxesReflectModelReset() {
    // Place two pawns on the board so split mode is available.
    ApiUtil.setPawnPosition(sessionId, player0Id, 0, player0Id, 1);
    ApiUtil.setPawnPosition(sessionId, player0Id, 1, player0Id, 2);
    ApiUtil.setCardForPlayer(sessionId, player0Id, 7);
    setPlayerIdPlaying(driver, player0Id);
    waitUntilCardsAreLoaded(driver);

    PawnId pawnA = new PawnId(player0Id, 0);
    PawnId pawnB = new PawnId(player0Id, 1);

    // Select card 7 then both pawns → activates split mode with default 0/7.
    driver.findElement(By.id("card_0_7")).click();
    driver.findElement(By.id(pawnA.toString())).click();
    driver.findElement(By.id(pawnB.toString())).click();

    new WebDriverWait(driver, Duration.ofSeconds(3))
        .until(d -> d.findElement(By.className("pawnIntegerBoxes")).isDisplayed());

    // Verify initial defaults.
    assertEquals("0", stepBox1Value());
    assertEquals("7", stepBox2Value());

    // User changes step 1 to 3 (step 2 becomes 4).
    WebElement box1 = driver.findElement(By.className("TextBoxForPawnSteps1"));
    box1.click();
    box1.clear();
    box1.sendKeys("3");
    box1.sendKeys(Keys.TAB);

    assertEquals("3", stepBox1Value());
    assertEquals("4", stepBox2Value());

    // Re-select card 7: deselect then select again.
    driver.findElement(By.id("card_0_7")).click();
    waitUntilDOMElementUpdates(driver, "cardDiv");
    driver.findElement(By.id("card_0_7")).click();

    new WebDriverWait(driver, Duration.ofSeconds(3))
        .until(d -> d.findElement(By.className("pawnIntegerBoxes")).isDisplayed());

    // The step boxes must now show the model-reset defaults (0/7), not the stale "3"/"4".
    assertEquals("Step box 1 must reset to 0 after re-selecting card 7", "0", stepBox1Value());
    assertEquals("Step box 2 must reset to 7 after re-selecting card 7", "7", stepBox2Value());
  }

  private static String stepBox1Value() {
    return driver.findElement(By.className("TextBoxForPawnSteps1")).getAttribute("value");
  }

  private static String stepBox2Value() {
    return driver.findElement(By.className("TextBoxForPawnSteps2")).getAttribute("value");
  }
}