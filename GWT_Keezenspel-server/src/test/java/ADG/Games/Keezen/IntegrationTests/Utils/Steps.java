package ADG.Games.Keezen.IntegrationTests.Utils;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickCardByValue;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickForfeitButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPawn;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPlayCardButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.pawnIsSelected;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.scrollUp;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilPawnStopsMoving;
import static org.junit.Assert.assertTrue;

import ADG.Games.Keezen.Player.PawnId;
import org.openqa.selenium.WebDriver;

public class Steps {
  public static void playerForfeits(WebDriver driver, String playerId) {
    setPlayerIdPlaying(driver,playerId);
    clickForfeitButton(driver);
  }

  public static void playerPlaysCard(WebDriver driver, String playerId, PawnId pawnId, int cardValue) {
    setPlayerIdPlaying(driver, playerId);
    waitUntilCardsAreLoaded(driver);
    clickCardByValue(driver, cardValue);
    clickPawn(driver, pawnId);

    TestUtils.wait(100);
    clickPlayCardButton(driver);
    waitUntilPawnStopsMoving(driver, pawnId);
  }

  public static void playerSwitchesPawns(WebDriver driver, String playerId, PawnId ownPawnId, PawnId otherPawnId) {
    setPlayerIdPlaying(driver, playerId);
    waitUntilCardsAreLoaded(driver);
    clickPawn(driver, ownPawnId);
    clickCardByValue(driver, 11);
    scrollUp(driver);
    clickPawn(driver, otherPawnId);
    assertTrue("Own pawn was not selected", pawnIsSelected(driver, ownPawnId));
    assertTrue("Other pawn was not selected", pawnIsSelected(driver, otherPawnId));
    clickPlayCardButton(driver);
    waitUntilPawnStopsMoving(driver, ownPawnId);
  }

  public static void whenPlayerWins(WebDriver driver, String playerId) {
    // WHEN player plays all cards until he wins
    // This is possible with the mocked CardsDeck as they never run out of cards to play
    int[][] winningMoves = {
        {1, 4, 7},    // Pawn 0
        {1, 4, 6},    // Pawn 1
        {1, 4, 5},    // Pawn 2
        {1, 4, 3, 1}  // Pawn 3
    };

    setPlayerIdPlaying(driver,playerId);
    for (int pawnNr = 0; pawnNr < 4; pawnNr++) {
      for (int step : winningMoves[pawnNr]) {
        playerPlaysCard(driver, playerId, new PawnId(playerId, pawnNr), step);
      }
    }
  }
}
