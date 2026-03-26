package ADG.Games.Keezen.IntegrationTests.Utils;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Player {

  public static void assertPlayerHasMedal(WebDriver driver, String playerId, int finishingPlace) {
    //todo: this is a clunky bug fix, I expected player2 but got player 2, i Winner_IT test.
    String medalId = playerId+"Medal";
//    String medalId = playerId.replaceAll("([a-zA-Z])(\\d)", "$1 $2") + "Medal";
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(7));
    try {
      wait.until(driver1 -> {
        List<WebElement> containers = driver1.findElements(By.className("playerListContainer"));
        if (containers.isEmpty()) return false;
        return !containers.get(0).findElements(By.id(medalId)).isEmpty();
      });
    } catch (TimeoutException e) {
      List<WebElement> containers = driver.findElements(By.className("playerListContainer"));
      String html = containers.isEmpty()
          ? "(playerListContainer not found)"
          : containers.get(0).getAttribute("innerHTML");
      int len = Math.min(html.length(), 2000);
      throw new AssertionError(
          "Medal element with id='" + medalId + "' not found after 7s.\n"
              + "playerListContainer HTML (first 2000 chars):\n" + html.substring(0, len), e);
    }
    WebElement container = driver.findElement(By.className("playerListContainer"));
    List<WebElement> medalForPlayer = container.findElements(By.id(medalId));
    assertEquals(1, medalForPlayer.size());
    assertEquals("Medal" + finishingPlace, medalForPlayer.get(0).getAttribute("class"));
  }
}
