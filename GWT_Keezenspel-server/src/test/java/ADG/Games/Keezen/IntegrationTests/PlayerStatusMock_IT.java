package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.Steps.playerPlaysCard;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.playerForfeits;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static org.junit.Assert.assertEquals;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.utils.BaseIntegrationTest;
import ADG.Games.Keezen.IntegrationTests.Utils.Steps;
import ADG.Games.Keezen.Player.PawnId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

// optimized before : 34 seconds after 7 seconds
@TestMethodOrder(OrderAnnotation.class)
public class PlayerStatusMock_IT extends BaseIntegrationTest {

  static WebDriver driver;
  static String sessionId;
  static String player0Id;
  static String player1Id;
  static String player2Id;

  @BeforeAll
  static void setUp() {
    sessionId = ApiUtil.createStandardGame();
    driver = getDriver(sessionId);
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid(sessionId,0));
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    player1Id = ApiUtil.getPlayerid(sessionId, 1);
    player2Id = ApiUtil.getPlayerid(sessionId, 2);
    waitUntilCardsAreLoaded(driver);
  }

  @AfterAll
  public static void tearDownAll() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  @Order(1)
  public void player0IsPlayingWhenStartingGame() {
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid(sessionId,0));
    waitUntilCardsAreLoaded(driver);
    WebElement player0 = driver.findElement(By.id(player0Id));
    assertEquals("playerPlaying playerActive", player0.getAttribute("class"));
  }

  @Test
  @Order(2)
  public void player0IsInactiveAfterForfeit_Player1IsActive() throws InterruptedException {
    // WHEN
    Steps.playerForfeits(driver, player0Id);

    // THEN
    setPlayerIdPlaying(driver, player1Id);
    waitUntilCardsAreLoaded(driver);
    WebElement player1 = driver.findElement(By.id(player1Id));
    assertEquals("playerPlaying playerActive", player1.getAttribute("class"));
    WebElement player0 = driver.findElement(By.id(player0Id));
    assertEquals("playerNotPlaying playerInactive", player0.getAttribute("class"));
  }

  @Test
  @Order(3)
  public void player2IsPlayingWhen0And1Forfeit() throws InterruptedException {
    // WHEN
    playerForfeits(driver, player1Id);
    setPlayerIdPlaying(driver, player2Id);
    waitUntilCardsAreLoaded(driver);
    // THEN
    WebElement player = driver.findElement(By.id(player2Id));
    assertEquals("playerPlaying playerActive", player.getAttribute("class"));
  }

  @Test
  @Order(4)
  public void player2IsStilActiveAfterPlayingCard() throws InterruptedException {
    // GIVEN game started

    // WHEN
    PawnId pawnId20 = new PawnId(player2Id, 0);
    playerPlaysCard(driver, sessionId, player2Id, pawnId20, 1);
    waitUntilCardsAreLoaded(driver);

    // THEN
    WebElement player0 = driver.findElement(By.id(player2Id));
    assertEquals("playerPlaying playerActive", player0.getAttribute("class"));
  }
}
