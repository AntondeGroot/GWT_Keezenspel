package ADG.Games.Keezen.FrontEnd;

import static ADG.Games.Keezen.FrontEnd.TestUtils.findCardByIndex;
import static ADG.Games.Keezen.FrontEnd.TestUtils.getDriver;
import static ADG.Games.Keezen.FrontEnd.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.FrontEnd.TestUtils.waitUntilDOMElementUpdates;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CardTest {
  WebDriver driver;

  @BeforeAll
  public static void skipIfDisabled() {
    // Todo: do not exclude selenium tests in CI
    if (Boolean.getBoolean("skip.selenium")) {
      Assumptions.assumeTrue(false, "Skipping Selenium tests");
    }
  }

  @BeforeEach
  public void setUp() {
    driver = getDriver();
    setPlayerIdPlaying(driver,"0");
  }

  @AfterEach
  public void tearDown() {
    driver.quit();
  }

  @Test
  public void clickOnCard_BorderVisible(){
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
  public void clickOnCardTwice_BorderHidden(){
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
  public void clickOnCard_ClickOtherCard_BorderFirstCardHidden(){
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
}
