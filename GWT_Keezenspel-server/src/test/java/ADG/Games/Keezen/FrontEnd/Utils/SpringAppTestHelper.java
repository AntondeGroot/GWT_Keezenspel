package ADG.Games.Keezen.FrontEnd.Utils;

import ADG.Application;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringAppTestHelper {

  private static ConfigurableApplicationContext context;

  private static Boolean isReal;

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

  private static void start(Boolean cardDeckIsReal) {
    resetGameState();

    if (context == null || cardDeckIsReal != isReal) {
      stopApp();
      SpringApplication app = new SpringApplication(Application.class);
      app.setAdditionalProfiles(cardDeckIsReal ? "realCardDeck" : "mockedCardDeck");
      app.setDefaultProperties(Map.of("server.port", "4200"));
      context = app.run();
      isReal = cardDeckIsReal;
    }
  }

  public static void stopApp() {
    if (context != null && context.isRunning()) {
      context.close();
      context = null;
    }
  }

  public static void resetGameState() {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4200/test/reset"))
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    try {
      client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (Exception ignored) {
    }
  }
}
