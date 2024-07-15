package gwtks;

public class PawnAndCardSelection {
    private static int playerId;
    private static Pawn pawn1;
    private static Pawn pawn2;
    private static Card card;

    public static void setPlayerId(int p_playerId) {
        playerId = p_playerId;
    }

    public static void addPawn(Pawn pawn) {
        if(playerId == pawn.getPlayerId()){
            pawn1 = pawn;
        }else{
            pawn2 = pawn;
        }
    }

    public static void setCard(Card p_card) {
        card = p_card;
    }

    public static void reset(){
        pawn1 = null;
        pawn2 = null;
        card = null;
    }
}
