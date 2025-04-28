package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickCardByValue;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickForfeitButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPlayCardButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPawn;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.Assert.assertEquals;

import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.Player.PawnId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@ExtendWith(ScreenshotOnFailure.class)
public class Winner_IT {

  static WebDriver driver;

  private final int[][] winningMoves = {
      {1, 4, 7},    // Pawn 0
      {1, 4, 6},    // Pawn 1
      {1, 4, 5},    // Pawn 2
      {1, 4, 3, 1}  // Pawn 3
  };

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
  public void letPlayer2Win_HeGetsFirstPrize(){
    // GIVEN player 0 forfeits
    waitUntilCardsAreLoaded(driver);
    clickForfeitButton(driver);

    // GIVEN player 1 forfeits
    setPlayerIdPlaying(driver,"1");
    clickForfeitButton(driver);

    // WHEN player 2 plays all cards until he wins
    // This is possible with the mocked CardsDeck as they never run out of cards to play
    setPlayerIdPlaying(driver,"2");
    for (int pawnNr = 0; pawnNr < 4; pawnNr++) {
      for (int step : winningMoves[pawnNr]) {
        clickPawn(driver, new PawnId("2",pawnNr));
        clickCardByValue(driver,step);
        clickPlayCardButton(driver);
      }
    }

    // Because the player does not actually play cards he has to forfeit at the end of his turn.
    clickForfeitButton(driver);

    // THEN player 2 got the first prize medal
    List<WebElement> medalForPlayer2 =  driver.findElements(By.id("player2Medal"));
    assertEquals(1, medalForPlayer2.size());
    assertEquals("Medal1", medalForPlayer2.get(0).getAttribute("class")); // player has the first prize
  }
}