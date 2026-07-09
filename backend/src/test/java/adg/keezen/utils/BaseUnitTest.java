package adg.keezen.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpringAppSuiteExtension.class)
public abstract class BaseUnitTest {
  @BeforeEach
  void resetState() {
    SpringAppTestHelper.resetGameState();
  }
}
