package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Player.assertPlayerHasMedal;
import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.whenPlayerWins;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.IntegrationTests.Utils.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(ScreenshotOnFailure.class)
public class Winner_IT {

  static WebDriver driver;
  String playerId0;
  String playerId1;
  String playerId2;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startTestApp();
    driver = getDriver();
    playerId0 = ApiUtil.getPlayerid("123",0);
    playerId1 = ApiUtil.getPlayerid("123",1);
    playerId2 = ApiUtil.getPlayerid("123",2);
    setPlayerIdPlaying(driver, playerId0);
  }

  @AfterAll
  public static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
  }

  @Test
  public void letPlayer2WinsAndGetsFirstPrize() throws InterruptedException {
    // GIVEN player 0 forfeits
    waitUntilCardsAreLoaded(driver);
    playerForfeits(driver, playerId0);

    // GIVEN player 1 forfeits
    playerForfeits(driver, playerId1);

    // WHEN player 2 plays all cards until he wins
    whenPlayerWins(driver, playerId2);

    // THEN player 2 got the first prize medal
    assertPlayerHasMedal(driver, playerId2, 1);
  }

  @Test
  public void letPlayer2Win_ThenPlayer0_ThenPlayer1() throws InterruptedException {
    // GIVEN player 0 forfeits
    waitUntilCardsAreLoaded(driver);
    playerForfeits(driver, playerId0);

    // GIVEN player 1 forfeits
    playerForfeits(driver, playerId1);

    // WHEN player 2 plays all cards until he wins
    whenPlayerWins(driver, playerId2);
    playerForfeits(driver, playerId2);

    // When
    playerForfeits(driver, playerId1);

    // WHEN player 0 plays until he wins
    whenPlayerWins(driver, playerId0);
    playerForfeits(driver, playerId0);

    assertPlayerHasMedal(driver, playerId0, 2);

    whenPlayerWins(driver, playerId1);
    TestUtils.wait(500);
    assertPlayerHasMedal(driver, playerId1, 3);
  }
}
