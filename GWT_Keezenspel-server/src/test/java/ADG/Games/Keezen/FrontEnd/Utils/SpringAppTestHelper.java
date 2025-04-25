package ADG.Games.Keezen.FrontEnd.Utils;

import ADG.Application;
import ADG.ApplicationAutomatedTest;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringAppTestHelper {

  private static ConfigurableApplicationContext context;
  private static Class<?> currentAppClass = null;

  /***
   * start the springboot server for each test
   * this way you will have a clean slate for your tests
   */
  public static void startTestApp() {
    if (context == null || !context.isRunning()) {
      stopApp();
      SpringApplication app = new SpringApplication(ApplicationAutomatedTest.class);
      app.setAdditionalProfiles("mockedCardDeck");
      app.setDefaultProperties(Map.of("server.port", "4200"));
      context = app.run();
      currentAppClass = ApplicationAutomatedTest.class;
    }
  }

  public static void startRealApp() {
    if (context == null || !context.isRunning()) {
      stopApp();
      SpringApplication app = new SpringApplication(Application.class);
      app.setAdditionalProfiles("realCardDeck");
      app.setDefaultProperties(Map.of("server.port", "4200"));
      context = app.run();
      currentAppClass = Application.class;
    }
  }

  public static void stopApp() {
    if (context != null && context.isRunning()) {
      context.close();
      context = null;
      currentAppClass = null;
    }
  }
}
