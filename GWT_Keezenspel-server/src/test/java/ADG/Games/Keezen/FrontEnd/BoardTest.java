package ADG.Games.Keezen.FrontEnd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static ADG.Games.Keezen.FrontEnd.TestUtils.getDriver;
import static ADG.Games.Keezen.FrontEnd.TestUtils.setPlayerIdPlaying;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardTest {

  WebDriver driver;

  @BeforeEach
  public void setUp() {
    driver = getDriver();
  }

  @AfterEach
  public void tearDown() {
    driver.quit();
  }

  @Test
  public void verifyTitle() {
    assertEquals("Keezenspel", driver.getTitle());
  }

  @Test
  public void verifyNumberOfTilesFor3Players(){
    assertEquals(24*3, driver.findElements(By.className("circle")).size());
  }

  @Test
  public void verifyNumberOfPawnsFor3Players(){
    assertEquals(12, driver.findElements(By.className("pawn")).size());
  }

  @Test
  public void verifyNumberOfCardsForPlayer1(){
    setPlayerIdPlaying(driver,"0");

    assertEquals(5, driver.findElements(By.className("cardDiv")).size());
  }
}