package ADG.Games.Keezen.FrontEnd;

import static ADG.Games.Keezen.FrontEnd.TestUtils.getDriver;
import static ADG.Games.Keezen.FrontEnd.TestUtils.playerForfeits;
import static ADG.Games.Keezen.FrontEnd.TestUtils.setPlayerIdPlaying;
import static org.junit.Assert.assertEquals;

import ADG.Games.Keezen.FrontEnd.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PlayerStatus {
  WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");

    SpringAppTestHelper.startTestApp();
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
  public void player0IsPlayingWhenStartingGame(){
    WebElement player0 = driver.findElement(By.id("player0"));
    assertEquals("playerPlaying", player0.getAttribute("class"));
  }

  @Test
  public void player1IsPlayingWhenPlayer0Forfeits() throws InterruptedException {
    playerForfeits(driver, "0");



    WebElement player1 = driver.findElement(By.id("player0"));
    assertEquals("playerNotPlaying", player1.getAttribute("class"));
  }
}
