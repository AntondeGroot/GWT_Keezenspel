package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@ExtendWith(ScreenshotOnFailure.class)
class Layout_IT {

  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid("123", 0));
    waitUntilCardsAreLoaded(driver);
  }

  @AfterEach
  public void tearDown() {
    SpringAppTestHelper.stopApp();
    if (driver != null) {
      driver.quit();
    }
  }

  @AfterAll
  public static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
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