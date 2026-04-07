package adg.keezen.utils;

import adg.keezen.IntegrationTests.Utils.SpringAppSuiteExtension;
import adg.keezen.IntegrationTests.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpringAppSuiteExtension.class)
public abstract class BaseUnitTest {
  @BeforeEach
  void resetState() {
    SpringAppTestHelper.resetGameState();
  }
}
