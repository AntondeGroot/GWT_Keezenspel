package ADG.services;

import ADG.Games.Keezen.GameSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ADG.Games.Keezen.GameRegistry;

@RestController
@RequestMapping("/test")
public class TestResetController {

  @PostMapping("/reset")
  public void resetGameState() {
    GameSession gameSession = GameRegistry.getGame("123");
    gameSession.reset();
  }
}
