package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerPlaysCard;
import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerSwitchesPawns;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.assertPointsEqual;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.assertPointsNotEqual;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getPawnLocation;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.IntegrationTests.Utils.TestUtils;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(ScreenshotOnFailure.class)
public class Jack_IT {

  static WebDriver driver;
  private PawnId pawnId10;
  private PawnId pawnId20;
  private String playerId0;
  private String playerId1;
  private String playerId2;
  
  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startTestApp();
    driver = getDriver();

    playerId0 = ApiUtil.getPlayerid("123",0);
    playerId1 = ApiUtil.getPlayerid("123",1);
    playerId2 = ApiUtil.getPlayerid("123",2);
    pawnId10 = new PawnId(playerId1, 0);
    pawnId20 = new PawnId(playerId2, 0);
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
  public void playersSwitchWithJack() {
    // GIVEN
    waitUntilCardsAreLoaded(driver);

    playerForfeits(driver, playerId0);

    // get on board
    setPlayerIdPlaying(driver, playerId1);
    driver.navigate().refresh();
    playerPlaysCard(driver, playerId1, pawnId10, 1);
    setPlayerIdPlaying(driver, playerId2);
    playerPlaysCard(driver, playerId2, pawnId20, 1);

    // move on board
    Point start = getPawnLocation(driver, pawnId10);
    playerPlaysCard(driver, playerId1, pawnId10, 2);
    Point end = getPawnLocation(driver, pawnId10);
    assertPointsNotEqual(
        "The pawn of player 1 did not move with 2 steps after coming on board", start, end);

    // now player 2 can switch using a Jack
    Point positionPlayer1 = getPawnLocation(driver, pawnId10);
    Point positionPlayer2 = getPawnLocation(driver, pawnId20);
    playerSwitchesPawns(driver, playerId2, pawnId20, pawnId10);

    // THEN
    TestUtils.wait(400);

    System.out.println(
        "original position player 1 :"
            + positionPlayer2
            + " original position player 2 :"
            + positionPlayer1);
    assertPointsEqual(
        "Expected player 2 to be at position 1",
        positionPlayer1,
        getPawnLocation(driver, pawnId20));
    assertPointsEqual(
        "Expected player 1 to be at point 2", positionPlayer2, getPawnLocation(driver, pawnId10));
  }
}
