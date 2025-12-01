package ADG.Games.Keezen.ApiTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static org.junit.Assert.assertThrows;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.utils.ApiCallsHelper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.springframework.web.client.HttpClientErrorException;

class ApiGameCreationTest {

  private final ApiCallsHelper apiHelper = new ApiCallsHelper();

  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
  }
  @Test
  void cannotCreateGame_withEmptyRoomName_throws400ErrorCode() {
    // GIVEN a game has started
    assertThrows(
        HttpClientErrorException.BadRequest.class,
        () -> {
          apiHelper.createNewGame("", 3);
        });
  }

  @Test
  void cannotCreateGame_withBlankRoomName_throws400ErrorCode() {
    // GIVEN a game has started
    assertThrows(
        HttpClientErrorException.BadRequest.class,
        () -> {
          apiHelper.createNewGame(" ", 3);
        });
  }

  @Test
  void cannotCreateGame_withTooShortRoomName_throws400ErrorCode() {
    // GIVEN a game has started
    assertThrows(
        HttpClientErrorException.BadRequest.class,
        () -> {
          apiHelper.createNewGame("ab", 3);
        });
  }

  @Test
  void cannotCreateGame_withTooShortEmptyRoomName_throws400ErrorCode() {
    assertThrows(
        HttpClientErrorException.BadRequest.class,
        () -> {
          apiHelper.createNewGame("  .", 3);
        });
  }
}
