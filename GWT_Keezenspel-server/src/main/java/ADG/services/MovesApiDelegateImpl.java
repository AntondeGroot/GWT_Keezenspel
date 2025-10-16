package ADG.services;

import com.adg.openapi.api.MovesApiDelegate;
import com.adg.openapi.model.MoveRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MovesApiDelegateImpl implements MovesApiDelegate {

  @Override
  public ResponseEntity<Void> makeMove(String sessionId,
      String playerId,
      MoveRequest moveRequest) {

    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
