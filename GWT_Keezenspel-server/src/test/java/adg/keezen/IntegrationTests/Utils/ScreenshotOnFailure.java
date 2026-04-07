package adg.keezen.IntegrationTests.Utils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
/***
 * in order to use ScreenshotOnFailure, the webdriver should not be quit in the
 * @AfterEach tearDown(), because then the driver would no longer be accessible
 * to take a screenshot with.
 *
 * The driver.quit() should then be put in the @AfterAll which comes after the TestWatcher
 * is done. This however is a static method, requiring the webdriver to be static as well.
 */
public class ScreenshotOnFailure implements TestWatcher, BeforeAllCallback {

  private static final String SCREENSHOT_DIR = "screenshots";
  private static final DateTimeFormatter FILE_FORMAT =
      DateTimeFormatter.ofPattern("MM_dd___HH_mm_ss");

  private static boolean cleaned = false;

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!cleaned) {
      deleteOldScreenshots();
      cleaned = true;
    }
  }

  private WebDriver extractWebDriver(Object testInstance) {
    try {
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
    try {
      String timeForFilename = LocalDateTime.now().format(FILE_FORMAT);

      File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
      File destination = new File(
          SCREENSHOT_DIR + "/" + timeForFilename + "_" + sanitize(testName) + ".png"
      );

      FileUtils.copyFile(source, destination);
      System.out.println("Screenshot saved: " + destination.getAbsolutePath());

    } catch (Exception e) {
      System.out.println("Failed to save screenshot: " + e.getMessage());
    }
  }

  private void deleteOldScreenshots() {
    File dir = new File(SCREENSHOT_DIR);

    if (!dir.exists()) return;

    String todayPrefix = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("MM_dd")) + "___";

    File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));
    if (files == null) return;

    for (File file : files) {
      if (!file.getName().startsWith(todayPrefix)) {
        if (file.delete()) {
          System.out.println("Deleted old screenshot: " + file.getName());
        }
      }
    }
  }

  private String sanitize(String input) {
    return input.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
  }
}