package gwtks;

import gwtks.handlers.TestMoveHandler;

public class PawnAndCardSelection {
    private static int playerId;
    private static Pawn pawn1 = new Pawn(new PawnId(-1,-1),new TileId(-1,90));
    private static Pawn pawn2 = new Pawn(new PawnId(-1,-1),new TileId(-1,90));
    private static Card card;
    private static boolean drawCards = true;

    public static void setPlayerId(int p_playerId) {
        playerId = p_playerId;
    }

    public static void addPawn(Pawn pawn) {
        if(playerId == pawn.getPlayerId()){
            pawn1 = pawn;
        }else{
            pawn2 = pawn;
        }
        drawCards = true;
    }

    public static void setCard(Card p_card) {
        card = p_card;
        drawCards = true;
    }

    public static Pawn getPawn1() {
        return pawn1;
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

    public static void reset(){
        pawn1 = new Pawn(new PawnId(-1,-1),new TileId(-1,90));
        pawn2 = new Pawn(new PawnId(-1,-1),new TileId(-1,90));
        card = null;
        drawCards = true;
    }
}
