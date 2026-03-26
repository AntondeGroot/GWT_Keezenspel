package ADG.Games.Keezen.utils;


import ADG.Games.Keezen.IntegrationTests.Utils.ScreenshotOnFailure;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppSuiteExtension;
import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("browser")
//@ExtendWith(ScreenshotOnFailure.class)//todo: screenshot on failure can clash with parallel execution of IT tests, that is, tests can say that they ran 0 tests and pass!!
public abstract class BaseIntegrationTest extends BaseUnitTest {
}
