package ADG.Games.Keezen.ApiTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

public class ApiCheckMoveTest {
  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
  }

  @Test
  public void checkMove() {
    ApiUtil.createStandardGame();
  }
}
