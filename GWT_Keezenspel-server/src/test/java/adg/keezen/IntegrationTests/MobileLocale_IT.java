package adg.keezen.IntegrationTests;

import static adg.keezen.IntegrationTests.Utils.TestUtils.waitUntilCardsAreVisible;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.utils.BaseIntegrationTest;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
class MobileLocale_IT extends BaseIntegrationTest {

    static WebDriver driver;

    @BeforeAll
    static void setUp() {
        String sessionId = ApiUtil.createStandardGame();
        String playerId = ApiUtil.getPlayerid(sessionId, 0);
        driver = buildMobileDriver(sessionId, playerId, "en");
        waitUntilCardsAreVisible(driver);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }

    private static WebDriver buildMobileDriver(String sessionId, String playerId, String locale) {
        Map<String, Object> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", "iPhone X");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("mobileEmulation", mobileEmulation);
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--mute-audio");
        options.addArguments("--user-data-dir=/tmp/chrome-user-data-" + System.nanoTime());

        WebDriver d = new ChromeDriver(options);
        d.get("http://localhost:4200/?sessionid=" + sessionId
                + "&playerid=" + playerId
                + "&locale=" + locale);
        d.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        return d;
    }

    @Test
    @Order(1)
    void mobileRedirectsToMobileHtml() {
        assertTrue(driver.getCurrentUrl().contains("mobile.html"),
                "Expected URL to contain mobile.html but was: " + driver.getCurrentUrl());
    }

    @Test
    @Order(2)
    void playButtonIsVisibleWithEnglishText() {
        WebElement sendButton = driver.findElement(By.className("sendButton"));
        assertTrue(sendButton.isDisplayed(), "Play card button should be visible");
        assertEquals("Play Card", sendButton.getText());
    }

    @Test
    @Order(3)
    void boardTilesAreVisible() {
        List<WebElement> tiles = driver.findElements(By.className("tile"));
        assertTrue(tiles.size() > 0, "Expected board tiles to be present on mobile");
    }

    @Test
    @Order(4)
    void afterSwitchingToNlPlayButtonAndTilesAreStillVisible() {
        WebElement langListbox = driver.findElement(By.className("lang-listbox"));
        new Select(langListbox).selectByValue("nl");

        // wait for the reload that changeLanguage() triggers, then for cards to appear
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> d.getCurrentUrl().contains("locale=nl"));
        waitUntilCardsAreVisible(driver);

        assertTrue(driver.getCurrentUrl().contains("mobile.html"),
                "Expected URL to still be on mobile.html after language switch");

        WebElement sendButton = driver.findElement(By.className("sendButton"));
        assertTrue(sendButton.isDisplayed(), "Play card button should still be visible after language switch");
        assertEquals("Kaart spelen", sendButton.getText());

        List<WebElement> tiles = driver.findElements(By.className("tile"));
        assertTrue(tiles.size() > 0, "Expected board tiles to still be present after language switch");
    }
}