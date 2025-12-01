package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerPlaysCard;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickForfeitButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.Assert.assertEquals;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.IntegrationTests.Utils.Steps;
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
  String playerId0;
  String playerId1;
  String playerId2;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startTestApp();
    driver = getDriver();
    playerId0 = ApiUtil.getPlayerid("123", 0);
    playerId1 = ApiUtil.getPlayerid("123", 1);
    playerId2 = ApiUtil.getPlayerid("123", 2);
    setPlayerIdPlaying(driver, playerId0);
    waitUntilCardsAreLoaded(driver);
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
  public void player0IsPlayingWhenStartingGame() {
    driver.navigate().refresh();
    WebElement player0 = driver.findElement(By.id(playerId0));
    assertEquals("playerPlaying playerActive", player0.getAttribute("class"));
  }

  @Test
  public void player0IsInactiveAfterForfeit() throws InterruptedException {
    // GIVEN game started

    // WHEN
    Steps.playerForfeits(driver, playerId0);

    // THEN
    setPlayerIdPlaying(driver, playerId1);
    waitUntilCardsAreLoaded(driver);
    WebElement player0 = driver.findElement(By.id(playerId0));
    assertEquals("playerNotPlaying playerInactive", player0.getAttribute("class"));
  }

  @Test
  public void player1IsPlayingWhenPlayer0Forfeits() throws InterruptedException {
    // GIVEN game started

    // WHEN
    clickForfeitButton(driver);
    TestUtils.wait(200);

    // THEN
    setPlayerIdPlaying(driver, playerId1);
    waitUntilCardsAreLoaded(driver);
    WebElement player1 = driver.findElement(By.id(playerId1));
    assertEquals("playerPlaying playerActive", player1.getAttribute("class"));
  }

  @Test
  public void player2IsPlayingWhen0And1Forfeit() throws InterruptedException {
    // GIVEN game started

    // WHEN
    playerForfeits(driver, playerId0);
    playerForfeits(driver, playerId1);

    // THEN
    WebElement player = driver.findElement(By.id(playerId2));
    assertEquals("playerPlaying playerActive", player.getAttribute("class"));
  }

  @Test
  public void player0IsStilActiveAfterPlayingCard() throws InterruptedException {
    // GIVEN game started

    // WHEN
    PawnId pawnId00 = new PawnId(playerId0, 0);
    playerPlaysCard(driver, playerId0, pawnId00, 1);
    TestUtils.wait(400);

    // THEN
    WebElement player0 = driver.findElement(By.id(playerId0));
    assertEquals("playerNotPlaying playerActive", player0.getAttribute("class"));
  }
}
