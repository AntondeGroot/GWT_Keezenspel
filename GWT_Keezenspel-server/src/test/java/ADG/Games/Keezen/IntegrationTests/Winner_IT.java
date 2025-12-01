package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Player.assertPlayerHasMedal;
import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.whenPlayerWins;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;

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

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startTestApp();
    driver = getDriver();
    setPlayerIdPlaying(driver, "player0");
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
    playerForfeits(driver, "player0");

    // GIVEN player 1 forfeits
    playerForfeits(driver, "player1");

    // WHEN player 2 plays all cards until he wins
    whenPlayerWins(driver, "player2");

    // THEN player 2 got the first prize medal
    assertPlayerHasMedal(driver, "player2", 1);
  }

  @Test
  public void letPlayer2Win_ThenPlayer0_ThenPlayer1() throws InterruptedException {
    // GIVEN player 0 forfeits
    waitUntilCardsAreLoaded(driver);
    playerForfeits(driver, "player0");

    // GIVEN player 1 forfeits
    playerForfeits(driver, "player1");

    // WHEN player 2 plays all cards until he wins
    whenPlayerWins(driver, "player2");
    playerForfeits(driver, "player2");

    // When
    playerForfeits(driver, "player1");

    // WHEN player 0 plays until he wins
    whenPlayerWins(driver, "player0");
    playerForfeits(driver, "player0");

    assertPlayerHasMedal(driver, "player0", 2);

    whenPlayerWins(driver, "player1");
    TestUtils.wait(500);
    assertPlayerHasMedal(driver, "player1", 3);
  }
}
