package ADG.testing;

import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.ImageProcessing;
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

    for (int i = 0; i < nrPlayers; i++) {
      ImageProcessing.create(i);
    }

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
