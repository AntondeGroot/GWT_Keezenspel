package gwtks;

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
            // deselect or select
            if(pawn1.equals(pawn)){
                pawn1 = resetPawn();
            }else{
                pawn1 = pawn;
            }
        }else{
            // only select another pawn if you have a Jack
            if(card!= null && card.getCard()==10){
                // deselect or select
                if(pawn2.equals(pawn)){
                    pawn2 = resetPawn();
                }else{
                    pawn2 = pawn;
                }
            }
            if(card==null || card.getCard()!=10){
                pawn2 = resetPawn();
            }
        }
        drawCards = true;
    }

    public static void setCard(Card p_card) {
        card = p_card;
        drawCards = true;
        // if you chose a card other than a Jack, you deselect the pawn belonging to another player
        if(card.getCard()!=10){
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
    }
}
