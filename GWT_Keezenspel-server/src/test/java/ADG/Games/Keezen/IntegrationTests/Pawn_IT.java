package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerPlaysCard;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.assertPointsNotEqual;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getPawnLocation;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.pawnIsSelected;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilPawnStopsMoving;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.openqa.selenium.By;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
    WebElement pawn1 = driver.findElement(By.id(pawnId.toString()));

    // WHEN
    pawn1.click();

    // THEN
    assertTrue(pawnIsSelected(driver, pawnId));
  }

  @Test
  @Order(2)
  public void clickOnOwnPawn_clickSecondPawn_FirstPawnDeselected() {
    // GIVEN
    PawnId pawnId1 = new PawnId(player0Id, 1);
    PawnId pawnId2 = new PawnId(player0Id, 2);
    WebElement pawn1 = driver.findElement(new ById(pawnId1.toString()));
    pawn1.click();

    // WHEN
    WebElement pawn2 = driver.findElement(new ById(pawnId2.toString()));
    pawn2.click();

    // THEN
    assertFalse(pawnIsSelected(driver, pawnId1));
    assertTrue(pawnIsSelected(driver, pawnId2));
  }

  @Test
  @Order(3)
  public void clickOnOtherPawnOnBase_NotSelected() {
    // GIVEN
    PawnId pawnId = new PawnId(player1Id, 1);
    WebElement pawnOtherPlayer = driver.findElement(new ById(pawnId.toString()));

    // WHEN
    pawnOtherPlayer.click();

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
