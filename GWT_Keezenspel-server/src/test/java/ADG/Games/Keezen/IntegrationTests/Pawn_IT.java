package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerPlaysCard;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.assertPointsNotEqual;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getPawnLocation;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.pawnIsSelected;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
  public void clickOnOwnPawn_Selected() throws InterruptedException {
    // GIVEN
    PawnId pawnId = new PawnId("0",0);
    WebElement pawn1 = driver.findElement(By.id(pawnId.toString()));

    // WHEN
    pawn1.click();

    // THEN
    WebElement pawn1Outline = driver.findElement(By.className(pawnId+"Overlay"));
    assertEquals("visible", pawn1Outline.getCssValue("visibility"));
  }

  @Test
  public void clickOnOwnPawn_clickSecondPawn_FirstPawnDeselected(){
    // GIVEN
    PawnId pawnId1 = new PawnId("0",1);
    PawnId pawnId2 = new PawnId("0",2);
    WebElement pawn1 = driver.findElement(new ById(pawnId1.toString()));
    pawn1.click();

    // WHEN
    WebElement pawn2 = driver.findElement(new ById(pawnId2.toString()));
    pawn2.click();

    // THEN
    WebElement pawn1Outline = driver.findElement(By.className(pawnId1+"Overlay"));
    assertEquals("hidden", pawn1Outline.getCssValue("visibility"));

    WebElement pawn2Outline = driver.findElement(By.className(pawnId2+"Overlay"));
    assertEquals("visible", pawn2Outline.getCssValue("visibility"));
  }

  @Test
  public void clickOnOtherPawnOnBase_NotSelected(){
    // GIVEN
    WebElement pawnOtherPlayer = driver.findElement(new ById("PawnId{1,1}"));

    // WHEN
    pawnOtherPlayer.click();

    // THEN
    WebElement pawnOtherPlayerOutline = driver.findElement(By.className("PawnId{1,1}Overlay"));
    assertEquals("hidden", pawnOtherPlayerOutline.getCssValue("visibility"));
  }

  @Test
  public void playerCanMoveOnBoardAndPlayWithoutHavingToRefreshPage(){
    PawnId pawnId = new PawnId("2",0);

    // GIVEN player 0 forfeits
    playerForfeits(driver, "0");

    // GIVEN player 1 forfeits
    playerForfeits(driver, "1");

    // GIVEN player 2 moves on board
    Point nest = getPawnLocation(driver, pawnId);
    playerPlaysCard(driver, "2", pawnId, 1);

    // WHEN
    Point before = getPawnLocation(driver, pawnId);
    playerPlaysCard(driver, "2", pawnId, 5);
    TestUtils.wait(1000);
    Point after = getPawnLocation(driver, pawnId);

    // THEN
    assertPointsNotEqual("The pawn did not move from the nest tile", nest, before);
    assertPointsNotEqual("The pawn did not move from the start tile", before, after);
    assertTrue(pawnIsSelected(driver, pawnId));
  }
}