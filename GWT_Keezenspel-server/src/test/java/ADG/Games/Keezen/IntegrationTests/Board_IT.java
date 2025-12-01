package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@ExtendWith(ScreenshotOnFailure.class)
class Board_IT {

  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
    setPlayerIdPlaying(driver, "player0");
  }

  @AfterEach
  public void tearDown() {
    SpringAppTestHelper.stopApp();
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
    SpringAppTestHelper.stopApp();
  }

  @Test
  public void verifyTitle() {
    assertEquals("Keezenspel", driver.getTitle());
  }

  @Test
  public void verifyNumberOfTilesFor3Players() {
    assertEquals(24 * 3, driver.findElements(By.className("tile")).size());
  }

  @Test
  public void verifyNumberOfPawnsFor3Players() {
    assertEquals(12, driver.findElements(By.className("pawnDiv")).size());
  }

  @Test
  public void verifyNumberOfCardsForPlayer1() {
    setPlayerIdPlaying(driver, "player0");

    assertEquals(5, driver.findElements(By.className("cardDiv")).size());
  }
}
