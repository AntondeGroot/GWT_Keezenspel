package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickCardByValue;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickForfeitButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPlayCardButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPawn;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.Assert.fail;

import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.IntegrationTests.Utils.TestUtils;
import ADG.Games.Keezen.Player.PawnId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(ScreenshotOnFailure.class)
public class Winner_IT {

  static WebDriver driver;

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
  public void letPlayer2Win(){
    // GIVEN player 0 forfeits
    waitUntilCardsAreLoaded(driver);
    clickForfeitButton(driver);

    // GIVEN player 1 forfeits
    setPlayerIdPlaying(driver,"1");
    clickForfeitButton(driver);

    // WHEN player 2 plays all cards until he wins
    // this is possible with the mocked cardsdeck as they never run out
    int[][] moves = {
        {1, 4, 7},    // Pawn 0
        {1, 4, 6},    // Pawn 1
        {1, 4, 5},    // Pawn 2
        {1, 4, 3, 2}  // Pawn 3
    };

    setPlayerIdPlaying(driver,"2");
    clickCardByValue(driver, 1);
    clickPawn(driver, new PawnId("2", 0));
    clickPlayCardButton(driver);


    driver.navigate().refresh();
    TestUtils.wait(1000);
//    waitUntilCardsAreLoaded(driver);
    clickPawn(driver, new PawnId("2", 0));
//    clickPawn(driver, new PawnId("2", 0));
//    clickCardByValue(driver,4);
//    clickPlayCardButton(driver);
//
//    clickCardByValue(driver,3);
//    clickPlayCardButton(driver);
//
//    driver.navigate().refresh();

//    todo: speed up animation time for when testing


//    for (int pawnNr = 0; pawnNr < 4; pawnNr++) {
//      TestUtils.wait(200);
//      clickPawn(driver, new PawnId("2",pawnNr));
//      for (int step : moves[pawnNr]) {
//        clickCardByValue(driver,moves[pawnNr][step]);
//        TestUtils.wait(200);
//        clickMakeMoveButton(driver);
//        TestUtils.wait(200);
//      }
//    }
    TestUtils.wait(3000);
    driver.navigate().refresh();
    TestUtils.wait(1000);
    fail();
  }

}