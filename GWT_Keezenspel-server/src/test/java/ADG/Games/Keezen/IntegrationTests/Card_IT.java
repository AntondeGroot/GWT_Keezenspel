package ADG.Games.Keezen.IntegrationTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.findCardByIndex;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.waitUntilDOMElementUpdates;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@ExtendWith(ScreenshotOnFailure.class)
public class Card_IT {

  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
    setPlayerIdPlaying(driver, "player0");
  }

  @AfterEach
  public void tearDown() {
    SpringAppTestHelper.stopApp();
    driver.quit();
  }

  /***
   * in order to use ScreenshotOnFailure, the webdriver should not be quit in the
   * @AfterEach tearDown(), because then the driver would no longer be accessible
   * to take a screenshot with.
   *
   * The driver.quit() should then be put in the @AfterAll which comes after the TestWatcher
   * is done. This however is a static method, requiring the webdriver to be static as well.
   */
  @AfterAll
  public static void tearDownAll() {
    // needed for skipping the selenium tests in CI
    if (driver != null) {
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
  }

  @Test
  public void clickOnCard_BorderVisible() {
    // GIVEN
    WebElement card0 = findCardByIndex(driver, "cardDiv", 0);

    // WHEN
    card0.click();

    // THEN
    waitUntilDOMElementUpdates(driver, "cardDiv");
    // Re-fetch the element after DOM changed
    WebElement updatedCard0 = findCardByIndex(driver, "cardDiv", 0);
    assertEquals("3px", updatedCard0.getCssValue("border-width"));
    assertEquals("rgb(255, 0, 0)", updatedCard0.getCssValue("border-color"));
  }

  @Test
  public void clickOnCardTwice_BorderHidden() {
    // GIVEN
    // you already clicked on a card
    WebElement card0 = findCardByIndex(driver, "cardDiv", 0);
    card0.click();
    waitUntilDOMElementUpdates(driver, "cardDiv");

    // WHEN
    // click again
    card0 = findCardByIndex(driver, "cardDiv", 0);
    card0.click();
    waitUntilDOMElementUpdates(driver, "cardDiv");

    // THEN
    // Re-fetch the element after DOM changed
    WebElement updatedCard0 = findCardByIndex(driver, "cardDiv", 0);
    assertEquals("0px", updatedCard0.getCssValue("border-width"));
    assertEquals("rgb(0, 0, 0)", updatedCard0.getCssValue("border-color"));
  }

  @Test
  public void clickOnCard_ClickOtherCard_BorderFirstCardHidden() {
    // GIVEN
    WebElement card0 = findCardByIndex(driver, "cardDiv", 0);
    card0.click();
    waitUntilDOMElementUpdates(driver, "cardDiv");

    // WHEN
    // click on another card
    WebElement card1 = findCardByIndex(driver, "cardDiv", 1);
    card1.click();
    waitUntilDOMElementUpdates(driver, "cardDiv");

    // THEN
    // Re-fetch the element after DOM changed
    WebElement updatedCard0 = findCardByIndex(driver, "cardDiv", 0);
    assertEquals("0px", updatedCard0.getCssValue("border-width"));
    assertEquals("rgb(0, 0, 0)", updatedCard0.getCssValue("border-color"));
  }

  @Test
  public void splitBoxes_DefaultNotShown() {
    WebElement pawnIntegerBox = driver.findElement(By.className("pawnIntegerBoxes"));
    assertFalse(pawnIntegerBox.isDisplayed());
  }
}
