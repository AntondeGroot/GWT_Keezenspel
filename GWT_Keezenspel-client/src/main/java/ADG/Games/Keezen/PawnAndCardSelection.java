package ADG.Games.Keezen;

import static ADG.Games.Keezen.Move.MessageType.MAKE_MOVE;
import static ADG.Games.Keezen.Move.MoveType.*;
import static ADG.Games.Keezen.util.BoardLogic.pawnIsOnNormalBoard;
import static ADG.Games.Keezen.util.CardDTOValueCheck.isJack;
import static ADG.Games.Keezen.util.CardDTOValueCheck.isSeven;

import ADG.Games.Keezen.Move.MessageType;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveType;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.dto.CardDTO;
import ADG.Games.Keezen.dto.PawnDTO;
import ADG.Games.Keezen.dto.PawnIdDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Visibility;
import java.util.ArrayList;
import java.util.Objects;

public class PawnAndCardSelection {
    private String playerId;
    private PawnDTO pawn1 = null;
    private PawnDTO pawn2 = null;
    private CardDTO card;
    private boolean drawCards = true;
    private MoveType moveType;
    private int nrStepsPawn1 = 0;
    private int nrStepsPawn2 = 0;
    private boolean uiEnabled = true;
    private ArrayList<PawnDTO> pawns = new ArrayList<>();

    public void disableUIForTests() {
        this.uiEnabled = false;
    }

    public void setPlayerId(String id) {
        // For testing purposes if you change a player ID then deselect everything
        // This makes it easier to say "player 2 picks card 2" instead of assuming PLayer 2 continues
        // playing with a card that Player 1 had selected.
        if(!Objects.equals(playerId,id)){
            reset();
        }
        playerId = id;
    }

    public void updatePawns(JsArray<PawnDTO> pawns){
      ArrayList<PawnDTO> tempPawns = new ArrayList<>();
      for (int i = 0; i < pawns.length(); i++) {
        tempPawns.add(pawns.get(i));
      }
        this.pawns = tempPawns;
        checkIfSelectedPawnsAreUpToDate();
    }

    public void checkIfSelectedPawnsAreUpToDate(){
        GWT.log("updatePawns trying to update");
        for (PawnDTO pawn : pawns) {
            // compares pawnId's then updates current position
            if(Objects.equals(pawn, pawn1)){
                pawn1 = pawn;
                GWT.log("PawnAndCardSelection.updatePawns(): " + pawn1);
            }
            if(Objects.equals(pawn, pawn2)){
                pawn2 = pawn;
                GWT.log("PawnAndCardSelection.updatePawns(): " + pawn1);
            }
        }
    }

    public String getPlayerId(){
        return playerId;
    }

    private PawnDTO getPawn(PawnIdDTO pawnId){
        for (PawnDTO pawn : pawns) {
            if(pawn.getPawnId().equals(pawnId)){
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
    public void addPawnId(PawnIdDTO pawnId) {
        PawnDTO pawn = getPawn(pawnId);
        GWT.log("test playerId = "+playerId);
        GWT.log("trying to add pawn = "+pawn);
        validateHowManyPawnsCanBeSelected(pawn); // not accounting for if they are on nest/board/finish
        validateSelectionBasedOnLocation();     // validate if they are on nest/board/finish
        validateMoveType();
    }

    /***
     * For testing purposes only, use addPawnId instead, otherwise you risk keeping track of outdated
     * current positions. In real life let server polls update PawnAncCardSelection to know what the pawns
     * locations are
     * @param pawn
     */
    public void addPawn(PawnDTO pawn) {
        GWT.log("test playerId = "+playerId);
        GWT.log("trying to add pawn = "+pawn);
        validateHowManyPawnsCanBeSelected(pawn); // not accounting for if they are on nest/board/finish
        validateSelectionBasedOnLocation();     // validate if they are on nest/board/finish
        validateMoveType();
    }

    private void validateSelectionBasedOnLocation() {
        if(card == null){
            return;
        }

        switch (card.getValue()) {
            case 1: break;//always valid: nest/board/finish
            case 11: secondPawnIsOnNormalBoardWhenYouPlayJack(); break;
            case 13: firstPawnIsOnNestWhenYouPlayKing(); break;
            default: validateAllPawnsAreOnBoardOrFinish(); break;
        }
    }

    private void secondPawnIsOnNormalBoardWhenYouPlayJack() {
        // you cannot switch,
        if(pawn2 != null){
            if(!pawnIsOnNormalBoard(pawn2)){
                pawn2 = null;
                System.out.println("Pawn 2 is not on normal board, you cannot switch with it, pawn is deselected.");
            }
        }
    }

    private void validateAllPawnsAreOnBoardOrFinish() {
// this is only for validating your own pawns, we do not care about this
    }

    private void firstPawnIsOnNestWhenYouPlayKing() {
//        if(pawn1 != null){
//            if(!isPawnOnNest(pawn1)){
//                pawn1 = null;
//            }
//        }
        if(pawn2 != null) {
            pawn2 = null;
        }
    }

    private boolean aPawnWasNotDeselected(PawnDTO pawn) {
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

    private void handlePlayerCanSelect2Pawns(PawnDTO pawn) {
        if(aPawnWasNotDeselected(pawn)){
            if (!playerId.equals(pawn.getPlayerId())) {
                return;
            }

            // select pawn
            if (pawn1 == null) {
                pawn1 = pawn;
            }else{
                pawn2 = pawn;
            }
        }
    }

    private void handlePlayerCanSelect1Pawn(PawnDTO pawn) {
        if(aPawnWasNotDeselected(pawn)){
            if (playerId.equals(pawn.getPlayerId())) {
                pawn1 = pawn;
            }
        }
    }

    private void handlePlayerCanSelectTheirOwnAndOpponentsPawn(PawnDTO pawn) {
       if(aPawnWasNotDeselected(pawn)){
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

    private void validateHowManyPawnsCanBeSelected(PawnDTO pawn) {
        if(card == null){
            handlePlayerCanSelect1Pawn(pawn);
            return;
        }

        switch (card.getValue()) {
            case 7: handlePlayerCanSelect2Pawns(pawn); break;
            case 11: handlePlayerCanSelectTheirOwnAndOpponentsPawn(pawn); break;
            default: handlePlayerCanSelect1Pawn(pawn); break;
        }
    }

    public void setCard(CardDTO p_card) {
        drawCards = true;

        // deselect when clicked twice
        if(card != null && card.equals(p_card)){
            System.out.println("PawnAndCardSelection the card was deselected, reset everything");
            card = null;
            nrStepsPawn1 = 0;
            nrStepsPawn2 = 0;
            moveType = null;
            return;
        }

        card = p_card;
        validateHowManyPawnsCanBeSelected(pawn2);
        validateSelectionBasedOnLocation();
        validateMoveType();
    }

    public PawnDTO getPawn1() {
        return pawn1;
    }

    public PawnIdDTO getPawnId1(){
        if(pawn1 == null){
            return null;
        }
        return pawn1.getPawnId();
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
        if(moveType == FORFEIT){
            pawn1 = null;
            pawn2 = null;
            nrStepsPawn1 = 0;
            nrStepsPawn2 = 0;
            card = null;
        }
    }

    public PawnIdDTO getPawnId2(){
        if(pawn2 == null){
            return null;
        }
        return pawn2.getPawnId();
    }

    public PawnDTO getPawn2() {
        return pawn2;
    }

    public CardDTO getCard() {
        return card;
    }

    public void setCardsAreDrawn(){
        drawCards = false;
    }

    public boolean getDrawCards(){
        return drawCards;
    }

    public void reset(){
        // do not reset playerId
        pawn1 = null;
        pawn2 = null;
        card = null;
        drawCards = true;
        moveType = null;
        nrStepsPawn1 = 0;
        nrStepsPawn2 = 0;
        setSplitBoxesVisibility(Visibility.HIDDEN);
    }

    private void validateMoveType(){
        if(card == null){
            return;
        }

        // hide boxes used to split a 7 over two pawns
        setSplitBoxesVisibility(Visibility.HIDDEN);
        switch (card.getValue()) {
            case 1: handleAce(); break;
            case 7: handleSeven(); break;
            case 11: handleJack(); break;
            case 13: handleKing(); break;
            default: handleDefaultCard(); break;
        }

        // selection of card deselects second pawn when card is not 7 or jack
        if(!isJack(card) && !isSeven(card)){
            pawn2 = null;
        }
    }

    private void handleAce() {
        if(pawn1 == null){
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
            // show boxes used to split a 7 over two pawns
            setSplitBoxesVisibility(Visibility.VISIBLE);
        } else {
            setMoveType(MOVE);
            nrStepsPawn1 = 7;
            nrStepsPawn2 = 0;
        }
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

    public void setNrStepsPawn1(int steps){
        nrStepsPawn1 = steps;
    }
    public void setNrStepsPawn1ForSplit(String stepsPawn1){
        // Check if the value is of length 1 and numerical
        if (!(stepsPawn1.length() == 1 && stepsPawn1.matches("\\d"))) {
            stepsPawn1 = "4";
        }
        // validate such that the total number of steps is always 7
        if (Integer.parseInt(stepsPawn1) <= 7 && Integer.parseInt(stepsPawn1) >= 0) {
            nrStepsPawn1 = Integer.parseInt(stepsPawn1);
            nrStepsPawn2 = 7 - nrStepsPawn1;
        } else {
            // the choice for 4 and 3 is arbitrary, but better than 7 and 0 so that
            // it is easy for the user to see that 7 is split over 2 pawns.
            nrStepsPawn1 = 4;
            nrStepsPawn2 = 3;
        }
    }
    public void setNrStepsPawn2(int steps){
        nrStepsPawn2 = steps;
    }

    public MoveMessage createTestMoveMessage() {
        MoveMessage moveMessage = createMessage();
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        return moveMessage;
    }

    public MoveMessage createMoveMessage() {
        if(pawn1 != null){
            GWT.log("PawnAncCardSelection creates move message and thinks pawn is on location "+pawn1.getCurrentTileId());
        }

        MoveMessage moveMessage = createMessage();
        moveMessage.setMessageType(MAKE_MOVE);
        moveType = null;
        return moveMessage;
    }

    private MoveMessage createMessage() {
        MoveMessage moveMessage = new MoveMessage();
//        moveMessage.setPlayerId(playerId);
//        moveMessage.setPawnId1(getPawnId1());
//        moveMessage.setPawnId2(getPawnId2());
//        moveMessage.setCard(card);
//        moveMessage.setMoveType(moveType);
//        moveMessage.setStepsPawn1(nrStepsPawn1);
//        if(moveType == SPLIT){ // comparing Enums with == is null safe
//            moveMessage.setStepsPawn2(nrStepsPawn2);
//        }

        return moveMessage;
    }

    private void setSplitBoxesVisibility(Visibility visibility){
        if (!uiEnabled) return;

        try {
            Document.get().getElementById("pawnIntegerBoxes").getStyle().setVisibility(visibility);
        } catch (Exception ignored) {}
    }

    @Override
    public String toString() {
        return "PawnAndCardSelection{" +
            "playerId='" + playerId + '\'' +
            ", pawn1=" + pawn1 +
            ", pawn2=" + pawn2 +
            ", card=" + card +
            ", drawCards=" + drawCards +
            ", moveType=" + moveType +
            ", nrStepsPawn1=" + nrStepsPawn1 +
            ", nrStepsPawn2=" + nrStepsPawn2 +
            '}';
    }
}
