package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerPlaysCard;
import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerSwitchesPawns;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.assertPointsEqual;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.assertPointsNotEqual;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPawn;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getPawnLocation;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilPawnStopsMoving;
import static org.junit.Assert.assertNotEquals;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.WebDriver;

// optimized 40 seconds before 13 seconds after
@TestMethodOrder(OrderAnnotation.class)
public class MovingOnBoard_IT extends BaseIntegrationTest {

  static WebDriver driver;
  static String sessionId;
  static PawnId pawnId00;
  static PawnId pawnId10;
  static PawnId pawnId20;
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
    pawnId00 = new PawnId(player0Id, 0);
    pawnId10 = new PawnId(player1Id, 0);
    pawnId20 = new PawnId(player2Id, 0);
  }

  @AfterAll
  public static void tearDownAll() {
    // needed for skipping the selenium tests in CI
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  @Order(1)
  public void pawnCanMoveOnBoardWithAce() {
    // GIVEN
    waitUntilCardsAreLoaded(driver);
    Point start = clickPawn(driver, pawnId00);

    // WHEN
    playerPlaysCard(driver, sessionId, player0Id, pawnId00,1);
    waitUntilPawnStopsMoving(driver, pawnId00);

    // THEN
    Point end = clickPawn(driver, pawnId00);
    assertNotEquals(start, end);
  }


  @Test
  @Order(2)
  public void pawnCanMoveOnBoardWithKing() throws InterruptedException {
    // GIVEN
    setPlayerIdPlaying(driver, player1Id);
    waitUntilCardsAreLoaded(driver);
    Point start = clickPawn(driver, pawnId10);

    // WHEN
    playerPlaysCard(driver, sessionId, player1Id, pawnId10,13);

    // THEN
    Point end = clickPawn(driver, pawnId10);
    assertNotEquals(start, end);
  }

  /***
   * This is to test if PawnAndCardSelection updates correctly
   * A pawn on a nest tile cannot move
   */
  @Test
  @Order(3)
  public void pawnAfterMovingOnboardCanImmediatelyMoveWithAce() {
    // GIVEN
    ApiUtil.setPawnPosition(sessionId, player2Id, 0, player2Id, 0);
    setPlayerIdPlaying(driver, player2Id);
    waitUntilCardsAreLoaded(driver);

    // WHEN
    Point start = getPawnLocation(driver, pawnId20);
    playerPlaysCard(driver, sessionId, player2Id, pawnId20, 1);
    waitUntilPawnStopsMoving(driver, pawnId20);
    Point end = getPawnLocation(driver, pawnId20);

    // THEN
    assertPointsNotEqual("The pawn did not move on the board", start, end);
  }

  /***
   * This is to test if PawnAndCardSelection updates correctly
   * A pawn on a nest tile cannot switch
   */
  @Test
  @Order(4)
  void pawnAfterMovingOnboardCanImmediatelySwitch() {
    ApiUtil.setPawnPosition(sessionId, player0Id, 0, player0Id, 0);
    ApiUtil.setPawnPosition(sessionId, player2Id, 0, player2Id, 15);
    // GIVEN - refresh so the browser sees the updated positions before capturing them
    setPlayerIdPlaying(driver, player0Id);
    waitUntilCardsAreLoaded(driver);
    Point positionPawn20 = getPawnLocation(driver, pawnId20);
    Point positionPawn00 = getPawnLocation(driver, pawnId00);

    // WHEN
    playerSwitchesPawns(driver, sessionId, player0Id, pawnId00, pawnId20);
    waitUntilPawnStopsMoving(driver, pawnId00);

    // THEN THE PAWNS SWITCHED PLACE
    assertPointsEqual(
        "Pawn 0 did not move to pawn 1", positionPawn20, getPawnLocation(driver, pawnId00));
    assertPointsEqual(
        "Pawn 1 did not move to pawn 0", positionPawn00, getPawnLocation(driver, pawnId20));
  }
}
