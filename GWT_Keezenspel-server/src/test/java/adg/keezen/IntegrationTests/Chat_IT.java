package adg.keezen.IntegrationTests;

import static adg.keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static adg.keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreLoaded;
import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilVisible;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.utils.BaseIntegrationTest;
import adg.keezen.IntegrationTests.Utils.ChatServerMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


// optimized before 18s -> 9 s -> 1s
@TestMethodOrder(OrderAnnotation.class)
class Chat_IT extends BaseIntegrationTest {

  static WebDriver driver;

  @BeforeAll
  static void setUp() {
    String sessionId = ApiUtil.createStandardGame();
    driver = getDriver(sessionId);
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid(sessionId,0));
    waitUntilCardsAreLoaded(driver);
  }

  @AfterAll
  static void tearDown() {
    ChatServerMock.stop();
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  @Order(3)
  public void chatSendButtonIsVisible() {
    ChatServerMock.start();
    waitUntilVisible(driver, "chatSendButton");
    WebElement sendButton = driver.findElement(By.className("chatSendButton"));
    assertTrue(sendButton.isDisplayed(), "Chat send button should be visible when chat server is available");
  }

  @Test
  @Order(4)
  public void chatSendButtonIsJustAboveFooter() {
    ChatServerMock.start();
    waitUntilVisible(driver, "chatSendButton");
    WebElement sendButton = driver.findElement(By.className("chatSendButton"));
    WebElement footer = driver.findElement(By.tagName("footer"));

    int buttonBottom = sendButton.getLocation().getY() + sendButton.getSize().getHeight();
    int footerTop = footer.getLocation().getY();

    assertTrue(
        buttonBottom < footerTop,
        "Chat send button bottom (" + buttonBottom + "px) should be above footer top (" + footerTop + "px)");
    assertTrue(
        footerTop - buttonBottom <= 60,
        "Chat send button should be just above the footer, but was " + (footerTop - buttonBottom) + "px away");
  }

  @Test
  @Order(1)
  public void chatInputIsNotVisibleWhenChatServerIsDown() {
    // No ChatServerMock started — polls will fail
    WebElement sendButton = driver.findElement(By.className("chatSendButton"));
    WebElement inputField = driver.findElement(By.className("chatInputField"));
    assertFalse(sendButton.isDisplayed(), "Chat send button should not be visible when chat server is unavailable");
    assertFalse(inputField.isDisplayed(), "Chat input field should not be visible when chat server is unavailable");
  }

  @Test
  @Order(2)
  public void chatInputBecomesVisibleWhenChatServerComesBack() {
    // Now bring the chat server up
    ChatServerMock.start();
    waitUntilVisible(driver, "chatSendButton");
    WebElement sendButton = driver.findElement(By.className("chatSendButton"));
    WebElement inputField = driver.findElement(By.className("chatInputField"));
    assertTrue(sendButton.isDisplayed(), "Chat send button should become visible once chat server is available");
    assertTrue(inputField.isDisplayed(), "Chat input field should become visible once chat server is available");
  }
}