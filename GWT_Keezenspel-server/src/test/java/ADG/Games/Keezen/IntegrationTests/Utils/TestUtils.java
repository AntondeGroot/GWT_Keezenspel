package ADG.Games.Keezen.IntegrationTests.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.Point;
import ADG.Log;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

/***
 *  WARNING
 *  Changes to this file may severely impact performance for the Selenium tests.
 *  Test before committing any changes.
 */
public class TestUtils {

  public static WebDriver getDriver() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--window-size=1920,1080");

    // this line is here to fix a CI error
    options.addArguments("--user-data-dir=/tmp/chrome-user-data-" + System.nanoTime());
    WebDriver driver = new ChromeDriver(options);
    driver.get("http://localhost:4200/");
    driver.manage().addCookie(new Cookie("sessionid", "123"));
    driver.manage().addCookie(new Cookie("playerid", "player0"));
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    return driver;
  }

  public static void waitUntilCardsAreLoaded(WebDriver driver) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

    try {
      wait.until(
          driver1 -> {
            try {
              return driver1.findElements(By.className("cardDiv")).stream()
                  .anyMatch(WebElement::isDisplayed);
            } catch (StaleElementReferenceException e) {
              return false;
            }
          });
    } catch (WebDriverException timeoutException) {
      Log.info("⚠️ Timeout waiting for game elements — continuing without failure.");
      // Optionally: set a flag or take fallback action
    }
  }

  public static void setPlayerIdPlaying(WebDriver driver, String playerId) {
    Cookie playerCookie = new Cookie("playerid", playerId);
    driver.manage().addCookie(playerCookie);
    driver.navigate().refresh();
    wait(200);
  }

  /***
   * A WebElement is no longer valid after you make changes to the DOM
   * E.g. change a CSS value.
   * You will then need to wait for the DOM to update and then fetch the WebElement again.
   * @param driver
   * @param className
   */
  public static void waitUntilDOMElementUpdates(WebDriver driver, String className) {
    // Re-fetch the element after DOM changed
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
    wait.until(
        driverTemp -> {
          try {
            WebElement updatedCard = driverTemp.findElement(By.className(className));
            updatedCard.getAttribute("id");
            return true;
          } catch (StaleElementReferenceException e) {
            return false;
          }
        });
  }

  public static WebElement findCardByIndex(WebDriver driver, String className, int index) {
    List<WebElement> cards = driver.findElements(By.className(className));
    return cards.get(index);
  }

  public static void clickCardByValue(WebDriver driver, int cardValue) {
    WebElement card = driver.findElement(By.id(new Card(0, cardValue).toString()));
    String initialBorder = card.getCssValue("border-color");

    Log.info("border: " + initialBorder);
    if (initialBorder.equals("rgb(0, 0, 0)")) {
      card.click();
    } else {
      // in a mocked cardsdeck you can keep on playing the exact same card. This does not change
      // movetype for an ace. Realistically you would chose another Ace card if you had two, this
      // would trigger the reevaluation of the movetype
      card.click();
      // after clicking the card it will become stale
      card = driver.findElement(By.id(new Card(0, cardValue).toString()));
      card.click();
    }
  }

  public static Point getPawnLocation(WebDriver driver, PawnId pawnId) {
    WebElement pawnElement = driver.findElement(By.id(pawnId.toString()));
    String x = pawnElement.getCssValue("left").replace("px", "");
    String y = pawnElement.getCssValue("top").replace("px", "");
    return new Point(Double.parseDouble(x), Double.parseDouble(y));
  }

  public static void scrollUp(WebDriver driver) {
    // useful when you select a Jack
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("window.scrollTo(0, 0);"); // Scroll to top
    TestUtils.wait(200);
  }

  public static Point clickPawn(WebDriver driver, PawnId pawnId) {
    WebElement pawnOverlay = driver.findElement(By.className(pawnId + "Overlay"));
    WebElement pawnElement = driver.findElement(By.id(pawnId.toString()));

    // Check if the overlay is not visible
    if (!pawnIsSelected(driver, pawnId)) {
      pawnElement.click();
    } else {
      pawnElement.click();

      pawnElement = driver.findElement(By.id(pawnId.toString()));
      pawnElement.click();
    }

    String x = pawnElement.getCssValue("left").replace("px", "");
    String y = pawnElement.getCssValue("top").replace("px", "");
    return new Point(Double.parseDouble(x), Double.parseDouble(y));
  }

  public static boolean pawnIsSelected(WebDriver driver, PawnId pawnId) {
    WebElement updatedElement = driver.findElement(By.className(pawnId.toString() + "Overlay"));
    String output = updatedElement.getCssValue("visibility");
    Log.info("pawnIsSelected: " + output + " for element " + updatedElement);
    return Objects.equals(output, "visible");
  }

  public static void clickPlayCardButton(WebDriver driver) {
    WebElement sendButton = driver.findElement(By.className("sendButton"));
    assertTrue(
        "⚠️ sendButton is not enabled: it was not the player's turn", sendButton.isEnabled());
    sendButton.click();
  }

  public static void waitUntilPawnStopsMoving(WebDriver driver, PawnId pawnId) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
    WebElement pawn = driver.findElement(By.id(pawnId.toString()));

    wait.until(
        driver1 -> {
          org.openqa.selenium.Point oldPosition = pawn.getLocation();
          try {
            Thread.sleep(100); // Wait a bit to allow animation to progress
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          org.openqa.selenium.Point newPosition = pawn.getLocation();

          // Check if position has stabilized
          if (oldPosition.equals(newPosition)) {
            return true;
          } else {
            return null; // keep waiting
          }
        });
  }

  public static void playerForfeits(WebDriver driver, String playerId) {
    setPlayerIdPlaying(driver, playerId);
    clickForfeitButton(driver);
  }

  public static void clickForfeitButton(WebDriver driver) {
    WebElement forfeitButton = driver.findElement(By.className("forfeitButton"));
    assertTrue(
        "⚠️ forfeitButton is not enabled: it was not the player's turn", forfeitButton.isEnabled());
    assertTrue("forfeitButton is not visible: ", forfeitButton.isDisplayed());
    forfeitButton.click();

    wait(200);

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
    try {
      wait.until(
          driver1 -> {
            try {
              return !driver1.findElement(By.className("forfeitButton")).isEnabled();
            } catch (StaleElementReferenceException e) {
              return false;
            }
          });
    } catch (WebDriverException timeoutException) {
      System.out.println("⚠️ Timeout waiting for game elements — continuing without failure.");
      // Optionally: set a flag or take fallback action
    }
  }

  /**
   * This method has to be called using TestUtils.wait() DO NOT USE only the method call wait()
   *
   * <p>TestUtils.wait() will execute it in the thread the test is running in wait() creates a
   * separate thread and will then not actually wait
   *
   * @param millis
   */
  public static void wait(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void assertPointsNotEqual(String msg, Point p1, Point p2) {
    assertNotEquals(msg, p1.getX(), p2.getX(), 2);
    assertNotEquals(msg, p1.getY(), p2.getY(), 2);
  }

  public static void assertPointsEqual(String msg, Point p1, Point p2) {
    assertEquals(msg, p1.getX(), p2.getX(), 2);
    assertEquals(msg, p1.getY(), p2.getY(), 2);
  }
}
