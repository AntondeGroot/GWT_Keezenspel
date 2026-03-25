package ADG.Games.Keezen.utils;

import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppSuiteExtension;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpringAppSuiteExtension.class)
public abstract class BaseUnitTest {
  @BeforeEach
  void resetState() {
    SpringAppTestHelper.resetGameState();
  }
}
