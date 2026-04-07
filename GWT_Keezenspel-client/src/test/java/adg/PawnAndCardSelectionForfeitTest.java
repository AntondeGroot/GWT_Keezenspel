package adg;

import static adg.keezen.move.MoveType.FORFEIT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import adg.keezen.move.MoveMessage;
import adg.keezen.PawnAndCardSelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PawnAndCardSelectionForfeitTest {
  private PawnAndCardSelection pawnAndCardSelection;

  @BeforeEach
  void setup() {
    pawnAndCardSelection = new PawnAndCardSelection();
    pawnAndCardSelection.disableUIForTests();
  }

  @Test
  public void ClickForfeit_OtherPlayerClicksPlay_DoesNotForfeitAsWell() {
    /**
     * this is for testing purposes only, where we test using 1 browser. Where one player forfeits
     * and another player accidentally clicks on the play button, and then accidentally forfeits as
     * well.
     */

    // WHEN player one forfeits
    pawnAndCardSelection.setMoveType(FORFEIT);
    pawnAndCardSelection.createMoveMessage();

    // WHEN player two clicks on the send button without selecting anything else
    MoveMessage msg = pawnAndCardSelection.createMoveMessage();

    // THEN
    assertEquals(new MoveMessage().getMoveType(), msg.getMoveType());
  }
}
