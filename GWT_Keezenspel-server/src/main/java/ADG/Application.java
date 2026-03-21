package ADG;

import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Player;
import java.io.File;
import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;

@ServletComponentScan
@SpringBootApplication(
    scanBasePackages = {
      "ADG",
      "com.adg.openapi.api"
    }) // this is necessary to use generated REST classes
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) {
    int NrPlayers = 3;

    SpringApplication.run(Application.class, args);

    String sessionId = GameRegistry.createNewGame("123");
    GameSession session = GameRegistry.getGame(sessionId);
    GameState gameState = session.getGameState();
    if (gameState.getPawns().isEmpty()) {
      for (int i = 0; i < NrPlayers; i++) {
        Player player = new Player().id("player" + i).name("player " + i);
        if (i == 0) {
          player.setIsPlaying(true);
        }
        System.out.println("application player:");
        System.out.println(player);
        gameState.addPlayer(player);
      }
    }
    System.out.println(gameState.getPlayers().size());
    // todo: remove testdata
    // todo: replace with isRunning method
    if (gameState.getPawns().isEmpty()) {
      gameState.start();
    }
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(Application.class);
  }

  @Component
  public static class EmbeddedServletContainerConfig
      implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
      // ClassPathResource handles both exploded (dev) and JAR (Pi) deployments.
      // getResource("/").getFile() breaks inside a fat JAR because the URL uses
      // the jar: protocol. Without a document root GWT-RPC still works because
      // all shared classes implement IsSerializable.
      try {
        File launcherDir = new ClassPathResource("launcherDir").getFile();
        if (launcherDir.exists()) {
          factory.setDocumentRoot(launcherDir);
        }
      } catch (IOException e) {
        // Running from a JAR — launcherDir is not extractable as a File; safe to skip.
      }
    }
  }
}
