package gwtks;

import com.google.gwt.core.client.GWT;

import static gwtks.MoveType.*;

public class PawnAndCardSelection {
    private static int playerId;
    private static Pawn pawn1 = new Pawn(new PawnId(-1,-1),new TileId(-1,90));
    private static Pawn pawn2 = new Pawn(new PawnId(-1,-1),new TileId(-1,90));
    private static Card card;
    private static boolean drawCards = true;
    private static MoveType moveType;
    private static int nrSteps;
    private static int cardNr; // for selecting which card in your hand you picked
    // if you have more than 4 players you will have 1 suit that is a double, if you have a hand with two of the same card
    // if you then pick one, both cards would otherwise be highlighted

    public static void setPlayerId(int p_playerId) {
        if(playerId != p_playerId){
            pawn1 = resetPawn();
            pawn2 = resetPawn();
            card = null;
            playerId = p_playerId;
        }
    }

    public static int getPlayerId(){
        return playerId;
    }

    public static void addPawn(Pawn pawn) {
        // deselect pawn1
        if(playerId == pawn.getPlayerId() && pawn1 !=null && pawn1.equals(pawn)){
            pawn1 = resetPawn();
            return;
        }

        // deselect pawn2
        if(playerId != pawn.getPlayerId() && pawn2 !=null && pawn2.equals(pawn)){
            pawn2 = resetPawn();
            return;
        }

        // select pawn1
        if(playerId == pawn.getPlayerId()){
            pawn1 = pawn;
        }

        // select pawn2
        if(playerId != pawn.getPlayerId()) {
            pawn2 = pawn;
        }

        cardValidation();
    }

    public static void setCard(Card p_card) {
        card = p_card;
        cardValidation();
        drawCards = true;
        // if you chose a card other than a Jack, you deselect the pawn belonging to another player
        if(card.getCardValue()!=10){
            pawn2 = resetPawn();
        }
    }

    public static Pawn getPawn1() {
        return pawn1;
    }

    public static PawnId getPawnId1(){
        if(pawn1 == null || pawn1.equals(resetPawn())){
            return null;
        }
        return pawn1.getPawnId();
    }

    public static MoveType getMoveType() {
        return moveType;
    }

    public static void setMoveType(MoveType moveType) {
        PawnAndCardSelection.moveType = moveType;
        if(card != null && card.getCardValue() == 0){
            if(pawn1.getCurrentTileId().getTileNr() < 0){
                PawnAndCardSelection.moveType = ONBOARD;
            }else{
                PawnAndCardSelection.moveType = MoveType.MOVE;
            }
        }
    }

    public static PawnId getPawnId2(){
        if(pawn2 == null || pawn2.equals(resetPawn())){
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
        return new Pawn(new PawnId(-1,-1),new TileId(-1,90));
    }

    public static void reset(){
        pawn1 = resetPawn();
        pawn2 = resetPawn();
        card = null;
        drawCards = true;
        moveType = null;
        nrSteps = 0;
    }

    private static void cardValidation(){
        if(card == null){
            pawn2 = resetPawn();
            return;
        }
        int cardFaceValue = card.getCardValue()+1;
        if(cardFaceValue == 1){
            // ace: onboard OR move
            GWT.log("ace selected move: "+PawnAndCardSelection.getPawn1());
            if(PawnAndCardSelection.getPawn1().getCurrentTileId().getTileNr() < 0){
                PawnAndCardSelection.setMoveType(ONBOARD);
            }else{
                PawnAndCardSelection.setMoveType(MOVE);
                nrSteps = cardFaceValue;
            }
        }else if(cardFaceValue == 11){
            // jack: switch pawns
            PawnAndCardSelection.setMoveType(SWITCH);
        }else if(cardFaceValue == 13){
            // king: onboard
            PawnAndCardSelection.setMoveType(ONBOARD);
        }else{
            // move
            if(cardFaceValue == 4){
                cardFaceValue = -4;
            }
            PawnAndCardSelection.setMoveType(MOVE);
            nrSteps = cardFaceValue;
        }
        if(card.getCardValue() != 10){
            pawn2 = resetPawn();
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
