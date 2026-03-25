package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

class Board_IT extends BaseIntegrationTest {

  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    driver = getDriver();
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid("123",0));
  }

  @AfterEach
  public void tearDown() {
    if (driver != null) {
      driver.quit();
    }
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
  public void verifyLayout(){
    verifyTitle();
    verifyNumberOfTilesFor3Players();
    verifyNumberOfPawnsFor3Players();
    verifyNumberOfCardsForPlayer1();
  }

  public void verifyTitle() {
    assertEquals("Keezenspel", driver.getTitle());
  }

  public void verifyNumberOfTilesFor3Players() {
    assertEquals(24 * 3, driver.findElements(By.className("tile")).size());
  }

  public void verifyNumberOfPawnsFor3Players() {
    assertEquals(12, driver.findElements(By.className("pawnDiv")).size());
  }

  public void verifyNumberOfCardsForPlayer1() {
    assertEquals(5, driver.findElements(By.className("cardDiv")).size());
  }
}
