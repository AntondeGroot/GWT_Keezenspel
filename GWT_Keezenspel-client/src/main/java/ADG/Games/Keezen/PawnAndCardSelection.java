package ADG.Games.Keezen;

import static ADG.Games.Keezen.Move.MessageType.MAKE_MOVE;
import static ADG.Games.Keezen.Move.MoveType.*;
import static ADG.Games.Keezen.Cards.CardValueCheck.isJack;
import static ADG.Games.Keezen.Cards.CardValueCheck.isSeven;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Move.MessageType;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveType;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;

public class PawnAndCardSelection {
    private String playerId;
    private Pawn pawn1 = resetPawn();
    private Pawn pawn2 = resetPawn();
    private Card card;
    private boolean drawCards = true;
    private MoveType moveType;
    private int nrStepsPawn1;
    private int nrStepsPawn2;

    public void setPlayerId(String id) {
        playerId = id;
    }

    public String getPlayerId(){
        return playerId;
    }

    public void addPawn(Pawn pawn) {
        validateSelectionBasedOnPlayerID(pawn); // not accounting for if they are on nest/board/finish
        validateSelectionBasedOnLocation();     // validate if they are on nest/board/finish
        validateMoveType();
    }

    private void validateSelectionBasedOnLocation() {
        if(card == null){
            return;
        }

        switch (card.getCardValue()) {
            case 1: break;//always valid: nest/board/finish
            case 11: validateAllPawnsAreOnBoard(); break;
            case 13: validateAllPawnsAreOnNest(); break;
            default: validateAllPawnsAreOnBoardOrFinish(); break;
        }
    }

    private void validateAllPawnsAreOnBoard() {
        if(!pawn1.equals(resetPawn())){
            if(!(pawn1.getCurrentTileId().getTileNr() >= 0 && pawn1.getCurrentTileId().getTileNr() < 16)){// reset when not on board
                pawn1 = resetPawn();
            }
        }
        if(!pawn2.equals(resetPawn())){
            if(!(pawn2.getCurrentTileId().getTileNr() >= 0 && pawn2.getCurrentTileId().getTileNr() < 16)){// todo: logic like this should be in some helper.util
                pawn2 = resetPawn();
            }
        }
    }

    private void validateAllPawnsAreOnBoardOrFinish() {
        if(!pawn1.equals(resetPawn())){
            if(pawn1.getCurrentTileId().getTileNr() < 0){// reset when on nest
                pawn1 = resetPawn();
            }
        }
        if(!pawn2.equals(resetPawn())){
            if(pawn2.getCurrentTileId().getTileNr() < 0){// reset when on nest
                pawn2 = resetPawn();
            }
        }
    }

    private void validateAllPawnsAreOnNest() {
        if(!pawn1.equals(resetPawn())){
            if(pawn1.getCurrentTileId().getTileNr() > 0){
                pawn1 = resetPawn();
            }
        }
        if(!pawn2.equals(resetPawn())){
            if(pawn2.getCurrentTileId().getTileNr() > 0){
                pawn2 = resetPawn();
            }
        }
    }

    private boolean aPawnWasNotDeselected(Pawn pawn) {
        if (pawn1.equals(pawn)) {
            // this moves pawn 2 to pawn1 and resets pawn2
            // if pawn 2 was already reset, this changes nothing but clears pawn1
            pawn1 = pawn2;
            pawn2 = resetPawn();
            return false;
        }

        // deselect pawn2
        if (pawn2.equals(pawn)) {
            pawn2 = resetPawn();
            return false;
        }

        return true;
    }

    private void handlePlayerCanSelect2Pawns(Pawn pawn) {
        if(aPawnWasNotDeselected(pawn)){
            if (!playerId.equals(pawn.getPlayerId())) {
                return;
            }

            // select pawn
            if (pawn1.equals(resetPawn())) {
                pawn1 = pawn;
            }else{
                pawn2 = pawn;
            }
        }
    }

    private void handlePlayerCanSelect1Pawn(Pawn pawn) {
        if(aPawnWasNotDeselected(pawn)){
            if (playerId.equals(pawn.getPlayerId())) {
                pawn1 = pawn;
            }
        }
    }

    private void handlePlayerCanSelectTheirOwnAndOpponentsPawn(Pawn pawn) {
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

    private void validateSelectionBasedOnPlayerID(Pawn pawn) {
        if(card == null){
            handlePlayerCanSelect1Pawn(pawn);
            return;
        }

        switch (card.getCardValue()) {
            case 7: handlePlayerCanSelect2Pawns(pawn); break;
            case 11: handlePlayerCanSelectTheirOwnAndOpponentsPawn(pawn); break;
            default: handlePlayerCanSelect1Pawn(pawn); break;
        }
    }

    public void setCard(Card p_card) {
        card = p_card;
        validateMoveType();
        validateSelectionBasedOnPlayerID(pawn2);
        validateSelectionBasedOnLocation();
        drawCards = true;
    }

    public Pawn getPawn1() {
        return pawn1;
    }

    public PawnId getPawnId1(){
        if(pawn1.equals(resetPawn())){
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
            pawn1 = resetPawn();
            pawn2 = resetPawn();
            card = null;
        }
    }

    public PawnId getPawnId2(){
        if(pawn2.equals(resetPawn())){
            return null;
        }
        return pawn2.getPawnId();
    }

    public Pawn getPawn2() {
        return pawn2;
    }

    public Card getCard() {
        return card;
    }

    public void setCardsAreDrawn(){
        drawCards = false;
    }

    public boolean getDrawCards(){
        return drawCards;
    }

    private Pawn resetPawn(){
        return new Pawn(new PawnId("-1",-1),new TileId("-1",90));//todo: improve reset
    }

    public void reset(){
        // do not reset playerId
        pawn1 = resetPawn();
        pawn2 = resetPawn();
        card = null;
        drawCards = true;
        moveType = null;
        nrStepsPawn1 = 0;
        nrStepsPawn2 = 0;
    }

    private void validateMoveType(){
        if(card == null){
            return;
        }

        switch (card.getCardValue()) {
            case 1: handleAce(); break;
            case 7: handleSeven(); break;
            case 11: handleJack(); break;
            case 13: handleKing(); break;
            default: handleDefaultCard(); break;
        }

        // selection of card deselects second pawn when card is not 7 or jack
        if(!isJack(card) && !isSeven(card)){
            pawn2 = resetPawn();
        }
    }

    private void handleAce() {
        if (pawn1.getCurrentTileId().getTileNr() < 0) {
            setMoveType(ONBOARD);
        } else {
            setMoveType(MOVE);
            nrStepsPawn1 = 1;
        }
    }

    private void handleSeven() {
        if (!pawn1.equals(resetPawn()) && !pawn2.equals(resetPawn())) {
            setMoveType(SPLIT);
        } else {
            setMoveType(MOVE);
            nrStepsPawn1 = 7;
            nrStepsPawn2 = 0;
        }
    }

    private void handleJack() {
        setMoveType(SWITCH);
    }

    private void handleKing() {
        setMoveType(ONBOARD);
    }

    private void handleDefaultCard() {
        setMoveType(MOVE);
        nrStepsPawn1 = card.getCardValue();
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
        MoveMessage moveMessage = createMessage();
        moveMessage.setMessageType(MAKE_MOVE);
        return moveMessage;
    }

    private MoveMessage createMessage() {
        MoveMessage moveMessage = new MoveMessage();
        moveMessage.setPlayerId(playerId);
        moveMessage.setPawnId1(getPawnId1());
        moveMessage.setPawnId2(getPawnId2());
        moveMessage.setCard(card);
        moveMessage.setMoveType(moveType);
        moveMessage.setStepsPawn1(nrStepsPawn1);
        if(moveType.equals(SPLIT)){
            moveMessage.setStepsPawn2(nrStepsPawn2);
        }

        return moveMessage;
    }
}
