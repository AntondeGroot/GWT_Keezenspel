package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickCardByValue;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickForfeitButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickMakeMoveButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPawn;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.Assert.assertEquals;

import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.IntegrationTests.Utils.TestUtils;
import ADG.Games.Keezen.Player.PawnId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@ExtendWith(ScreenshotOnFailure.class)
public class PlayerStatusMock_IT {
  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startTestApp();
    driver = getDriver();
    setPlayerIdPlaying(driver,"0");
  }

  @AfterEach
  public void tearDown() {
    SpringAppTestHelper.stopApp();
  }

  /***
   * in order to use ScreenshotOnFailure, the webdriver should not be quit in the
   * @AfterEach tearDown(), because then the driver would no longer be accessible
   * to take a screenshot with.
   *
   * The driver.quit() should then be put in the @AfterAll which comes after the TestWatcher
   * is done. This however is a static method, requiring the webdriver to be static as well.
   */
  @AfterAll
  public static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
  }

  @Test
  public void player0IsPlayingWhenStartingGame(){
    WebElement player0 = driver.findElement(By.id("player0"));
    assertEquals("playerPlaying playerActive", player0.getAttribute("class"));
  }

  @Test
  public void player1IsPlayingWhenPlayer0Forfeits() throws InterruptedException {
    // GIVEN
    waitUntilCardsAreLoaded(driver);

    // WHEN
    clickForfeitButton(driver);
    TestUtils.wait(200);

    // THEN
    setPlayerIdPlaying(driver,"1");
    waitUntilCardsAreLoaded(driver);
    WebElement player0 = driver.findElement(By.id("player0"));
    assertEquals("playerNotPlaying playerInactive", player0.getAttribute("class"));
  }
  @Test
  public void player0IsStilActiveAfterPlayingCard() throws InterruptedException {
    // GIVEN
    waitUntilCardsAreLoaded(driver);

    // WHEN
    clickPawn(driver, new PawnId("0",0));
    TestUtils.wait(200);
    clickCardByValue(driver, 1);
    TestUtils.wait(200);
    clickMakeMoveButton(driver);
    TestUtils.wait(5000);

    // THEN
    WebElement player0 = driver.findElement(By.id("player0"));
    assertEquals("playerNotPlaying playerActive", player0.getAttribute("class"));
  }
}
