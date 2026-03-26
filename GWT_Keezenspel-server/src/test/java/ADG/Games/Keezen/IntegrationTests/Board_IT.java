package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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

  @Test
  public void playingFieldIsCenteredVertically() {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    int viewportHeight = ((Long) js.executeScript("return window.innerHeight")).intValue();

    WebElement header = driver.findElement(By.tagName("h1"));
    WebElement footer = driver.findElement(By.tagName("footer"));
    WebElement columnWrapper = driver.findElement(By.className("columnWrapper"));

    int headerBottom = header.getLocation().getY() + header.getSize().getHeight();
    int footerTop = footer.getLocation().getY();
    int availableHeight = footerTop - headerBottom;

    int wrapperTop = columnWrapper.getLocation().getY();
    int wrapperBottom = wrapperTop + columnWrapper.getSize().getHeight();
    int wrapperCenter = (wrapperTop + wrapperBottom) / 2;

    int availableCenter = headerBottom + availableHeight / 2;
    int deviation = Math.abs(wrapperCenter - availableCenter);

    System.out.println("Viewport height : " + viewportHeight);
    System.out.println("Header bottom   : " + headerBottom);
    System.out.println("Footer top      : " + footerTop);
    System.out.println("Available height: " + availableHeight);
    System.out.println("Wrapper center  : " + wrapperCenter + " (expected: " + availableCenter + ", deviation: " + deviation + "px)");

    assertTrue(
        deviation <= 50,
        "Playing field center (" + wrapperCenter + "px) should be within 50px of the available"
            + " vertical center (" + availableCenter + "px), but deviation was " + deviation + "px");
  }
}
