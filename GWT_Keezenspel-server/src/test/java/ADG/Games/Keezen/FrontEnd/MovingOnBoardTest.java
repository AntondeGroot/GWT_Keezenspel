package ADG.Games.Keezen.FrontEnd;

import static ADG.Games.Keezen.FrontEnd.TestUtils.clickCardByValue;
import static ADG.Games.Keezen.FrontEnd.TestUtils.clickPawn;
import static ADG.Games.Keezen.FrontEnd.TestUtils.getDriver;
import static ADG.Games.Keezen.FrontEnd.TestUtils.makeMove;
import static ADG.Games.Keezen.FrontEnd.TestUtils.setPlayerIdPlaying;
import static org.junit.Assert.assertNotEquals;

import ADG.Games.Keezen.FrontEnd.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

public class MovingOnBoardTest {
  WebDriver driver;

  @BeforeEach
  public void setUp() {
//    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");

    SpringAppTestHelper.startTestApp();
    driver = getDriver();
    setPlayerIdPlaying(driver,"0");
  }

  @AfterEach
  public void tearDown() {
    // needed for skipping the selenium tests in CI
    if(driver != null){
      driver.quit();
    }
    SpringAppTestHelper.stopApp();
  }

  @Test
  public void pawnCanMoveOnBoardWithAce(){
    // GIVEN
    Point start = clickPawn(driver, new PawnId("0",0));

    // WHEN
    clickCardByValue(driver, 1);
    makeMove(driver);

    // THEN
    Point end = clickPawn(driver, new PawnId("0",0));
    assertNotEquals(start, end);
  }

  @Test
  public void pawnCanMoveOnBoardWithKing() throws InterruptedException {
    // GIVEN
    Point start = clickPawn(driver, new PawnId("0",0));

    // WHEN
    clickCardByValue(driver, 13);
    makeMove(driver);

    // THEN
    Point end = clickPawn(driver, new PawnId("0",0));
    assertNotEquals(start, end);
  }
}
