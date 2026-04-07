package adg.keezen.utils;


import org.junit.jupiter.api.Tag;

@Tag("browser")
//@ExtendWith(ScreenshotOnFailure.class)//todo: screenshot on failure can clash with parallel execution of IT tests, that is, tests can say that they ran 0 tests and pass!!
public abstract class BaseIntegrationTest extends BaseUnitTest {
}
