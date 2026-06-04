package adg.keezen.IntegrationTests;

import static adg.keezen.IntegrationTests.Utils.TestUtils.clickById;
import static adg.keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static adg.keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import adg.keezen.IntegrationTests.Utils.TestUtils;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.cards.Card;
import adg.keezen.utils.BaseIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CardAnimation_IT extends BaseIntegrationTest {

  static WebDriver driver;
  static String sessionId;
  static String player0Id;

  @BeforeAll
  static void setUp() {
    sessionId = ApiUtil.createStandardGame();
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    driver = getDriver(sessionId);
    setPlayerIdPlaying(driver, player0Id);
  }

  @AfterAll
  static void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  void clickSend_withCardButNoPawn_cardRemainsInHand() {
    // GIVEN a card is available and selected, but no pawn is chosen
    int cardValue = 5;
    ApiUtil.setCardForPlayer(sessionId, player0Id, cardValue);
    driver.navigate().refresh();
    waitUntilCardsAreLoaded(driver);

    String cardId = new Card(0, cardValue).toString();
    clickById(driver, cardId);

    // WHEN clicking Send without a pawn (server will reject the move)
    driver.findElement(By.className("sendButton")).click();

    // THEN the card must still be in the player's hand — animation must not have removed it
    TestUtils.wait(700); // allow enough time for the HTTP round-trip to complete
    List<WebElement> cards = driver.findElements(By.id(cardId));
    assertFalse("Card element should still be in the DOM after a rejected move", cards.isEmpty());
    assertTrue("Card should be displayed in hand after a rejected move", cards.getFirst().isDisplayed());
  }
}