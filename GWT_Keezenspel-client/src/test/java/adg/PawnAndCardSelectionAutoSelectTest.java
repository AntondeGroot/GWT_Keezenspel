package adg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import adg.keezen.PawnAndCardSelection;
import adg.keezen.TileId;
import adg.keezen.dto.CardClient;
import adg.keezen.dto.PawnClient;
import adg.keezen.move.MoveType;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for auto-selecting the right card from the player's hand based on the pawn selection,
 * without the player having to click the card first.
 */
public class PawnAndCardSelectionAutoSelectTest {
  private PawnClient ownPawnOnBoard;
  private PawnClient ownPawnOnBoard2;
  private PawnClient ownPawnOnNest;
  private PawnClient otherPawnOnBoard;

  private final CardClient ace = new CardClient(0, 1);
  private final CardClient five = new CardClient(0, 5);
  private final CardClient seven = new CardClient(0, 7);
  private final CardClient jack = new CardClient(0, 11);
  private final CardClient king = new CardClient(0, 13);

  private PawnAndCardSelection pawnAndCardSelection;

  @BeforeEach
  void setup() {
    ownPawnOnBoard = new PawnClient("1", 1, new TileId("1", 0));
    ownPawnOnBoard2 = new PawnClient("1", 2, new TileId("1", 0));
    ownPawnOnNest = new PawnClient("1", 3, new TileId("1", -1));
    otherPawnOnBoard = new PawnClient("2", 1, new TileId("2", 0));

    pawnAndCardSelection = new PawnAndCardSelection();
    pawnAndCardSelection.disableUIForTests();
    pawnAndCardSelection.setPlayerId("1");
  }

  // ---------- JACK: own pawn + opponent pawn ----------

  @Test
  public void ownPawnThenEnemyPawn_WithJackInHand_AutoSelectsJackAndSwitch() {
    // GIVEN
    pawnAndCardSelection.setHand(Arrays.asList(jack, five));

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnBoard);
    pawnAndCardSelection.addPawn(otherPawnOnBoard);

    // THEN
    assertEquals(jack, pawnAndCardSelection.getCard());
    assertEquals(ownPawnOnBoard, pawnAndCardSelection.getPawn1());
    assertEquals(otherPawnOnBoard, pawnAndCardSelection.getPawn2());
    assertEquals(MoveType.SWITCH, pawnAndCardSelection.getMoveType());
  }

  @Test
  public void ownPawnThenEnemyPawn_WithoutJackInHand_CannotSelectEnemy() {
    // GIVEN
    pawnAndCardSelection.setHand(Arrays.asList(five, seven));

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnBoard);
    pawnAndCardSelection.addPawn(otherPawnOnBoard);

    // THEN
    assertNull(pawnAndCardSelection.getCard());
    assertNull(pawnAndCardSelection.getPawnId2());
    assertEquals(ownPawnOnBoard, pawnAndCardSelection.getPawn1());
  }

  @Test
  public void jackAutoSelected_ClickEnemyAgain_DeselectsAndClearsCard() {
    // GIVEN
    pawnAndCardSelection.setHand(Collections.singletonList(jack));
    pawnAndCardSelection.addPawn(ownPawnOnBoard);
    pawnAndCardSelection.addPawn(otherPawnOnBoard);
    assertEquals(jack, pawnAndCardSelection.getCard());

    // WHEN
    pawnAndCardSelection.addPawn(otherPawnOnBoard);

    // THEN
    assertNull(pawnAndCardSelection.getPawnId2());
    assertNull(pawnAndCardSelection.getCard());
  }

  // ---------- SEVEN: two of your own pawns ----------

  @Test
  public void twoOwnPawns_WithSevenInHand_AutoSelectsSevenAndSplit() {
    // GIVEN
    pawnAndCardSelection.setHand(Arrays.asList(five, seven));

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnBoard);
    pawnAndCardSelection.addPawn(ownPawnOnBoard2);

    // THEN
    assertEquals(seven, pawnAndCardSelection.getCard());
    assertEquals(ownPawnOnBoard, pawnAndCardSelection.getPawn1());
    assertEquals(ownPawnOnBoard2, pawnAndCardSelection.getPawn2());
    assertEquals(MoveType.SPLIT, pawnAndCardSelection.getMoveType());
  }

  @Test
  public void twoOwnPawns_WithoutSevenInHand_OnlyOnePawnSelected() {
    // GIVEN
    pawnAndCardSelection.setHand(Collections.singletonList(five));

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnBoard);
    pawnAndCardSelection.addPawn(ownPawnOnBoard2);

    // THEN
    assertNull(pawnAndCardSelection.getCard());
    assertNull(pawnAndCardSelection.getPawnId2());
    assertEquals(ownPawnOnBoard2, pawnAndCardSelection.getPawn1());
  }

  // ---------- KING / ACE: coming on board from the nest ----------

  @Test
  public void nestPawn_WithKingInHand_AutoSelectsKingAndOnBoard() {
    // GIVEN
    pawnAndCardSelection.setHand(Arrays.asList(king, ace, five));

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnNest);

    // THEN — King is preferred because it can only be used to come on board
    assertEquals(king, pawnAndCardSelection.getCard());
    assertEquals(ownPawnOnNest, pawnAndCardSelection.getPawn1());
    assertEquals(MoveType.ONBOARD, pawnAndCardSelection.getMoveType());
  }

  @Test
  public void nestPawn_WithoutKingButAceInHand_AutoSelectsAceAndOnBoard() {
    // GIVEN
    pawnAndCardSelection.setHand(Arrays.asList(ace, five));

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnNest);

    // THEN
    assertEquals(ace, pawnAndCardSelection.getCard());
    assertEquals(ownPawnOnNest, pawnAndCardSelection.getPawn1());
    assertEquals(MoveType.ONBOARD, pawnAndCardSelection.getMoveType());
  }

  @Test
  public void nestPawn_WithoutKingOrAce_NoCardSelected() {
    // GIVEN
    pawnAndCardSelection.setHand(Arrays.asList(five, seven));

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnNest);

    // THEN — pawn can still be selected, but no card is auto-picked
    assertNull(pawnAndCardSelection.getCard());
    assertEquals(ownPawnOnNest, pawnAndCardSelection.getPawn1());
  }

  @Test
  public void nestPawn_ClickAgain_DeselectsAndClearsAutoCard() {
    // GIVEN
    pawnAndCardSelection.setHand(Collections.singletonList(king));
    pawnAndCardSelection.addPawn(ownPawnOnNest);
    assertEquals(king, pawnAndCardSelection.getCard());

    // WHEN
    pawnAndCardSelection.addPawn(ownPawnOnNest);

    // THEN
    assertNull(pawnAndCardSelection.getPawnId1());
    assertNull(pawnAndCardSelection.getCard());
  }

  // ---------- manual card pick still wins ----------

  @Test
  public void manualCardPick_IsNotOverriddenByAutoSelect() {
    // GIVEN a five is manually selected
    pawnAndCardSelection.setHand(Arrays.asList(five, jack));
    pawnAndCardSelection.setCard(five);

    // WHEN selecting an own pawn and then an enemy pawn
    pawnAndCardSelection.addPawn(ownPawnOnBoard);
    pawnAndCardSelection.addPawn(otherPawnOnBoard);

    // THEN the manually chosen card stays, enemy pawn cannot be selected with a five
    assertEquals(five, pawnAndCardSelection.getCard());
    assertNull(pawnAndCardSelection.getPawnId2());
  }
}
