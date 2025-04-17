package ADG.Games.Keezen.FrontEnd;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class TestUtils {

  public static WebDriver getDriver(){
    WebDriver driver = new ChromeDriver();
    driver.get("http://localhost:4200/");
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    return driver;
  }
}
