package ADG.Games.Keezen.FrontEnd.Utils;

import ADG.Application;
import ADG.ApplicationAutomatedTest;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringAppTestHelper {

  private static ConfigurableApplicationContext context;

  /***
   * start the springboot server for each test
   * this way you will have a clean slate for your tests
   */
  public static void startTestApp() {
    start(false);
  }

  public static void startRealApp() {
    start(true);
  }

  private static void start(Boolean cardDeckIsReal){
    if (context == null || !context.isRunning()) {
      stopApp();
      SpringApplication app = new SpringApplication(Application.class);
      app.setAdditionalProfiles(cardDeckIsReal ? "realCardDeck" : "mockedCardDeck");
      app.setDefaultProperties(Map.of("server.port", "4200"));
      context = app.run();
    }
  }

  public static void stopApp() {
    if (context != null && context.isRunning()) {
      context.close();
      context = null;
    }
  }
}
