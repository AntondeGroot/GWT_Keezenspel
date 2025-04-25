package ADG.Games.Keezen.FrontEnd;

import static ADG.Games.Keezen.FrontEnd.TestUtils.findCardByIndex;
import static ADG.Games.Keezen.FrontEnd.TestUtils.getDriver;
import static ADG.Games.Keezen.FrontEnd.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.FrontEnd.TestUtils.waitUntilDOMElementUpdates;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import ADG.Games.Keezen.FrontEnd.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CardTest {
  WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
    setPlayerIdPlaying(driver,"0");
  }

  @AfterEach
  public void tearDown() {
    // needed for skipping the selenium tests in CI
    if(driver != null){
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
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

  @Test
  public void splitBoxes_DefaultNotShown(){
    WebElement pawnIntegerBox = driver.findElement(By.className("pawnIntegerBoxes"));
    assertFalse(pawnIntegerBox.isDisplayed());
  }
}
