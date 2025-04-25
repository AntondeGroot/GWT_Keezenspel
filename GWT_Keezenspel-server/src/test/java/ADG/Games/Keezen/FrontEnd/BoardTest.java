package ADG.Games.Keezen.FrontEnd;

import ADG.Games.Keezen.FrontEnd.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
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
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
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
  public void verifyTitle() {
    assertEquals("Keezenspel", driver.getTitle());
  }

  @Test
  public void verifyNumberOfTilesFor3Players(){
    assertEquals(24*3, driver.findElements(By.className("tile")).size());
  }

  @Test
  public void verifyNumberOfPawnsFor3Players(){
    assertEquals(12, driver.findElements(By.className("pawnDiv")).size());
  }

  @Test
  public void verifyNumberOfCardsForPlayer1(){
    setPlayerIdPlaying(driver,"0");

    assertEquals(5, driver.findElements(By.className("cardDiv")).size());
  }
}