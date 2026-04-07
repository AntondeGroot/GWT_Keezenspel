package adg.testing;

import adg.keezen.GameRegistry;
import adg.keezen.GameSession;
import adg.keezen.GameState;
import com.adg.openapi.model.Player;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("realCardDeck")
public class TestGameInitializer_RealCards {

  @PostConstruct
  public void setupTestGame() {
    int nrPlayers = 3;

    String sessionId = GameRegistry.createNewGame("123");
    GameSession session = GameRegistry.getGame(sessionId);
    GameState gameState = session.getGameState();

    if (gameState.getPawns().isEmpty()) {
      for (int i = 0; i < nrPlayers; i++) {
        Player player = new Player().id("player" + i).name("player " + i);
        if (i == 0) {
          player.setIsPlaying(true);
        }
        gameState.addPlayer(player);
      }
    }

    if (gameState.getPawns().isEmpty()) {
      gameState.start();
    }
  }
}
