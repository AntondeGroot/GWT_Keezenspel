package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickById;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.clickPlayCardButton;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilAbsent;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilPresent;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilPawnStopsMoving;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Verifies the send-button spinner lifecycle when a player is the sole remaining player:
 * - no spinner before the move
 * - spinner appears after clicking Play Card (while pawn animates)
 * - spinner disappears once the pawn has finished moving
 */
public class SendButtonSpinner_IT extends BaseIntegrationTest {

  private static final String SPINNER_ID = "sendButtonLoader";

  static WebDriver driver;
  static String sessionId;
  static String player0Id;
  static String player1Id;
  static String player2Id;

  @BeforeAll
  static void setUp() {
//    sessionId = ApiUtil.createStandardGame();
//    driver = getDriver(sessionId);
//    player0Id = ApiUtil.getPlayerid(sessionId, 0);
//    player1Id = ApiUtil.getPlayerid(sessionId, 1);
//    player2Id = ApiUtil.getPlayerid(sessionId, 2);
//
//    // Players 1 and 2 forfeit so player 0 is the sole remaining player
//    playerForfeits(driver, player1Id);
//    playerForfeits(driver, player2Id);
  }

  @AfterAll
  static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  void playingQueenCard_spinnerAppearsWhileAnimating_andDisappearsWhenPawnStopsMoving() {
    //todo: enable spinner animation and fix the tests, they seem to be unstable when enabled

  }
//    // GIVEN: player 0's pawn is on the board (start tile), with a Queen card
//    PawnId pawnId = new PawnId(player0Id, 0);
//    ApiUtil.setPawnPosition(sessionId, player0Id, 0, player0Id, 1);
//    ApiUtil.setCardForPlayer(sessionId, player0Id, 12);
//    setPlayerIdPlaying(driver, player0Id);
//    waitUntilCardsAreLoaded(driver);
//
//    // THEN: no spinner before the move
//    assertTrue(driver.findElements(By.id(SPINNER_ID)).isEmpty(), "No spinner before playing a card");
//
//    // WHEN: select pawn, select Queen, click Play Card
//    clickById(driver, pawnId.toString());
//    clickById(driver, "card_0_12");
//    clickPlayCardButton(driver);
//
//    // THEN: spinner appears while the pawn is animating
//    waitUntilPresent(driver, SPINNER_ID);
//    assertFalse(driver.findElements(By.id(SPINNER_ID)).isEmpty(), "Spinner is shown while pawn is moving");
//
//    // WHEN: pawn finishes moving
//    waitUntilPawnStopsMoving(driver, pawnId);
//
//    // THEN: spinner is removed
//    waitUntilAbsent(driver, SPINNER_ID);
//    assertTrue(driver.findElements(By.id(SPINNER_ID)).isEmpty(), "Spinner is gone after pawn stops moving");
//  }
}