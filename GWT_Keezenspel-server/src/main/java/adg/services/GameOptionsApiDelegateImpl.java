package adg.services;

import adg.keezen.KeezenGameOptions;
import com.adg.openapi.api.GameOptionsApiDelegate;
import com.adg.openapi.model.GameOption;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GameOptionsApiDelegateImpl implements GameOptionsApiDelegate {

  @Override
  public ResponseEntity<List<GameOption>> getGameOptions() {
    return ResponseEntity.ok(KeezenGameOptions.all());
  }
}