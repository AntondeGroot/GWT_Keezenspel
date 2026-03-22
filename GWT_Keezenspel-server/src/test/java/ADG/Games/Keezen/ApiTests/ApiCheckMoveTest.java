package ADG.Games.Keezen.ApiTests;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApiCheckMoveTest {

  @BeforeEach
  public void setUp() {
    SpringAppTestHelper.startRealApp();
  }

  @Test
  public void checkMove() {
    ApiUtil.createStandardGame();
  }
}
