package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.assertPointsNotEqual;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickCardByValue;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPlayCardButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPawn;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getPawnLocation;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilPawnStopsMoving;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(ScreenshotOnFailure.class)
public class MovingOnBoard_IT {
  static WebDriver driver;
  private final PawnId pawnId10 = new PawnId("1",0);
  private final PawnId pawnId20 = new PawnId("2",0);

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");

    SpringAppTestHelper.startTestApp();
    driver = getDriver();
    setPlayerIdPlaying(driver,"0");
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
  public void pawnCanMoveOnBoardWithAce(){
    // GIVEN
    Point start = clickPawn(driver, new PawnId("0",0));

    // WHEN
    clickCardByValue(driver, 1);
    clickPlayCardButton(driver);

    // THEN
    Point end = clickPawn(driver, new PawnId("0",0));
    assertNotEquals(start, end);
  }

  @Test
  public void pawnCanMoveOnBoardWithKing() throws InterruptedException {
    // GIVEN
    Point start = clickPawn(driver, new PawnId("0",0));

    // WHEN
    clickCardByValue(driver, 13);
    clickPlayCardButton(driver);

    // THEN
    Point end = clickPawn(driver, new PawnId("0",0));
    assertNotEquals(start, end);
  }

  /***
   * This is to test if PawnAndCardSelection updates correctly
   * A pawn on a nest tile cannot move
   */
  @Test
  public void pawnAfterMovingOnboardCanImmediatelyMove(){
    // GIVEN
    playerForfeits(driver, "0");
    playerForfeits(driver, "1");

    // WHEN
    setPlayerIdPlaying(driver,"2");
    clickPawn(driver, pawnId20);
    clickCardByValue(driver, 1);
    clickPlayCardButton(driver);
    Point startTile = getPawnLocation(driver, pawnId20);

    // WHEN
    clickCardByValue(driver, 5);
    clickPlayCardButton(driver);

    // THEN
    waitUntilPawnStopsMoving(driver, pawnId20);
    Point endLocation = getPawnLocation(driver, pawnId20);

    assertPointsNotEqual("The pawn did not move", startTile, endLocation);
  }

  /***
   * This is to test if PawnAndCardSelection updates correctly
   * A pawn on a nest tile cannot switch
   */
  @Test
  public void pawnAfterMovingOnboardCanImmediatelySwitch(){
    // GIVEN
    playerForfeits(driver, "0");

    // WHEN BOTH PLAYERS GET ON BOARD
    setPlayerIdPlaying(driver,"1");
    clickPawn(driver, pawnId10);
    clickCardByValue(driver, 1);
    clickPlayCardButton(driver);
    waitUntilPawnStopsMoving(driver, pawnId10);

    setPlayerIdPlaying(driver,"2");
    clickPawn(driver, pawnId20);
    clickCardByValue(driver, 1);
    clickPlayCardButton(driver);
    waitUntilPawnStopsMoving(driver, pawnId20);

    setPlayerIdPlaying(driver,"1");
    Point p1 = clickPawn(driver, pawnId10);
    clickCardByValue(driver, 5);
    clickPlayCardButton(driver);
    waitUntilPawnStopsMoving(driver, pawnId10);
    Point p2 = getPawnLocation(driver, pawnId10);

    assertPointsNotEqual("The pawn 10 did not move after coming on board", p1, p2);

    Point positionPawn10 = getPawnLocation(driver, pawnId10);
    Point positionPawn20 = getPawnLocation(driver, pawnId20);

    // WHEN PLAYER 2 SWITCHES WITH PLAYER 1
    setPlayerIdPlaying(driver,"2");
    clickCardByValue(driver, 11);
    clickPawn(driver, pawnId20);
    clickPawn(driver, pawnId10);
    clickPlayCardButton(driver);

    waitUntilPawnStopsMoving(driver, pawnId20);

    // THEN THE PAWNS SWITCHED PLACE
    assertEquals(positionPawn10, getPawnLocation(driver, pawnId20));
    assertEquals(positionPawn20, getPawnLocation(driver, pawnId10));
  }

  /***
   * This is to test if PawnAndCardSelection updates correctly
   * A pawn on a nest tile cannot split
   */
  @Test
  public void pawnOnboardCanImmediatelyPlay7(){}

}
