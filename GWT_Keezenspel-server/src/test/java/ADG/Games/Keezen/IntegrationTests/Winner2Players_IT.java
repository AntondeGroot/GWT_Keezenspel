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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

// Tests winner logic specifically for 2-player games.
// Bug report: in a 2-player game (red=player0, blue=player1), when blue wins, red received the medal instead.
class Winner2Players_IT extends BaseIntegrationTest {

  static WebDriver driver;
  static String sessionId;
  static String player0Id;
  static String player1Id;

  @BeforeAll
  static void setUp() {
    sessionId = ApiUtil.createTwoPlayerGame();
    driver = getDriver(sessionId);
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    player1Id = ApiUtil.getPlayerid(sessionId, 1);
    setPlayerIdPlaying(driver, player0Id);
  }

  @AfterAll
  static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  void letPlayer1Win_Player1ShouldGetMedal_NotPlayer0() {
    // GIVEN player 0 (red) forfeits so player 1 (blue) has the turn
    waitUntilCardsAreLoaded(driver);
    playerForfeits(driver, player0Id);

    // WHEN player 1 (blue) wins by moving last pawn from player0's section tile 15 into own finish
    ApiUtil.setPawnPosition(sessionId, player1Id, 0, player1Id, 19);
    ApiUtil.setPawnPosition(sessionId, player1Id, 1, player1Id, 18);
    ApiUtil.setPawnPosition(sessionId, player1Id, 2, player1Id, 17);
    ApiUtil.setPawnPosition(sessionId, player1Id, 3, player0Id, 15);
    playerPlaysCard(driver, sessionId, player1Id, new PawnId(player1Id, 3), 1);

    // THEN player 1 (blue) gets the medal, NOT player 0 (red)
    assertPlayerHasMedal(driver, player1Id, 1);
  }
}