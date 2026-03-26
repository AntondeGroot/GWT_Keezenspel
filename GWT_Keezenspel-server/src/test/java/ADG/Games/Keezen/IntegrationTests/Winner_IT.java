package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Player.assertPlayerHasMedal;
import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerPlaysCard;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import ADG.Games.Keezen.IntegrationTests.Utils.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

// optimized
public class Winner_IT extends BaseIntegrationTest {

  static WebDriver driver;
  static String sessionId;
  static String player0Id;
  static String player1Id;
  static String player2Id;

  @BeforeAll
  static void setUp() {
    sessionId = ApiUtil.createStandardGame();
    driver = getDriver(sessionId);
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid(sessionId,0));
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    player1Id = ApiUtil.getPlayerid(sessionId, 1);
    player2Id = ApiUtil.getPlayerid(sessionId, 2);
  }

  @AfterAll
  public static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  public void letPlayer2Win_ThenPlayer0_ThenPlayer1() throws InterruptedException {
    // GIVEN player 0 forfeits
    waitUntilCardsAreLoaded(driver);
    playerForfeits(driver, player0Id);

    // GIVEN player 1 forfeits
    playerForfeits(driver, player1Id);

    // WHEN player 2 wins by moving last pawn from player1's section tile 15 into own finish with an Ace
    ApiUtil.setPawnPosition(sessionId, player2Id, 0, player2Id, 19);
    ApiUtil.setPawnPosition(sessionId, player2Id, 1, player2Id, 18);
    ApiUtil.setPawnPosition(sessionId, player2Id, 2, player2Id, 17);
    ApiUtil.setPawnPosition(sessionId, player2Id, 3, player1Id, 15);
    playerPlaysCard(driver, sessionId, player2Id, new PawnId(player2Id, 3), 1);
    assertPlayerHasMedal(driver, player2Id, 1);

    // WHEN player 0 wins (preceded on the board by player2)
    ApiUtil.setPawnPosition(sessionId, player0Id, 0, player0Id, 19);
    ApiUtil.setPawnPosition(sessionId, player0Id, 1, player0Id, 18);
    ApiUtil.setPawnPosition(sessionId, player0Id, 2, player0Id, 17);
    ApiUtil.setPawnPosition(sessionId, player0Id, 3, player2Id, 15);
    waitUntilCardsAreLoaded(driver);
    playerForfeits(driver, player1Id);// a new round of cards was dealt, meaning player 1 begins
    setPlayerIdPlaying(driver, player0Id);
    waitUntilCardsAreLoaded(driver);
    playerPlaysCard(driver, sessionId, player0Id, new PawnId(player0Id, 3), 1);
    assertPlayerHasMedal(driver, player0Id, 2);

    // WHEN player 1 wins (preceded on the board by player0)
    ApiUtil.setPawnPosition(sessionId, player1Id, 0, player1Id, 19);
    ApiUtil.setPawnPosition(sessionId, player1Id, 1, player1Id, 18);
    ApiUtil.setPawnPosition(sessionId, player1Id, 2, player1Id, 17);
    ApiUtil.setPawnPosition(sessionId, player1Id, 3, player0Id, 15);
    playerForfeits(driver, player0Id);
    playerPlaysCard(driver, sessionId, player1Id, new PawnId(player1Id, 3), 1);
    TestUtils.wait(500);
    assertPlayerHasMedal(driver, player1Id, 3);
  }
}
