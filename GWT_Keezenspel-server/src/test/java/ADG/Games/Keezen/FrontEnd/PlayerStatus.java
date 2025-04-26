package ADG.Games.Keezen.FrontEnd;

import static ADG.Games.Keezen.FrontEnd.Utils.TestUtils.clickForfeitButton;
import static ADG.Games.Keezen.FrontEnd.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.FrontEnd.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.FrontEnd.Utils.TestUtils.waitUntilPageIsLoaded;
import static org.junit.Assert.assertEquals;

import ADG.Games.Keezen.FrontEnd.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.FrontEnd.Utils.SpringAppTestHelper;
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
public class PlayerStatus {
  static WebDriver driver;


  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
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
    assertEquals("playerPlaying", player0.getAttribute("class"));
  }

  @Test
  public void player1IsPlayingWhenPlayer0Forfeits() throws InterruptedException {
    waitUntilPageIsLoaded(driver);
    clickForfeitButton(driver);

    driver.navigate().refresh();
    WebElement player1 = driver.findElement(By.id("player0"));

    assertEquals("playerNotPlaying", player1.getAttribute("class"));
  }
}
