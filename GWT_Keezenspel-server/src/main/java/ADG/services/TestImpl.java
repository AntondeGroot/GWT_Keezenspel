package ADG.services;

import com.adg.openapi.api.TestApiDelegate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TestImpl implements TestApiDelegate {

  @Override
  public ResponseEntity<Integer> testGet() {
    return new ResponseEntity<>(7, HttpStatus.OK);
  }
}
