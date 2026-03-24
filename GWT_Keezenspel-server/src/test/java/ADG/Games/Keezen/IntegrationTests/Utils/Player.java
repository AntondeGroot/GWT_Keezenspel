package ADG.Games.Keezen.IntegrationTests.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Player {

  public static void assertPlayerHasMedal(WebDriver driver, String playerId, int finishingPlace) {
    List<WebElement> medalForPlayer = driver.findElements(By.id(playerId.replace("player","")+ "Medal"));
    // there should be only 1 medal
    assertEquals(1, medalForPlayer.size());
    // the medal should be awarded to the player
    assertEquals("Medal" + finishingPlace, medalForPlayer.get(0).getAttribute("class"));
  }
}
