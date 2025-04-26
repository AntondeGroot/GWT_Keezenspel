package ADG.Games.Keezen.FrontEnd;

import static ADG.Games.Keezen.FrontEnd.Utils.TestUtils.clickCardByValue;
import static ADG.Games.Keezen.FrontEnd.Utils.TestUtils.clickMakeMoveButton;
import static ADG.Games.Keezen.FrontEnd.Utils.TestUtils.clickPawn;
import static ADG.Games.Keezen.FrontEnd.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.FrontEnd.Utils.TestUtils.setPlayerIdPlaying;
import static org.junit.Assert.assertNotEquals;

import ADG.Games.Keezen.FrontEnd.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.FrontEnd.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(ScreenshotOnFailure.class)
public class MovingOnBoardTest {
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
    // needed for skipping the selenium tests in CI
    if (driver != null) {
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
  }

  @Test
  public void pawnCanMoveOnBoardWithAce(){
    // GIVEN
    Point start = clickPawn(driver, new PawnId("0",0));

    // WHEN
    clickCardByValue(driver, 1);
    clickMakeMoveButton(driver);

    // THEN
    Point end = clickPawn(driver, new PawnId("0",0));
    assertNotEquals(start, end);
  }

  @Test
  public void pawnCanMoveOnBoardWithKing() throws InterruptedException {
    // GIVEN
    Point start = clickPawn(driver, new PawnId("0",0));

    // WHEN
    clickCardByValue(driver, 13);
    clickMakeMoveButton(driver);

    // THEN
    Point end = clickPawn(driver, new PawnId("0",0));
    assertNotEquals(start, end);
  }
}
