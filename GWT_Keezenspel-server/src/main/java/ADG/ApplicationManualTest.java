package ADG;

import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.ImageProcessing;
import ADG.Games.Keezen.Player.Player;
import java.io.File;
import java.util.Objects;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ServletComponentScan
public class ApplicationManualTest
        extends SpringBootServletInitializer {

  public static void main(String[] args) {
//    int NrPlayers = 3;
//
//    SpringApplication.run(ApplicationManualTest.class,
//                          args);
//    for (int i = 0; i < NrPlayers; i++) {
//      ImageProcessing.create(i);
//    }
//    String sessionId = GameRegistry.createTestGame("123");
//    GameSession session = GameRegistry.getGame(sessionId);
//    GameState gameState = session.getGameState();
//    if(gameState.getPawns().isEmpty()){
//      for (int i = 0; i < NrPlayers; i++) {
//        Player player = new Player("player"+i,String.valueOf(i));
//        if(i==0){
//          player.setIsPlaying(true);
//        }
//        gameState.addPlayer(player);
//      }
//    }
//    //todo: remove testdata
//    //todo: replace with isRunning method
//    if(gameState.getPawns().isEmpty()){
//      gameState.start();
//    }
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(ApplicationManualTest.class);
  }

  @Component
  public static class EmbeddedServletContainerConfig
      implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
      File laucherDirDirectory = new File(Objects.requireNonNull(getClass().getResource("/"))
                                                 .getFile(),
                                          "launcherDir");
      if (laucherDirDirectory.exists()) {
        // You have to set a document root here, otherwise RemoteServiceServlet will failed to find the
        // corresponding serializationPolicyFilePath on a temporary web server started by spring boot application:
        // servlet.getServletContext().getResourceAsStream(serializationPolicyFilePath) returns null.
        // This has impact that java.io.Serializable can be no more used in RPC, only IsSerializable works.
        factory.setDocumentRoot(laucherDirDirectory);
      }
    }
  }
}
