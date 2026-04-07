package adg.keezen.IntegrationTests;

import static adg.keezen.IntegrationTests.Utils.Steps.playerForfeits;
import static adg.keezen.IntegrationTests.Utils.Steps.playerPlaysCard;
import static adg.keezen.IntegrationTests.Utils.TestUtils.assertPointsNotEqual;
import static adg.keezen.IntegrationTests.Utils.TestUtils.clickById;
import static adg.keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static adg.keezen.IntegrationTests.Utils.TestUtils.getPawnLocation;
import static adg.keezen.IntegrationTests.Utils.TestUtils.pawnIsSelected;
import static adg.keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilPawnStopsMoving;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.utils.BaseIntegrationTest;
import adg.keezen.player.PawnId;
import adg.keezen.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.WebDriver;

// optimized
@TestMethodOrder(OrderAnnotation.class)
public class Pawn_IT extends BaseIntegrationTest {

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
  }

  @Test
  @Order(1)
  public void clickOnOwnPawn_Selected() {
    // GIVEN
    PawnId pawnId = new PawnId(player0Id, 0);

    // WHEN
    clickById(driver, pawnId.toString());

    // THEN
    assertTrue(pawnIsSelected(driver, pawnId));
  }

  @Test
  @Order(2)
  public void clickOnOwnPawn_clickSecondPawn_FirstPawnDeselected() {
    // GIVEN
    PawnId pawnId1 = new PawnId(player0Id, 1);
    PawnId pawnId2 = new PawnId(player0Id, 2);
    clickById(driver, pawnId1.toString());

    // WHEN
    clickById(driver, pawnId2.toString());

    // THEN
    assertFalse(pawnIsSelected(driver, pawnId1));
    assertTrue(pawnIsSelected(driver, pawnId2));
  }

  @Test
  @Order(3)
  public void clickOnOtherPawnOnBase_NotSelected() {
    // GIVEN / WHEN
    PawnId pawnId = new PawnId(player1Id, 1);
    clickById(driver, pawnId.toString());

    // THEN
    assertFalse(pawnIsSelected(driver, pawnId));
  }

  @Test
  @Order(4)
  public void playerCanMoveOnBoardAndPlayWithoutHavingToRefreshPage() {
    PawnId pawnId = new PawnId(player2Id, 0);

    // GIVEN player 0 forfeits
    playerForfeits(driver, player0Id);

    // GIVEN player 1 forfeits
    playerForfeits(driver, player1Id);

    // GIVEN player 2 moves on board
    Point nest = getPawnLocation(driver, pawnId);
    playerPlaysCard(driver, sessionId, player2Id, pawnId, 1);

    // WHEN
    Point before = getPawnLocation(driver, pawnId);
    playerPlaysCard(driver,sessionId, player2Id, pawnId, 2);
    waitUntilPawnStopsMoving(driver, pawnId);
    Point after = getPawnLocation(driver, pawnId);

    // THEN
    assertPointsNotEqual("The pawn did not move from the nest tile", nest, before);
    assertPointsNotEqual("The pawn did not move from the start tile", before, after);
    assertTrue(pawnIsSelected(driver, pawnId));
  }
}
