package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.IntegrationTests.Utils.ChatServerMock;
import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.IntegrationTests.Utils.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@ExtendWith(ScreenshotOnFailure.class)
class Chat_IT {

  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
    setPlayerIdPlaying(driver, ApiUtil.getPlayerid("123", 0));
  }

  @AfterEach
  public void tearDown() {
    ChatServerMock.stop();
    SpringAppTestHelper.stopApp();
    if (driver != null) {
      driver.quit();
    }
  }

  @AfterAll
  public static void tearDownAll() {
    ChatServerMock.stop();
    if (driver != null) {
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
  }

  @Test
  public void chatSendButtonIsVisible() {
    ChatServerMock.start();
    TestUtils.wait(1500);
    WebElement sendButton = driver.findElement(By.className("chatSendButton"));
    assertTrue(sendButton.isDisplayed(), "Chat send button should be visible when chat server is available");
  }

  @Test
  public void chatSendButtonIsJustAboveFooter() {
    ChatServerMock.start();
    TestUtils.wait(1500);
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
  public void chatInputIsNotVisibleWhenChatServerIsDown() {
    // No ChatServerMock started — polls will fail
    TestUtils.wait(1500);
    WebElement sendButton = driver.findElement(By.className("chatSendButton"));
    WebElement inputField = driver.findElement(By.className("chatInputField"));
    assertFalse(sendButton.isDisplayed(), "Chat send button should not be visible when chat server is unavailable");
    assertFalse(inputField.isDisplayed(), "Chat input field should not be visible when chat server is unavailable");
  }

  @Test
  public void chatInputBecomesVisibleWhenChatServerComesBack() {
    // First confirm both are hidden with no server
    TestUtils.wait(1500);
    WebElement sendButton = driver.findElement(By.className("chatSendButton"));
    WebElement inputField = driver.findElement(By.className("chatInputField"));
    assertFalse(sendButton.isDisplayed(), "Chat send button should not be visible before chat server starts");
    assertFalse(inputField.isDisplayed(), "Chat input field should not be visible before chat server starts");

    // Now bring the chat server up
    ChatServerMock.start();
    TestUtils.wait(1500);
    sendButton = driver.findElement(By.className("chatSendButton"));
    inputField = driver.findElement(By.className("chatInputField"));
    assertTrue(sendButton.isDisplayed(), "Chat send button should become visible once chat server is available");
    assertTrue(inputField.isDisplayed(), "Chat input field should become visible once chat server is available");
  }
}