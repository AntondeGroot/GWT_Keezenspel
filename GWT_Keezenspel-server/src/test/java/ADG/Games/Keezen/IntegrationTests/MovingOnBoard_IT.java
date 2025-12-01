package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerPlaysCard;
import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerSwitchesPawns;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.assertPointsEqual;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.assertPointsNotEqual;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickCardByValue;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPawn;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPlayCardButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getPawnLocation;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static org.junit.Assert.assertNotEquals;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.Point;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(ScreenshotOnFailure.class)
public class MovingOnBoard_IT {

  static WebDriver driver;
  private List<String> playerIds;
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
    playerIds = ApiUtil.getPlayerIds("123");
    playerId0 = playerIds.get(0);
    playerId1 = playerIds.get(1);
    playerId2 = playerIds.get(2);

    pawnId10 = new PawnId(playerId1, 0);
    pawnId20 = new PawnId(playerId2, 0);
    setPlayerIdPlaying(driver, playerId0);
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
  public void pawnCanMoveOnBoardWithAce() {
    // GIVEN
    Point start = clickPawn(driver, new PawnId(playerId0, 0));

    // WHEN
    clickCardByValue(driver, 1);
    clickPlayCardButton(driver);

    // THEN
    Point end = clickPawn(driver, new PawnId(playerId0, 0));
    assertNotEquals(start, end);
  }

  @Test
  public void pawnCanMoveOnBoardWithKing() throws InterruptedException {
    // GIVEN
    Point start = clickPawn(driver, new PawnId(playerId0, 0));

    // WHEN
    clickCardByValue(driver, 13);
    clickPlayCardButton(driver);

    // THEN
    Point end = clickPawn(driver, new PawnId(playerId0, 0));
    assertNotEquals(start, end);
  }

  /***
   * This is to test if PawnAndCardSelection updates correctly
   * A pawn on a nest tile cannot move
   */
  @Test
  @Timeout(25)
  public void pawnAfterMovingOnboardCanImmediatelyMoveWithAce() {
    /***
     *
     */

    // GIVEN
    playerForfeits(driver, playerId0);
    playerForfeits(driver, playerId1);

    // WHEN
    Point nest = getPawnLocation(driver, pawnId20);
    playerPlaysCard(driver, playerId2, pawnId20, 13); // onboard with king
    Point start = getPawnLocation(driver, pawnId20);

    playerPlaysCard(driver, playerId2, pawnId20, 1); // move with Ace
    Point end = getPawnLocation(driver, pawnId20);

    // THEN
    assertPointsNotEqual("The pawn did not move from nest", nest, start);
    assertPointsNotEqual("The pawn did not move on the board", start, end);
  }

  /***
   * This is to test if PawnAndCardSelection updates correctly
   * A pawn on a nest tile cannot switch
   */
  @Test
  public void pawnAfterMovingOnboardCanImmediatelySwitch() {
    // GIVEN
    playerForfeits(driver, playerId0);

    // WHEN BOTH PLAYERS GET ON BOARD
    playerPlaysCard(driver, playerId1, pawnId10, 1);

    playerPlaysCard(driver, playerId2, pawnId20, 1);

    // THEN one pawn moves from starttile so that it can be switched
    Point p1 = getPawnLocation(driver, pawnId10);
    playerPlaysCard(driver, playerId1, pawnId10, 5);
    Point p2 = getPawnLocation(driver, pawnId10);

    assertPointsNotEqual("The pawn 10 did not move after coming on board", p1, p2);

    Point positionPawn10 = getPawnLocation(driver, pawnId10);
    Point positionPawn20 = getPawnLocation(driver, pawnId20);

    // WHEN PLAYER 2 SWITCHES WITH PLAYER 1
    playerSwitchesPawns(driver, playerId2, pawnId20, pawnId10);

    // THEN THE PAWNS SWITCHED PLACE
    assertPointsEqual(
        "Pawn 2 did not move to pawn 1", positionPawn10, getPawnLocation(driver, pawnId20));
    assertPointsEqual(
        "Pawn 1 did not move to pawn 2", positionPawn20, getPawnLocation(driver, pawnId10));
  }

  /***
   * This is to test if PawnAndCardSelection updates correctly
   * A pawn on a nest tile cannot split
   */
  @Test
  public void pawnOnboardCanImmediatelyPlay7() {}
}
