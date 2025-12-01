package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerPlaysCard;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.assertPointsNotEqual;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getPawnLocation;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.pawnIsSelected;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.IntegrationTests.Utils.TestUtils;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@ExtendWith(ScreenshotOnFailure.class)
public class Pawn_IT {

  static WebDriver driver;
  String HIDDEN = "hidden";
  String VISIBLE = "visible";
  String playerId0;
  String playerId1;
  String playerId2;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");

    SpringAppTestHelper.startRealApp();
    driver = getDriver();
    playerId0 = ApiUtil.getPlayerid("123", 0);
    playerId1 = ApiUtil.getPlayerid("123", 1);
    playerId2 = ApiUtil.getPlayerid("123", 2);
    setPlayerIdPlaying(driver, playerId0);
  }

  @AfterEach
  public void tearDown() {
    driver.quit();
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
  public void clickOnOwnPawn_Selected() {
    // GIVEN
    PawnId pawnId = new PawnId(playerId0, 0);
    WebElement pawn1 = driver.findElement(By.id(pawnId.toString()));

    // WHEN
    pawn1.click();

    // THEN
    WebElement pawn1Outline = driver.findElement(By.className(pawnId + "Overlay"));
    assertEquals(VISIBLE, pawn1Outline.getCssValue("visibility"));
  }

  @Test
  public void clickOnOwnPawn_clickSecondPawn_FirstPawnDeselected() {
    // GIVEN
    String pawnId1 = playerId0+"_1";
    String pawnId2 = playerId0+"_2";
    WebElement pawn1 = driver.findElement(new ById(pawnId1));
    pawn1.click();

    // WHEN
    WebElement pawn2 = driver.findElement(new ById(pawnId2));
    pawn2.click();

    // THEN
    WebElement pawn1Outline = driver.findElement(By.className(pawnId1 + "Overlay"));
    assertEquals(HIDDEN, pawn1Outline.getCssValue("visibility"));

    WebElement pawn2Outline = driver.findElement(By.className(pawnId2 + "Overlay"));
    assertEquals(VISIBLE, pawn2Outline.getCssValue("visibility"));
  }

  @Test
  public void clickOnOtherPawnOnBase_NotSelected() {
    // GIVEN
    PawnId pawnId = new PawnId(playerId1, 1);
    WebElement pawnOtherPlayer = driver.findElement(new ById(pawnId.toString()));

    // WHEN
    pawnOtherPlayer.click();

    // THEN
    WebElement pawnOtherPlayerOutline = driver.findElement(By.className(pawnId + "Overlay"));
    assertEquals(HIDDEN, pawnOtherPlayerOutline.getCssValue("visibility"));
  }

  @Test
  public void playerCanMoveOnBoardAndPlayWithoutHavingToRefreshPage() {
    PawnId pawnId = new PawnId(playerId2, 0);

    // GIVEN player 0 forfeits
    playerForfeits(driver, playerId0);

    // GIVEN player 1 forfeits
    playerForfeits(driver, playerId1);

    // GIVEN player 2 moves on board
    driver.navigate().refresh();
    Point nest = getPawnLocation(driver, pawnId);
    playerPlaysCard(driver, playerId2, pawnId, 1);

    // WHEN
    Point before = getPawnLocation(driver, pawnId);
    playerPlaysCard(driver, playerId2, pawnId, 5);
    TestUtils.wait(1000);
    Point after = getPawnLocation(driver, pawnId);

    // THEN
    assertPointsNotEqual("The pawn did not move from the nest tile", nest, before);
    assertPointsNotEqual("The pawn did not move from the start tile", before, after);
    assertTrue(pawnIsSelected(driver, pawnId));
  }
}
