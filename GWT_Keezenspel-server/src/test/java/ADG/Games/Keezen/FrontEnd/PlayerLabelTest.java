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
public class PlayerLabelTest {

  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
  }

  @AfterAll
  public static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
  }
}