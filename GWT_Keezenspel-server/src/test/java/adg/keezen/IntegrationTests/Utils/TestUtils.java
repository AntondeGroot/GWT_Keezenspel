package adg.keezen.IntegrationTests.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.cards.Card;
import adg.keezen.player.PawnId;
import adg.keezen.Point;
import adg.Log;
import java.time.Duration;
import java.util.List;
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
    WebDriver driver = new ChromeDriver(buildOptions());
    driver.get("http://localhost:4200/");
    driver.manage().addCookie(new Cookie("sessionid", "123"));
    driver.manage().addCookie(new Cookie("playerid", "player0"));
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    return driver;
  }

  public static WebDriver getDriver(String sessionId) {
    WebDriver driver = new ChromeDriver(buildOptions());
    driver.get("http://localhost:4200/");
    driver.manage().addCookie(new Cookie("sessionid", sessionId));
    driver.manage().addCookie(new Cookie("playerid", "player0"));
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    return driver;
  }

  private static ChromeOptions buildOptions() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--window-size=1920,1080");
    options.addArguments("--mute-audio");
    // unique profile dir prevents cross-instance conflicts in CI
    options.addArguments("--user-data-dir=/tmp/chrome-user-data-" + System.nanoTime());
    return options;
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
    try {
      // ignored because setting cookie for a player that is already playing
      // would otherwise result in an error, but that is just overly safe behavior
      driver.manage().addCookie(playerCookie);
    }catch (Exception ignored){};
    driver.navigate().refresh();
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
    String playerId = driver.manage().getCookieNamed("playerid").getValue();
    ApiUtil.setCardForPlayer(playerId, cardValue);
    driver.navigate().refresh();
    clickById(driver, new Card(0, cardValue).toString());
  }

  public static void clickCardByValue(WebDriver driver, String sessionId, int cardValue) {
    String playerId = driver.manage().getCookieNamed("playerid").getValue();
    ApiUtil.setCardForPlayer(sessionId, playerId, cardValue);
    driver.navigate().refresh();
    clickById(driver, new Card(0, cardValue).toString());
  }

  public static Point getPawnLocation(WebDriver driver, PawnId pawnId) {
    WebElement pawnElement = driver.findElement(By.id(pawnId.toString()));
    String x = pawnElement.getCssValue("left").replace("px", "");
    String y = pawnElement.getCssValue("top").replace("px", "");
    return new Point(Double.parseDouble(x), Double.parseDouble(y));
  }

  /**
   * Clicks the element with the given id, retrying if the DOM is updated between lookup and click
   * (StaleElementReferenceException). GWT's polling can re-render elements at any time, so a
   * bare findElement().click() is not reliable under parallel test load.
   */
  public static void clickById(WebDriver driver, String id) {
    new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
      try {
        d.findElement(By.id(id)).click();
        return true;
      } catch (StaleElementReferenceException e) {
        return false;
      }
    });
  }

  public static void scrollUp(WebDriver driver) {
    // useful when you select a Jack
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("window.scrollTo(0, 0);"); // Scroll to top
  }

  public static Point clickPawn(WebDriver driver, PawnId pawnId) {
    WebElement pawnElement = driver.findElement(By.id(pawnId.toString()));

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

  /** Returns the inline borderColor CSS value of .TextBoxForPawnSteps{pawnNr} as "rgb(r, g, b)". */
  public static String getStepBoxBorderColor(WebDriver driver, int pawnNr) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    return (String) js.executeScript(
        "return document.querySelector('.TextBoxForPawnSteps" + pawnNr + "').style.borderColor");
  }

  /** Returns the inline color CSS value of .pawn{pawnNr}Label as "rgb(r, g, b)". */
  public static String getPawnLabelColor(WebDriver driver, int pawnNr) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    return (String) js.executeScript(
        "return document.querySelector('.pawn" + pawnNr + "Label').style.color");
  }

  /** Converts a hex color like "#1e90ff" to the browser rgb() string "rgb(30, 144, 255)". */
  public static String hexToRgbCss(String hex) {
    int r = Integer.parseInt(hex.substring(1, 3), 16);
    int g = Integer.parseInt(hex.substring(3, 5), 16);
    int b = Integer.parseInt(hex.substring(5, 7), 16);
    return "rgb(" + r + ", " + g + ", " + b + ")";
  }

  public static boolean pawnIsSelected(WebDriver driver, PawnId pawnId) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    String opacity = (String) js.executeScript(
        "var pawn = document.getElementById(arguments[0]);" +
        "if (!pawn) return 'none';" +
        "var highlight = pawn.querySelector('.highlight');" +
        "if (!highlight) return 'none';" +
        "return highlight.style.opacity;",
        pawnId.toString()
    );
    Log.info("pawnIsSelected: opacity=" + opacity + " for pawn " + pawnId);
    return "1".equals(opacity);
  }

  public static void clickPlayCardButton(WebDriver driver) {
    WebElement sendButton = driver.findElement(By.className("sendButton"));
    assertTrue(
        "⚠️ sendButton is not enabled: it was not the player's turn", sendButton.isEnabled());
    sendButton.click();
  }

  public static void waitUntilPawnStopsMoving(WebDriver driver, PawnId pawnId) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(6));
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
    new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
      try {
        WebElement btn = d.findElement(By.className("forfeitButton"));
        if (!btn.isEnabled() || !btn.isDisplayed()) return false;
        btn.click();
        return true;
      } catch (StaleElementReferenceException e) {
        return false;
      }
    });

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

  public static void waitUntilVisible(WebDriver driver, String className) {
    new WebDriverWait(driver, Duration.ofSeconds(3))
        .until(d -> d.findElement(By.className(className)).isDisplayed());
  }

  public static void waitUntilHidden(WebDriver driver, String className) {
    new WebDriverWait(driver, Duration.ofSeconds(3))
        .until(d -> !d.findElement(By.className(className)).isDisplayed());
  }

  public static void assertPointsNotEqual(String msg, Point p1, Point p2) {
    boolean sameX = Math.abs(p1.getX() - p2.getX()) <= 2;
    boolean sameY = Math.abs(p1.getY() - p2.getY()) <= 2;

    if (sameX && sameY) {
      fail(msg + " Points are equal within tolerance");
    }
  }

  /** Waits until an element with the given id is present in the DOM (not necessarily visible). */
  public static void waitUntilPresent(WebDriver driver, String id) {
    new WebDriverWait(driver, Duration.ofSeconds(5))
        .until(d -> !d.findElements(By.id(id)).isEmpty());
  }

  /** Waits until no element with the given id exists in the DOM. */
  public static void waitUntilAbsent(WebDriver driver, String id) {
    new WebDriverWait(driver, Duration.ofSeconds(10))
        .until(d -> d.findElements(By.id(id)).isEmpty());
  }

  public static void assertPointsEqual(String msg, Point p1, Point p2) {
    assertEquals(msg, p1.getX(), p2.getX(), 2);
    assertEquals(msg, p1.getY(), p2.getY(), 2);
  }

  /**
   * Waits up to 5 seconds for the element with the given id to have exactly the expected CSS class
   * string. Use this instead of a bare findElement + getAttribute immediately after a GWT state
   * change, because GWT's polling cycle may not have re-rendered the element yet.
   */
  public static void waitForPlayerClass(WebDriver driver, String playerId, String expectedClass) {
    new WebDriverWait(driver, Duration.ofSeconds(5))
        .until(d -> expectedClass.equals(d.findElement(By.id(playerId)).getAttribute("class")));
  }

  /** Waits up to 5 seconds for all .cardDiv elements to disappear from the DOM. */
  public static void waitUntilCardsAreGone(WebDriver driver) {
    new WebDriverWait(driver, Duration.ofSeconds(5))
        .until(d -> d.findElements(By.className("cardDiv")).isEmpty());
  }

  /** Waits up to 5 seconds for at least one .cardDiv to be visible. Throws on timeout. */
  public static void waitUntilCardsAreVisible(WebDriver driver) {
    new WebDriverWait(driver, Duration.ofSeconds(5))
        .until(d -> d.findElements(By.className("cardDiv")).stream().anyMatch(WebElement::isDisplayed));
  }
}
