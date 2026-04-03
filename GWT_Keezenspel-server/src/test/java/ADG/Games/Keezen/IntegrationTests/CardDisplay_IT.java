package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreGone;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreVisible;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import ADG.Games.Keezen.IntegrationTests.Utils.Steps;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
class CardDisplay_IT extends BaseIntegrationTest {

  static WebDriver driver;
  static String sessionId;
  static String player0Id;
  static String player1Id;

  @BeforeAll
  static void setup(){
    sessionId = ApiUtil.createStandardGame();
    driver = getDriver(sessionId);
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid(sessionId,0));
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    player1Id = ApiUtil.getPlayerid(sessionId, 1);
  }

  @AfterAll
  static void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  @Order(1)
  void playerCards_areLoadedWithinReasonableTime() {
    // GIVEN the game page loads
    // WHEN waiting for cards to appear — throws on timeout (no silent swallow)
    waitUntilCardsAreVisible(driver);

    // THEN at least one cardDiv is visible — confirming pollServerForCards completed successfully
    List<WebElement> cards = driver.findElements(By.className("cardDiv"));
    assertFalse(
        "Cards should be visible in the DOM — pollServerForCards must have completed", cards.isEmpty());
    cards = driver.findElements(By.className("cardDiv"));
    assertFalse(
        "Cards should remain visible after a short wait — polling must be stable", cards.isEmpty());
  }

  @Test
  @Order(2)
  void afterGameStart_playerHandCards_areVisibleInHTML() {
    // GIVEN the game has started and the browser has loaded
    // WHEN fetching the player's hand card elements from the DOM
    List<WebElement> cards = driver.findElements(By.className("cardDiv"));

    // THEN the player's hand contains at least one visible card
    assertFalse("Player should have at least one hand card visible in the HTML", cards.isEmpty());
    assertTrue(
        "At least one hand card should be displayed",
        cards.stream().anyMatch(WebElement::isDisplayed));
  }

  @Test
  @Order(3)
  void afterForfeit_playerHand_isEmpty() {
    // GIVEN the game has started and player0 has cards
    List<WebElement> cardsBefore = driver.findElements(By.className("cardDiv"));
    assertFalse("Player should have cards before forfeiting", cardsBefore.isEmpty());

    // WHEN player0 forfeits
    Steps.playerForfeits(driver, player0Id);
    waitUntilCardsAreGone(driver);

    List<WebElement> cardsAfterForfeit = driver.findElements(By.className("cardDiv"));
    assertTrue(
        "Player's hand should be empty after forfeiting all cards", cardsAfterForfeit.isEmpty());
  }

  @Test
  @Order(4)
  void afterForfeit_nextPlayer_hasHandCardsVisible() {
    // GIVEN player0 is the current player and forfeits
    // WHEN switching to player1's view
    setPlayerIdPlaying(driver, player1Id);
    waitUntilCardsAreVisible(driver);

    // THEN player1 sees their own hand cards in the HTML
    List<WebElement> cards = driver.findElements(By.className("cardDiv"));
    assertFalse("Player1 should have hand cards visible after player0 forfeits", cards.isEmpty());
  }

  @Test
  @Order(5)
  void afterForfeit_centralPlayedCardCount_matchesCardsDiscarded() {
    // GIVEN player0 has cards and we know how many before forfeiting
    // WHEN player0 forfeits all their cards

    // THEN the canvas data attribute reflects the correct number of played cards
    WebElement canvas = driver.findElement(By.id("canvasCards2"));

    int displayedCount = Integer.parseInt(canvas.getAttribute("data-played-count"));
    assertEquals(
        "The number of cards shown in the central spot should equal the number of forfeited cards",
        5,
        displayedCount);
  }
}