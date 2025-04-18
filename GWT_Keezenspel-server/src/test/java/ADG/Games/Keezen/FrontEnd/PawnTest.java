package ADG.Games.Keezen.FrontEnd;

import static ADG.Games.Keezen.FrontEnd.TestUtils.getDriver;
import static ADG.Games.Keezen.FrontEnd.TestUtils.setPlayerIdPlaying;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PawnTest {
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
  public void clickOnOwnPawn_Selected() throws InterruptedException {
    // GIVEN
    WebElement pawn1 = driver.findElement(new ById("PawnId{0,1}"));

    // WHEN
    pawn1.click();

    // THEN
    // todo: this should actually be by.Id, but for some reason that seems to break the toggle functionality
    WebElement pawn1Outline = driver.findElement(By.className("PawnId{0,1}Overlay"));
    assertEquals("visible", pawn1Outline.getCssValue("visibility"));
  }

  @Test
  public void clickOnOwnPawn_clickSecondPawn_FirstPawnDeselected(){
    // GIVEN
    WebElement pawn1 = driver.findElement(new ById("PawnId{0,1}"));
    pawn1.click();

    // WHEN
    WebElement pawn2 = driver.findElement(new ById("PawnId{0,2}"));
    pawn2.click();

    // THEN
    // todo: this should actually be by.Id, but for some reason that seems to break the toggle functionality
    WebElement pawn1Outline = driver.findElement(By.className("PawnId{0,1}Overlay"));
    assertEquals("hidden", pawn1Outline.getCssValue("visibility"));

    WebElement pawn2Outline = driver.findElement(By.className("PawnId{0,2}Overlay"));
    assertEquals("visible", pawn2Outline.getCssValue("visibility"));
  }

  @Test
  public void clickOnOtherPawnOnBase_NotSelected(){
    // GIVEN
    WebElement pawnOtherPlayer = driver.findElement(new ById("PawnId{1,1}"));

    // WHEN
    pawnOtherPlayer.click();

    // THEN
    // todo: this should actually be by.Id, but for some reason that seems to break the toggle functionality
    WebElement pawnOtherPlayerOutline = driver.findElement(By.className("PawnId{1,1}Overlay"));
    assertEquals("hidden", pawnOtherPlayerOutline.getCssValue("visibility"));
  }
}
