package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickCardByValue;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPawn;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPlayCardButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilPawnStopsMoving;
import static org.junit.Assert.assertEquals;

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
  private final PawnId pawnId10 = new PawnId("1",0);
  private final PawnId pawnId20 = new PawnId("2",0);

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startTestApp();
    driver = getDriver();
    setPlayerIdPlaying(driver,"0");
  }

  @AfterAll
  public static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
  }

  @Test
  public void playersSwitchWithJack(){
    // GIVEN
    waitUntilCardsAreLoaded(driver);

    playerForfeits(driver, "0");

    setPlayerIdPlaying(driver, "1");
    clickPawn(driver, pawnId10);
    clickCardByValue(driver, 1);
    clickPlayCardButton(driver);
    TestUtils.wait(400);

    setPlayerIdPlaying(driver, "2");
    clickPawn(driver, pawnId20);
    clickCardByValue(driver, 1);
    clickPlayCardButton(driver);
    waitUntilPawnStopsMoving(driver, pawnId20);

    setPlayerIdPlaying(driver, "1");
    clickPawn(driver, pawnId10);
    clickCardByValue(driver, 2);
    clickPlayCardButton(driver);

    // now player 2 can switch using a Jack
    setPlayerIdPlaying(driver, "2");
    Point positionPlayer2 = clickPawn(driver, pawnId20); // this deselects player 1
    clickCardByValue(driver, 11); // this should enable selecting player 1's pawn
    Point positionPlayer1 = clickPawn(driver, pawnId10);
    clickPlayCardButton(driver);

    waitUntilPawnStopsMoving(driver, pawnId10);
    // THEN
    TestUtils.wait(400);

    System.out.println("original position player 1 :"+positionPlayer2+" original position player 2 :"+positionPlayer1);
    assertEquals("Expected player 2 to be: ", positionPlayer1, clickPawn(driver, pawnId20));
    assertEquals("Expected player 1 to be:", positionPlayer2, clickPawn(driver, pawnId10));
  }
}
