package ADG.Games.Keezen;

import static ADG.Games.Keezen.MoveType.*;

public class PawnAndCardSelection {
    // todo: turn this into a model, do not use statics
    private static String playerId;
    private static Pawn pawn1 = resetPawn();
    private static Pawn pawn2 = resetPawn();
    private static Card card;
    private static boolean drawCards = true;
    private static MoveType moveType;
    private static int nrSteps;
    private static int nrStepsPawn2;
    private static int cardNr; // for selecting which card in your hand you picked
    // if you have more than 4 players you will have 1 suit that is a double, if you have a hand with two of the same card
    // if you then pick one, both cards would otherwise be highlighted

    public static void setPlayerId(String id) {
        playerId = id;
    }

    public static String getPlayerId(){
        return playerId;
    }

    public static void addPawn(Pawn pawn) {
        validateSelectionBasedOnPlayerID(pawn); // not accounting for if they are on nest/board/finish
        validateSelectionBasedOnLocation();     // validate if they are on nest/board/finish
        validateMoveType();
    }

    private static void validateSelectionBasedOnLocation() {
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

    private static void validateAllPawnsAreOnBoard() {
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

    private static void validateAllPawnsAreOnBoardOrFinish() {
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

    private static void validateAllPawnsAreOnNest() {
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

    private static boolean aPawnWasNotDeselected(Pawn pawn) {
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

    private static void handlePlayerCanSelect2Pawns(Pawn pawn) {
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

    private static void handlePlayerCanSelect1Pawn(Pawn pawn) {
        if(aPawnWasNotDeselected(pawn)){
            if (playerId.equals(pawn.getPlayerId())) {
                pawn1 = pawn;
            }
        }
    }

    private static void handlePlayerCanSelectTheirOwnAndOpponentsPawn(Pawn pawn) {
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

    private static void validateSelectionBasedOnPlayerID(Pawn pawn) {
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

    public static void setCard(Card p_card) {
        card = p_card;
        validateMoveType();
        validateSelectionBasedOnLocation();
        drawCards = true;
    }

    public static Pawn getPawn1() {
        return pawn1;
    }

    public static PawnId getPawnId1(){
        if(pawn1.equals(resetPawn())){
            return null;
        }
        return pawn1.getPawnId();
    }

    public static MoveType getMoveType() {
        return moveType;
    }

    public static void setMoveType(MoveType moveType) {
        PawnAndCardSelection.moveType = moveType;
        if(moveType == FORFEIT){
            pawn1 = resetPawn();
            pawn2 = resetPawn();
            card = null;
        }
    }

    public static PawnId getPawnId2(){
        if(pawn2.equals(resetPawn())){
            return null;
        }
        return pawn2.getPawnId();
    }

    public static Pawn getPawn2() {
        return pawn2;
    }

    public static Card getCard() {
        return card;
    }

    public static void setCardsAreDrawn(){
        drawCards = false;
    }

    public static boolean getDrawCards(){
        return drawCards;
    }

    private static Pawn resetPawn(){
        return new Pawn(new PawnId("-1",-1),new TileId("-1",90));//todo: improve reset
    }

    public static void reset(){
        // do not reset playerId
        pawn1 = resetPawn();
        pawn2 = resetPawn();
        card = null;
        drawCards = true;
        moveType = null;
        nrSteps = 0;
        nrStepsPawn2 = 0;
    }

    private static void validateMoveType(){
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
        if(card.getCardValue() != 11 && card.getCardValue() != 7){
            pawn2 = resetPawn();
        }
    }

    private static void handleAce() {
        if (pawn1.getCurrentTileId().getTileNr() < 0) {
            setMoveType(ONBOARD);
        } else {
            setMoveType(MOVE);
            nrSteps = 1;
        }
    }

    private static void handleSeven() {
        if (!pawn1.equals(resetPawn()) && !pawn2.equals(resetPawn())) {
            setMoveType(SPLIT);
            //todo: set nr steps when 7 splits
        } else {
            setMoveType(MOVE);
            nrSteps = 7;
            nrStepsPawn2 = 0;
        }
    }

    private static void handleJack() {
        setMoveType(SWITCH);
    }

    private static void handleKing() {
        setMoveType(ONBOARD);
    }

    private static void handleDefaultCard() {
        setMoveType(MOVE);
        nrSteps = card.getCardValue();
        if (nrSteps == 4) {
            nrSteps = -4;
        }
    }

    public static int getCardNr() {
        return cardNr;
    }

    public static void setCardNr(int cardNr) {
        PawnAndCardSelection.cardNr = cardNr;
    }

    public static int getNrSteps() {
        return nrSteps;
    }
}
