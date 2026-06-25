package adg.keezen;

import static adg.keezen.move.MessageType.MAKE_MOVE;
import static adg.keezen.move.MoveType.*;
import static adg.keezen.util.BoardLogic.isPawnOnNest;
import static adg.keezen.util.BoardLogic.pawnIsOnNormalBoard;
import static adg.keezen.util.CardValueCheck.isJack;
import static adg.keezen.util.CardValueCheck.isSeven;

import adg.keezen.move.MoveMessage;
import adg.keezen.move.MoveType;
import adg.keezen.dto.CardClient;
import adg.keezen.dto.PawnClient;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Visibility;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PawnAndCardSelection {

  private String playerId;
  private PawnClient pawn1 = null;
  private PawnClient pawn2 = null;
  private CardClient card;
  private boolean drawCards = true;
  private MoveType moveType;
  private int nrStepsPawn1 = 0;
  private int nrStepsPawn2 = 0;
  private boolean uiEnabled = true;
  private List<PawnClient> pawns = new ArrayList<>();
  private List<CardClient> hand = new ArrayList<>();
  private boolean cardWasAutoSelected = false;
  private boolean splitDefaultPending = false;

  public void disableUIForTests() {
    this.uiEnabled = false;
  }

  public void setPlayerId(String id) {
    // For testing purposes if you change a player ID then deselect everything
    // This makes it easier to say "player 2 picks card 2" instead of assuming PLayer 2 continues
    // playing with a card that Player 1 had selected.
    if (!Objects.equals(playerId, id)) {
      reset();
    }
    playerId = id;
  }

  public void updatePawns(List<PawnClient> pawns) {
    this.pawns = new ArrayList<>(pawns);
    checkIfSelectedPawnsAreUpToDate();
  }

  /***
   * Keep track of the player's current hand so a card can be auto-selected when the
   * player's pawn selection makes the intended card unambiguous (see autoSelectCardFor).
   * @param hand the cards currently held by the player
   */
  public void setHand(List<CardClient> hand) {
    this.hand = (hand == null) ? new ArrayList<>() : new ArrayList<>(hand);
  }

  public void checkIfSelectedPawnsAreUpToDate() {
    GWT.log("updatePawns trying to update");
    for (PawnClient pawn : pawns) {
      // compares pawnId's then updates current position
      if (Objects.equals(pawn, pawn1)) {
        pawn1 = pawn;
        GWT.log("PawnAndCardSelection.updatePawns(): " + pawn1);
      }
      if (Objects.equals(pawn, pawn2)) {
        pawn2 = pawn;
        GWT.log("PawnAndCardSelection.updatePawns(): " + pawn1);
      }
    }
  }

  public String getPlayerId() {
    return playerId;
  }

  private PawnClient getPawn(String pawnId) {
    for (PawnClient pawn : pawns) {
      if (pawn.getPawnId().equals(pawnId)) {
        return pawn;
      }
    }
    return null;
  }

  /***
   * For selecting a pawn based on its pawnId in onClick eventHandlers
   * Otherwise you will add pawns with outdated current positions
   * Let PawnAndCardSelection decide based on server calls where the pawns are and then decide
   * what message to send.
   * @param pawnId
   */
  // for real life
  public void addPawnId(String pawnId) {
    PawnClient pawn = getPawn(pawnId);
    GWT.log("test playerId = " + playerId);
    GWT.log("trying to add pawn = " + pawn);
    validateHowManyPawnsCanBeSelected(pawn); // not accounting for if they are on nest/board/finish
    validateSelectionBasedOnLocation(); // validate if they are on nest/board/finish
    validateMoveType();
    clearAutoCardIfIncomplete();
  }

  /***
   * For testing purposes only, use addPawnId instead, otherwise you risk keeping track of outdated
   * current positions. In real life let server polls update PawnAncCardSelection to know what the pawns
   * locations are
   * @param pawn
   */
  public void addPawn(PawnClient pawn) {
    GWT.log("test playerId = " + playerId);
    GWT.log("trying to add pawn = " + pawn);
    validateHowManyPawnsCanBeSelected(pawn); // not accounting for if they are on nest/board/finish
    validateSelectionBasedOnLocation(); // validate if they are on nest/board/finish
    validateMoveType();
    clearAutoCardIfIncomplete();
  }

  private void validateSelectionBasedOnLocation() {
    if (card == null) {
      return;
    }

    switch (card.getValue()) {
      case 1:
        break; // always valid: nest/board/finish
      case 11:
        secondPawnIsOnNormalBoardWhenYouPlayJack();
        break;
      case 13:
        firstPawnIsOnNestWhenYouPlayKing();
        break;
      default:
        break;
    }
  }

  private void secondPawnIsOnNormalBoardWhenYouPlayJack() {
    // you cannot switch,
    if (pawn2 != null) {
      if (!pawnIsOnNormalBoard(pawn2)) {
        pawn2 = null;
        System.out.println(
            "Pawn 2 is not on normal board, you cannot switch with it, pawn is deselected.");
      }
    }
  }

  private void firstPawnIsOnNestWhenYouPlayKing() {
    //        if(pawn1 != null){
    //            if(!isPawnOnNest(pawn1)){
    //                pawn1 = null;
    //            }
    //        }
    if (pawn2 != null) {
      pawn2 = null;
    }
  }

  private boolean aPawnWasNotDeselected(PawnClient pawn) {
    if (Objects.equals(pawn1, pawn)) {
      // this moves pawn 2 to pawn1 and resets pawn2
      // if pawn 2 was already reset, this changes nothing but clears pawn1
      pawn1 = pawn2;
      pawn2 = null;
      return false;
    }

    // deselect pawn2
    if (Objects.equals(pawn2, pawn)) {
      pawn2 = null;
      return false;
    }

    return true;
  }

  private void handlePlayerCanSelect2Pawns(PawnClient pawn) {
    if (aPawnWasNotDeselected(pawn)) {
      if (!playerId.equals(pawn.getPlayerId())) {
        return;
      }

      // select pawn
      if (pawn1 == null) {
        pawn1 = pawn;
      } else {
        pawn2 = pawn;
      }
    }
  }

  private void handlePlayerCanSelect1Pawn(PawnClient pawn) {
    if (aPawnWasNotDeselected(pawn)) {
      if (playerId.equals(pawn.getPlayerId())) {
        pawn1 = pawn;
      }
    }
  }

  private void handlePlayerCanSelectTheirOwnAndOpponentsPawn(PawnClient pawn) {
    if (aPawnWasNotDeselected(pawn)) {
      // select pawn1
      if (playerId.equals(pawn.getPlayerId())) {
        pawn1 = pawn;
      }

      // select pawn2
      if (!playerId.equals(pawn.getPlayerId())) {
        pawn2 = pawn;
      }
    }
  }

  private void validateHowManyPawnsCanBeSelected(PawnClient pawn) {
    // An auto-selected card is tied to the pawns that justified it. Re-derive it from
    // scratch on every click so it never lingers after the selection that caused it changes.
    if (cardWasAutoSelected) {
      card = null;
      cardWasAutoSelected = false;
      // A brand-new pawn (not a re-click that deselects an already-selected pawn) starts a
      // fresh inference, so drop the extra pawn the previous auto-card had pulled in.
      if (!Objects.equals(pawn, pawn1) && !Objects.equals(pawn, pawn2)) {
        pawn2 = null;
      }
    }

    if (card == null) {
      autoSelectCardFor(pawn);
    }

    if (card == null) {
      handlePlayerCanSelect1Pawn(pawn);
      return;
    }

    switch (card.getValue()) {
      case 7:
        handlePlayerCanSelect2Pawns(pawn);
        break;
      case 11:
        handlePlayerCanSelectTheirOwnAndOpponentsPawn(pawn);
        break;
      default:
        handlePlayerCanSelect1Pawn(pawn);
        break;
    }
  }

  /***
   * When no card is selected yet, infer which card the player must intend from the pawn they
   * are clicking and auto-select it from their hand (when they actually hold it):
   *  - clicking an opponent's pawn while one of your own is selected -> Jack (switch)
   *  - clicking a second pawn of your own                            -> Seven (split)
   *  - clicking a pawn still in your nest                            -> King, or Ace if no King
   * If the required card is not in hand the card stays null, so the selection is rejected just
   * as before (e.g. you cannot select an opponent's pawn without a Jack).
   */
  private void autoSelectCardFor(PawnClient pawn) {
    if (playerId == null || pawn == null) {
      return;
    }
    boolean isOwnPawn = playerId.equals(pawn.getPlayerId());

    // Selecting a second pawn (re-clicking pawn1 is a deselect, not a second selection).
    if (pawn1 != null && !Objects.equals(pawn1, pawn)) {
      if (isOwnPawn) {
        autoSelectCardWithValue(7); // two of your own pawns -> Seven
      } else {
        autoSelectCardWithValue(11); // your pawn + opponent's pawn -> Jack
      }
      return;
    }

    // Selecting your first pawn straight out of the nest means coming on board. A King can only
    // be used to come on board, so prefer it over an Ace, which has other uses.
    if (pawn1 == null && isOwnPawn && isPawnOnNest(pawn)) {
      if (!autoSelectCardWithValue(13)) {
        autoSelectCardWithValue(1);
      }
    }
  }

  /** Selects the first card in hand with the given value; returns whether one was found. */
  private boolean autoSelectCardWithValue(int value) {
    for (CardClient handCard : hand) {
      if (handCard.getValue() == value) {
        card = handCard;
        cardWasAutoSelected = true;
        return true;
      }
    }
    return false;
  }

  /***
   * An auto-selected card only makes sense while the selection that triggered it is complete.
   * If a needed pawn ends up deselected (e.g. an opponent pawn that turned out not to be on the
   * normal board for a Jack), drop the auto-selected card again so nothing lingers.
   */
  private void clearAutoCardIfIncomplete() {
    if (!cardWasAutoSelected || card == null) {
      return;
    }
    boolean complete;
    switch (card.getValue()) {
      case 7:
      case 11:
        complete = pawn1 != null && pawn2 != null; // need two pawns
        break;
      default:
        complete = pawn1 != null; // King / Ace need the pawn coming on board
        break;
    }
    if (!complete) {
      card = null;
      cardWasAutoSelected = false;
      moveType = null;
      nrStepsPawn1 = 0;
      nrStepsPawn2 = 0;
    }
  }

  public void setCard(CardClient p_card) {
    drawCards = true;
    // a manual card pick is authoritative and is never treated as auto-selected
    cardWasAutoSelected = false;

    // deselect when clicked twice
    if (card != null && card.equals(p_card)) {
      System.out.println("PawnAndCardSelection the card was deselected, reset everything");
      card = null;
      nrStepsPawn1 = 0;
      nrStepsPawn2 = 0;
      moveType = null;
      return;
    }

    card = p_card;
    validateSelectionBasedOnLocation();
    validateMoveType();
  }

  public PawnClient getPawn1() {
    return pawn1;
  }

  public String getPawnId1() {
    if (pawn1 == null) {
      return null;
    }
    return pawn1.getPawnId();
  }

  public MoveType getMoveType() {
    return moveType;
  }

  public void setMoveType(MoveType moveType) {
    this.moveType = moveType;
  }

  public String getPawnId2() {
    if (pawn2 == null) {
      return null;
    }
    return pawn2.getPawnId();
  }

  public PawnClient getPawn2() {
    return pawn2;
  }

  public CardClient getCard() {
    return card;
  }

  public void setCardsAreDrawn() {
    drawCards = false;
  }

  public boolean getDrawCards() {
    return drawCards;
  }

  public void resetSuccesfulMove(){
    pawn2 = null;
    card = null;
    cardWasAutoSelected = false;
    splitDefaultPending = false;
    drawCards = true;
    moveType = null;
    nrStepsPawn1 = 0;
    nrStepsPawn2 = 0;
    setSplitBoxesVisibility(Visibility.HIDDEN);
  }

  public void reset() {
    // do not reset playerId
    pawn1 = null;
    pawn2 = null;
    card = null;
    cardWasAutoSelected = false;
    splitDefaultPending = false;
    drawCards = true;
    moveType = null;
    nrStepsPawn1 = 0;
    nrStepsPawn2 = 0;
    setSplitBoxesVisibility(Visibility.HIDDEN);
  }

  private void validateMoveType() {
    if (card == null) {
      return;
    }

    // hide boxes used to split a 7 over two pawns
    setSplitBoxesVisibility(Visibility.HIDDEN);
    switch (card.getValue()) {
      case 1:
        handleAce();
        break;
      case 7:
        handleSeven();
        break;
      case 11:
        handleJack();
        break;
      case 13:
        handleKing();
        break;
      default:
        handleDefaultCard();
        break;
    }

    // selection of card deselects second pawn when card is not 7 or jack
    if (!isJack(card) && !isSeven(card)) {
      pawn2 = null;
    }
  }

  private void handleAce() {
    if (pawn1 == null) {
      nrStepsPawn1 = 1;
      setMoveType(MOVE);
      return;
    }

    if (pawn1.getCurrentTileId().getTileNr() < 0) {
      nrStepsPawn1 = 0;
      setMoveType(ONBOARD);
    } else {
      nrStepsPawn1 = 1;
      setMoveType(MOVE);
    }
  }

  private void handleSeven() {
    if (pawn1 != null && pawn2 != null) {
      setMoveType(SPLIT);
      nrStepsPawn1 = 0;
      nrStepsPawn2 = 7;
      // The selection just became a 2-pawn split: ask the presenter to adopt the server's
      // recommended allocation (deepest into the finish) as the starting value.
      splitDefaultPending = true;
      // show boxes used to split a 7 over two pawns
      setSplitBoxesVisibility(Visibility.VISIBLE);
    } else {
      setMoveType(MOVE);
      nrStepsPawn1 = 7;
      nrStepsPawn2 = 0;
    }
  }

  /** True right after a 7-split selection is formed, until the recommended default is applied. */
  public boolean isSplitDefaultPending() {
    return splitDefaultPending;
  }

  public void clearSplitDefaultPending() {
    splitDefaultPending = false;
  }

  private void handleJack() {
    nrStepsPawn1 = 0;
    nrStepsPawn2 = 0;
    setMoveType(SWITCH);
  }

  private void handleKing() {
    nrStepsPawn1 = 0;
    nrStepsPawn2 = 0;
    setMoveType(ONBOARD);
  }

  private void handleDefaultCard() {
    setMoveType(MOVE);
    nrStepsPawn1 = card.getValue();
    if (nrStepsPawn1 == 4) {
      nrStepsPawn1 = -4;
    }
  }

  public int getNrStepsPawn1() {
    return nrStepsPawn1;
  }

  public int getNrStepsPawn2() {
    return nrStepsPawn2;
  }

  public void setNrStepsPawn1(int steps) {
    nrStepsPawn1 = steps;
  }

  public void setNrStepsPawn1ForSplit(String input) {
    int value;
    try {
      value = Integer.parseInt(input);
    } catch (NumberFormatException e) {
      value = nrStepsPawn1;
    }
    if (value > 7) value = 0;
    else if (value < 0) value = 7;
    nrStepsPawn1 = value;
    nrStepsPawn2 = 7 - nrStepsPawn1;
  }

  public void setNrStepsPawn2ForSplit(String input) {
    int value;
    try {
      value = Integer.parseInt(input);
    } catch (NumberFormatException e) {
      value = nrStepsPawn2;
    }
    if (value > 7) value = 0;
    else if (value < 0) value = 7;
    nrStepsPawn2 = value;
    nrStepsPawn1 = 7 - nrStepsPawn2;
  }

  public void setNrStepsPawn2(int steps) {
    nrStepsPawn2 = steps;
  }

  public MoveMessage createMoveMessage() {
    if (pawn1 != null) {
      GWT.log(
          "PawnAncCardSelection creates move message and thinks pawn is on location "
              + pawn1.getCurrentTileId());
    }

    MoveMessage moveMessage = createMessage();
    moveMessage.setMessageType(MAKE_MOVE);
    moveType = null;
    return moveMessage;
  }

  private MoveMessage createMessage() {
    return new MoveMessage();
  }

  private void setSplitBoxesVisibility(Visibility visibility) {
    if (!uiEnabled) {
      return;
    }

    try {
      Document.get().getElementById("pawnIntegerBoxes").getStyle().setVisibility(visibility);
    } catch (Exception ignored) {
    }
  }

  @Override
  public String toString() {
    return "PawnAndCardSelection{"
        + "playerId='"
        + playerId
        + '\''
        + ", pawn1="
        + pawn1
        + ", pawn2="
        + pawn2
        + ", card="
        + card
        + ", drawCards="
        + drawCards
        + ", moveType="
        + moveType
        + ", nrStepsPawn1="
        + nrStepsPawn1
        + ", nrStepsPawn2="
        + nrStepsPawn2
        + '}';
  }
}
