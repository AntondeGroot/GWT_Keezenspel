package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import ADG.Games.Keezen.IntegrationTests.Utils.Steps;
import ADG.Games.Keezen.IntegrationTests.Utils.TestUtils;
import ADG.Games.Keezen.utils.ApiCallsHelper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

class CardDisplay_IT extends BaseIntegrationTest {

  static WebDriver driver;
  private final ApiCallsHelper apiHelper = new ApiCallsHelper();

  @BeforeEach
  public void setUp() {
    driver = getDriver();
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid("123", 0));
  }

  @AfterEach
  public void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @AfterAll
  public static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  public void afterGameStart_playerHandCards_areVisibleInHTML() {
    // GIVEN the game has started and the browser has loaded
    waitUntilCardsAreLoaded(driver);

    // WHEN fetching the player's hand card elements from the DOM
    List<WebElement> cards = driver.findElements(By.className("cardDiv"));

    // THEN the player's hand contains at least one visible card
    assertFalse("Player should have at least one hand card visible in the HTML", cards.isEmpty());
    assertTrue(
        "At least one hand card should be displayed",
        cards.stream().anyMatch(WebElement::isDisplayed));
  }

  @Test
  public void afterForfeit_playerHand_isEmpty() {
    // GIVEN the game has started and player0 has cards
    String player0Id = ApiUtil.getPlayerid("123", 0);
    setPlayerIdPlaying(driver, player0Id);
    waitUntilCardsAreLoaded(driver);

    List<WebElement> cardsBefore = driver.findElements(By.className("cardDiv"));
    assertFalse("Player should have cards before forfeiting", cardsBefore.isEmpty());

    // WHEN player0 forfeits
    Steps.playerForfeits(driver, player0Id);

    // THEN the player's hand is empty in the HTML (cards have been discarded)
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
    wait.until(
        d -> {
          try {
            List<WebElement> cardsAfter = d.findElements(By.className("cardDiv"));
            return cardsAfter.isEmpty();
          } catch (StaleElementReferenceException e) {
            return false;
          }
        });

    List<WebElement> cardsAfterForfeit = driver.findElements(By.className("cardDiv"));
    assertTrue(
        "Player's hand should be empty after forfeiting all cards", cardsAfterForfeit.isEmpty());
  }

  @Test
  public void afterForfeit_nextPlayer_hasHandCardsVisible() {
    // GIVEN player0 is the current player and forfeits
    String player0Id = ApiUtil.getPlayerid("123", 0);
    String player1Id = ApiUtil.getPlayerid("123", 1);
    Steps.playerForfeits(driver, player0Id);

    // WHEN switching to player1's view
    setPlayerIdPlaying(driver, player1Id);
    waitUntilCardsAreLoaded(driver);

    // THEN player1 sees their own hand cards in the HTML
    List<WebElement> cards = driver.findElements(By.className("cardDiv"));
    assertFalse("Player1 should have hand cards visible after player0 forfeits", cards.isEmpty());
  }

  @Test
  public void playerCards_areLoadedWithinReasonableTime() {
    // GIVEN the game page loads

    // WHEN waiting up to 2 seconds for cards to appear
    waitUntilCardsAreLoaded(driver);

    // THEN at least one cardDiv is visible — confirming pollServerForCards completed successfully
    List<WebElement> cards = driver.findElements(By.className("cardDiv"));
    assertFalse(
        "Cards should be visible in the DOM — pollServerForCards must have completed", cards.isEmpty());
    TestUtils.wait(200);
    cards = driver.findElements(By.className("cardDiv"));
    assertFalse(
        "Cards should remain visible after a short wait — polling must be stable", cards.isEmpty());
  }

  @Test
  public void afterForfeit_centralPlayedCardCount_matchesCardsDiscarded() {
    // GIVEN player0 has cards and we know how many before forfeiting
    String player0Id = ApiUtil.getPlayerid("123", 0);
    setPlayerIdPlaying(driver, player0Id);
    waitUntilCardsAreLoaded(driver);

    Map<String, Object> cardInfoBefore = apiHelper.getPubliclyAvailableCardInformation("123");
    Map<String, Integer> nrOfCardsPerPlayer =
        (Map<String, Integer>) cardInfoBefore.get("nrOfCardsPerPlayer");
    int cardsInHand = nrOfCardsPerPlayer.get(player0Id);

    // WHEN player0 forfeits all their cards
    Steps.playerForfeits(driver, player0Id);

    // THEN the canvas data attribute reflects the correct number of played cards
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
    WebElement canvas = driver.findElement(By.id("canvasCards2"));
    wait.until(
        d -> {
          try {
            String attr = canvas.getAttribute("data-played-count");
            return attr != null && Integer.parseInt(attr) >= cardsInHand;
          } catch (StaleElementReferenceException | NumberFormatException e) {
            return false;
          }
        });

    int displayedCount = Integer.parseInt(canvas.getAttribute("data-played-count"));
    assertEquals(
        "The number of cards shown in the central spot should equal the number of forfeited cards",
        cardsInHand,
        displayedCount);
  }
}