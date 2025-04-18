package ADG.Games.Keezen.FrontEnd;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestUtils {

  public static WebDriver getDriver(){
    WebDriver driver = new ChromeDriver();
    driver.get("http://localhost:4200/");
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    return driver;
  }

  public static void setPlayerIdPlaying(WebDriver driver, String playerId){
    Cookie playerCookie = new Cookie("playerid", playerId);
    driver.manage().addCookie(playerCookie);
  }


  /***
   * A WebElement is no longer valid after you make changes to the DOM
   * E.g. change a CSS value.
   * You will then need to wait for the DOM to update and then fetch the WebElement again.
   * @param driver
   * @param id
   */
  public static void waitUntilDOMElementUpdates(WebDriver driver, String id){
    // Re-fetch the element after DOM changed
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
      wait.until(driverTemp -> {
      try {
        WebElement updatedCard = driverTemp.findElement(By.id(id));
        updatedCard.getAttribute("id");
        return true;
      } catch (StaleElementReferenceException e) {
        return false;
      }
    });
  }
}
