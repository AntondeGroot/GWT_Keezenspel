package adg;

import static adg.CardEnum.ACE;
import static adg.keezen.move.MoveType.FORFEIT;
import static adg.keezen.move.MoveType.MOVE;
import static adg.keezen.move.MoveType.ONBOARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import adg.keezen.PawnAndCardSelection;
import adg.keezen.TileId;
import adg.keezen.dto.CardClient;
import adg.keezen.move.MoveType;
import adg.keezen.dto.PawnClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PawnAndCardSelectionAceTest {
  private final PawnClient ownPawnOnBoard = new PawnClient("1", 1, new TileId("1", 0));
  private final PawnClient ownPawnOnNest = new PawnClient("1", 2, new TileId("1", -1));
  private final PawnClient ownPawnOnFinish = new PawnClient("1", 3, new TileId("1", 16));
  private PawnAndCardSelection pawnAndCardSelection;

  @BeforeEach
  void setup() {
    pawnAndCardSelection = new PawnAndCardSelection();
    pawnAndCardSelection.disableUIForTests();
  }

  // TEST: NEST
  @Test
  public void setAce_SelectPawnOffBoard_MoveOnBoard() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.setCard(ACE.get());

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnNest);

    // THEN
    assertEquals(MoveType.ONBOARD, pawnAndCardSelection.getMoveType());
  }

  @Test
  public void selectPawnOffBoard_SetAce_MoveOnBoard() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.addPawn(ownPawnOnNest);

    // WHEN
    pawnAndCardSelection.setCard(ACE.get());

    // THEN
    assertEquals(MoveType.ONBOARD, pawnAndCardSelection.getMoveType());
  }

  // TEST: ON BOARD
  @Test
  public void setAce_SelectPawnOnBoard_MoveTypeMove() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.setCard(ACE.get());

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnBoard);

    // THEN
    assertEquals(MOVE, pawnAndCardSelection.getMoveType());
  }

  @Test
  public void selectPawnOnBoard_SelectAce_MoveTypeMove() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.addPawn(ownPawnOnBoard);

    // WHEN
    pawnAndCardSelection.setCard(ACE.get());

    // THEN
    assertEquals(MOVE, pawnAndCardSelection.getMoveType());
  }

  // TEST: FINISH
  @Test
  public void setAce_SelectPawnOnFinish_MoveTypeMove() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.setCard(ACE.get());

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnFinish);

    // THEN
    assertEquals(MOVE, pawnAndCardSelection.getMoveType());
  }

  @Test
  public void selectPawnOnFinish_SelectAce_MoveTypeMove() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.addPawn(ownPawnOnFinish);

    // WHEN
    pawnAndCardSelection.setCard(ACE.get());

    // THEN
    assertEquals(MOVE, pawnAndCardSelection.getMoveType());
  }

  // TEST: FORFEIT
  @Test
  public void selectAce_ThenForfeit_Resets() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.addPawn(ownPawnOnBoard);
    pawnAndCardSelection.setCard(ACE.get());

    // WHEN
    pawnAndCardSelection.setMoveType(FORFEIT);

    // THEN
    assertEquals(FORFEIT, pawnAndCardSelection.getMoveType());
    assertNull(pawnAndCardSelection.getCard());
    assertNull(pawnAndCardSelection.getPawnId1());
  }

  // TEST: DESELECT BY SELECTING TWICE
  @Test
  public void selectPawnTwice_Deselects() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.addPawn(ownPawnOnBoard);
    pawnAndCardSelection.setCard(ACE.get());

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnBoard);

    // THEN
    assertNull(pawnAndCardSelection.getPawnId1());
  }

  @Test
  public void ONBOARD_SetAce_NonAce_ThenAce_MOVEStillONBOARD() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.setCard(ACE.get());
    pawnAndCardSelection.addPawn(ownPawnOnNest);
    pawnAndCardSelection.setCard(new CardClient(0, 5));
    // giving a non ace card deselects the pawn, but when you select an Ace and the pawn
    // you should no longer see nrStepsPawn1 as 5.

    // WHEN
    pawnAndCardSelection.setCard(ACE.get());

    // THEN
    assertEquals(ONBOARD, pawnAndCardSelection.getMoveType());
    assertEquals(0, pawnAndCardSelection.getNrStepsPawn1());
  }

  @Test
  public void pawnOnBoard_SelectAce_Move() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.addPawn(ownPawnOnBoard);
    pawnAndCardSelection.setCard(new CardClient(0, 5));
    pawnAndCardSelection.setCard(ACE.get());

    // THEN
    assertEquals(MOVE, pawnAndCardSelection.getMoveType());
    assertEquals(1, pawnAndCardSelection.getNrStepsPawn1());
  }

  @Test
  public void pawnOnBoard_SelectAce_Mov() {
    // GIVEN
    pawnAndCardSelection.setPlayerId("1");
    pawnAndCardSelection.addPawn(ownPawnOnBoard);
    pawnAndCardSelection.setCard(new CardClient(0, 5));
    pawnAndCardSelection.setCard(ACE.get());

    // THEN
    assertEquals(MOVE, pawnAndCardSelection.getMoveType());
    assertEquals(1, pawnAndCardSelection.getNrStepsPawn1());
  }
}
