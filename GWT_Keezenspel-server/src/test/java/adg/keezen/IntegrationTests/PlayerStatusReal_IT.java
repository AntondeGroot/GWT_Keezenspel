package adg.keezen.IntegrationTests;

import static adg.keezen.IntegrationTests.Utils.TestUtils.clickForfeitButton;
import static adg.keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static adg.keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.Assert.assertEquals;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.utils.BaseIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

// optimized
@TestMethodOrder(OrderAnnotation.class)
class PlayerStatusReal_IT extends BaseIntegrationTest {

  static WebDriver driver;
  static String player0Id;
  static String player1Id;
  static String sessionId;

  @BeforeAll
  static void setUp() {
    sessionId = ApiUtil.createStandardGame();
    driver = getDriver(sessionId);
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid(sessionId,0));
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    player1Id = ApiUtil.getPlayerid(sessionId, 1);
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
  static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  @Order(1)
  void player0IsPlayingWhenStartingGame() {
    // GIVEN a started game
    waitUntilCardsAreLoaded(driver);

    // WHEN you look at player 0
    WebElement player0 = driver.findElement(By.id(player0Id));

    // THEN it is player 0's turn
    assertEquals("playerPlaying playerActive", player0.getAttribute("class"));
  }

  @Test
  @Order(2)
  void player1IsPlayingWhenPlayer0Forfeits() throws InterruptedException {
    // GIVEN player 0's view is loaded
    setPlayerIdPlaying(driver, player0Id);
    waitUntilCardsAreLoaded(driver);

    // WHEN
    clickForfeitButton(driver);

    // THEN;
    WebElement player1 = driver.findElement(By.id(player0Id));
    assertEquals("playerNotPlaying playerInactive", player1.getAttribute("class"));
  }
}
