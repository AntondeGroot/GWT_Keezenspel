package ADG.Games.Keezen.FrontEnd;

import static ADG.Games.Keezen.FrontEnd.TestUtils.getDriver;
import static ADG.Games.Keezen.FrontEnd.TestUtils.setPlayerIdPlaying;
import static ADG.Games.Keezen.FrontEnd.TestUtils.waitUntilDOMElementUpdates;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CardTest {
  WebDriver driver;

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
    WebElement card0 = driver.findElement(new ById("card0"));

    // WHEN
    card0.click();

    // THEN
    waitUntilDOMElementUpdates(driver, "card0");
    // Re-fetch the element after DOM changed
    WebElement updatedCard0 = driver.findElement(By.id("card0"));
    assertEquals("3px", updatedCard0.getCssValue("border-width"));
    assertEquals("rgb(255, 0, 0)", updatedCard0.getCssValue("border-color"));
  }

  @Test
  public void clickOnCardTwice_BorderHidden(){
    // GIVEN
    // you already clicked on a card
    WebElement card0 = driver.findElement(new ById("card0"));
    card0.click();
    waitUntilDOMElementUpdates(driver, "card0");

    // WHEN
    // click again
    card0 = driver.findElement(By.id("card0"));
    card0.click();
    waitUntilDOMElementUpdates(driver, "card0");

    // THEN
    // Re-fetch the element after DOM changed
    WebElement updatedCard0 = driver.findElement(By.id("card0"));
    assertEquals("0px", updatedCard0.getCssValue("border-width"));
    assertEquals("rgb(0, 0, 0)", updatedCard0.getCssValue("border-color"));
  }

  @Test
  public void clickOnCard_ClickOtherCard_BorderFirstCardHidden(){
    // GIVEN
    WebElement card0 = driver.findElement(new ById("card0"));
    card0.click();
    waitUntilDOMElementUpdates(driver, "card0");

    // WHEN
    // click on another card
    WebElement card1 = driver.findElement(By.id("card1"));
    card1.click();
    waitUntilDOMElementUpdates(driver, "card1");

    // THEN
    // Re-fetch the element after DOM changed
    WebElement updatedCard0 = driver.findElement(By.id("card0"));
    assertEquals("0px", updatedCard0.getCssValue("border-width"));
    assertEquals("rgb(0, 0, 0)", updatedCard0.getCssValue("border-color"));
  }
}
