package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickForfeitButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.Assert.assertEquals;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PlayerStatusReal_IT extends BaseIntegrationTest {

  static WebDriver driver;
  String playerId0;

  @BeforeEach
  public void setUp() {
    driver = getDriver();
    playerId0 = ApiUtil.getPlayerid("123",0);
    setPlayerIdPlaying(driver, playerId0);
  }

  @AfterEach
  public void tearDown() {
    driver.quit();
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
  }

  @Test
  public void player0IsPlayingWhenStartingGame() {
    // GIVEN a started game
    waitUntilCardsAreLoaded(driver);

    // WHEN you look at player 0
    WebElement player0 = driver.findElement(By.id(playerId0));

    // THEN it is player 0's turn
    assertEquals("playerPlaying playerActive", player0.getAttribute("class"));
  }

  @Test
  public void player1IsPlayingWhenPlayer0Forfeits() throws InterruptedException {
    // GIVEN a started game
    waitUntilCardsAreLoaded(driver);

    // WHEN
    clickForfeitButton(driver);

    // THEN;
    WebElement player1 = driver.findElement(By.id(playerId0));
    assertEquals("playerNotPlaying playerInactive", player1.getAttribute("class"));
  }
}
