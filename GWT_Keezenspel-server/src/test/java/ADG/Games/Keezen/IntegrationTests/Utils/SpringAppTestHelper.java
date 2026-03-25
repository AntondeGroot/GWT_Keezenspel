package ADG.Games.Keezen.IntegrationTests.Utils;

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

  public static synchronized void startTestApp() {
    start(false);
  }

  public static synchronized void startRealApp() {
    start(true);
  }

  private static synchronized void start(Boolean cardDeckIsReal) {
    if (context != null) {
      if (!isReal.equals(cardDeckIsReal)) {
        throw new IllegalStateException(
            "Spring app already started with profile "
                + (isReal ? "realCardDeck" : "mockedCardDeck")
                + ", cannot restart with "
                + (cardDeckIsReal ? "realCardDeck" : "mockedCardDeck"));
      }
      return;
    }

    SpringApplication app = new SpringApplication(Application.class);
    app.setAdditionalProfiles(cardDeckIsReal ? "realCardDeck" : "mockedCardDeck");
    app.setDefaultProperties(Map.of("server.port", "4200"));
    context = app.run();
    isReal = cardDeckIsReal;
  }

  public static synchronized void stopApp() {
    if (context != null) {
      context.close();
      context = null;
      isReal = null;
    }
  }

  public static void resetGameState() {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:4200/test/reset"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

    try {
      client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (Exception ignored) {
    }
  }
}