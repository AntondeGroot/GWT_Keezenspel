package adg.services;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a request names a session that has no game; Spring maps it to an HTTP 404. */
@ResponseStatus(HttpStatus.NOT_FOUND)
class GameNotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  GameNotFoundException(String sessionId) {
    super("No game with session: " + sessionId);
  }
}
