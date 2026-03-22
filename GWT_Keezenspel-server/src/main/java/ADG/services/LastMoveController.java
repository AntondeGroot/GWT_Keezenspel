package ADG.services;

import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import com.adg.openapi.model.MoveResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LastMoveController {

  @GetMapping("/moves/{sessionId}/last")
  public ResponseEntity<MoveResponse> getLastMove(@PathVariable("sessionId") String sessionId) {
    GameSession session = GameRegistry.getGame(sessionId);
    if (session == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    MoveResponse last = session.getLastMoveResponse();
    if (last == null) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    return ResponseEntity.ok(last);
  }
}