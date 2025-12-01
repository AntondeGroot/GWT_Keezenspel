package ADG;

import static ADG.CardEnum.KING;
import static ADG.CardEnum.NORMALCARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.PawnAndCardSelection;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.TileId;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PawnAndCardSelectionValidationTest {
  private PawnAndCardSelection pawnAndCardSelection;
  private Pawn pawn = new Pawn(new PawnId("2", 0), new TileId("2", 5));
  private Card ace = new Card(0, 1);

  @BeforeEach
  public void setUp() {
    pawnAndCardSelection = new PawnAndCardSelection();
    pawnAndCardSelection.disableUIForTests();
  }

  @AfterEach
  public void tearDown() {}

  @Test
  public void addPawnAndCard5_DeselectPawn_AddCard9_NrStepsUpdated() {
    /***
     * This was a bug, where you can no longer add a pawn after deselecting it
     */
    // GIVEN card and pawn, pawn deselected
    pawnAndCardSelection.setPlayerId("2");

    pawnAndCardSelection.addPawn(pawn);
    pawnAndCardSelection.setCard(new Card(0, 5));
    pawnAndCardSelection.addPawn(pawn);
    assertNull(pawnAndCardSelection.getPawn1());
    assertEquals(new Card(0, 5), pawnAndCardSelection.getCard());

    // WHEN adding new card
    pawnAndCardSelection.setCard(new Card(0, 9));

    // THEN
    assertEquals(9, pawnAndCardSelection.getCard().getCardValue());
    assertEquals(9, pawnAndCardSelection.getNrStepsPawn1());
  }

  @Test
  public void addPawnAndCard_DeselectPawn_AddCard9_AddPawnAgain() {
    /***
     * This was a bug, where you can no longer add a pawn
     */

    // GIVEN
    pawnAndCardSelection.setPlayerId("2");

    //
    pawnAndCardSelection.addPawn(pawn);
    pawnAndCardSelection.setCard(ace);
    pawnAndCardSelection.createMoveMessage();

    pawnAndCardSelection.setCard(new Card(0, 9));
    pawnAndCardSelection.addPawn(pawn);
    pawnAndCardSelection.addPawn(pawn);

    assertEquals(pawn.getPawnId(), pawnAndCardSelection.getPawnId1());
    assertEquals(9, pawnAndCardSelection.getNrStepsPawn1());
  }

  @Test
  public void deselectingCard_ResetsNrSteps() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("2");
    pawnAndCardSelection.setCard(NORMALCARD.get());

    // WHEN
    pawnAndCardSelection.setCard(NORMALCARD.get());

    // THEN
    assertEquals(0, pawnAndCardSelection.getNrStepsPawn1());
  }

  @Test
  public void deselectingKing_ResetsMoveType() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("2");
    pawnAndCardSelection.setCard(KING.get());

    // WHEN
    pawnAndCardSelection.setCard(KING.get());

    // THEN
    assertNull(pawnAndCardSelection.getMoveType());
  }

  @Test
  public void updatePawnWithNewCurrentPosition() {
    // GIVEN
    Pawn pawnOld = new Pawn(new PawnId("2", 0), new TileId("2", -1));
    Pawn pawnNew = new Pawn(new PawnId("2", 0), new TileId("2", 5));

    pawnAndCardSelection.setPlayerId("2");
    pawnAndCardSelection.addPawn(pawnOld);

    // WHEN
    pawnAndCardSelection.updatePawns(new ArrayList<Pawn>(Arrays.asList(pawnNew)));

    // THEN
    assertEquals(5, pawnAndCardSelection.getPawn1().getCurrentTileId().getTileNr());
  }
}
