package ADG.Games.Keezen.utils;


import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppSuiteExtension;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("browser")
@ExtendWith(ScreenshotOnFailure.class)
public abstract class BaseIntegrationTest extends BaseUnitTest {
}
