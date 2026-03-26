package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// optimized
class Board_IT extends BaseIntegrationTest {

  static WebDriver driver;

  @BeforeAll
  public static void setUp() {
    String sessionId = ApiUtil.createStandardGame();
    driver = getDriver(sessionId);
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid(sessionId,0));
  }

  @AfterAll
  public static void tearDown() {
    if (driver != null) {
      driver.quit();
    }
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
    assertEquals(5, driver.findElements(By.className("cardDiv")).size());
  }
}
