package ADG.Games.Keezen.FrontEnd;

import static ADG.Games.Keezen.FrontEnd.Utils.TestUtils.getDriver;

import ADG.Games.Keezen.FrontEnd.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.FrontEnd.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(ScreenshotOnFailure.class)
public class WinnerTest {

  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startTestApp();
    driver = getDriver();
  }

  @AfterAll
  public static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
  }

  public void letPlayerWin(){
    // 1,4,7
    // 1,4,6
    // 1,4,5
    // 1,4,3,2

    // loop over pawns of playerToWin
    // start looping over players
    // change player playing
    // if player is not playerToWin && if forfeit Button is enabled then forfeit
    //
  }

}