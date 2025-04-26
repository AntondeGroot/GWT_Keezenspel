package ADG.Games.Keezen.FrontEnd.Utils;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotOnFailure implements TestWatcher {

  private WebDriver extractWebDriver(Object testInstance) {
    try {
      // Try to find a "driver" field
      var field = testInstance.getClass().getDeclaredField("driver");
      field.setAccessible(true);
      Object value = field.get(testInstance);
      if (value instanceof WebDriver) {
        return (WebDriver) value;
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      System.out.println("Could not extract WebDriver: " + e.getMessage());
    }
    return null;
  }

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    Object testInstance = context.getRequiredTestInstance();
    WebDriver driver = extractWebDriver(testInstance);
    if (driver != null) {
      takeScreenshot(driver, context.getDisplayName());
    } else {
      System.out.println("No WebDriver found to take screenshot.");
    }
  }

  private void takeScreenshot(WebDriver driver, String testName) {
    System.out.println("IS TAKING A SCREENSHOT");
    try {
      String timeForFilename = LocalTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss"));

      File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
      File destination = new File("screenshots/" + testName +"_"+timeForFilename+ ".png");
      //Now you can do whatever you need to do with it, for example copy somewhere
      FileUtils.copyFile(source, destination);
      System.out.println("Screenshot saved: " + destination.getAbsolutePath());
    } catch (Exception e) {
      System.out.println("Failed to save screenshot: " + e.getMessage());
    }
  }
}
