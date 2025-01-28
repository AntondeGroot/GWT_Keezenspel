package ADG.Games.Keezen.handlers;

import ADG.Games.Keezen.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;

import java.util.List;

public class CanvasClickHandler {

    public static void handleCanvasClick(ClickEvent event, int x, int y, int stepsPawn1Split, int stepsPawn2Split) {
        if(y>600){//todo: this will not work if the canvas size were changed
            handleOnCardsDeckClick(x,y, stepsPawn1Split, stepsPawn2Split);
        }else{
            handleOnBoardClick(x,y, stepsPawn1Split, stepsPawn2Split);
        }
    }

    private static void handleOnCardsDeckClick(int x, int y, int stepsPawn1Split, int stepsPawn2Split){
        int padding = 10;
        int start;
        int end;
        int cardNr = -1;

        //TODO: make it independent on hard coded values
        for (int i = 0; i < 5; i++) {
            start = (100+padding)*i;
            end = start + 100;
            if(start < x && end > x){
                cardNr = i;
                break;
            }
        }
        if(y < 620 || y>620+158){
            cardNr = -1;
        }
        GWT.log("Clicked on CardsDeck : "+x+","+y+"\n"+"CardNr: "+cardNr);
        if(cardNr > -1){
            Card card = CardsDeck.pickCard(cardNr);
            PawnAndCardSelection.setCardNr(cardNr); // to distinguish when you have multiple identical cards
            if(card == null){return;}

            PawnAndCardSelection.setCard(card);
            PawnAndCardSelection.setCardNr(cardNr);

            if(PawnAndCardSelection.getPawn1() != null && PawnAndCardSelection.getCard() != null){
                if(PawnAndCardSelection.getMoveType()==MoveType.SPLIT){
                    PawnAndCardSelection.setNrStepsPawn1(stepsPawn1Split);
                    PawnAndCardSelection.setNrStepsPawn2(stepsPawn2Split);
                }
                TestMoveHandler testMoveHandler = new TestMoveHandler();
                testMoveHandler.sendMoveToServer();// todo: not an elegant way
            }
        }
        else{
            PawnAndCardSelection.reset();
        }
    }

    private static void handleOnBoardClick(int x, int y, int stepsPawn1Split, int stepsPawn2Split){
        StringBuilder logMsg = new StringBuilder();
        List<TileMapping> tiles = Board.getTiles();
        List<Pawn> pawns = Board.getPawns();
        logMsg.append("clicked on Board : (").append(x).append(",").append(y).append(")\n");
        for (Pawn pawn : pawns) {
            TileId tileOfPawn = pawn.getCurrentTileId();
            for (TileMapping tile : tiles) {
                if (tile.getTileId().equals(tileOfPawn)) {
                    if(isWithinDistance(tile.getPosition(), new Point(x,y))){
                        PawnAndCardSelection.addPawn(pawn);
                        // for debugging
                        logMsg.append("You clicked on pawn").append(pawn.getPawnId()).append(" position: ").append(tile.getPosition());
                        if(PawnAndCardSelection.getPawn1() != null && PawnAndCardSelection.getCard() != null){
                            if(PawnAndCardSelection.getMoveType()==MoveType.SPLIT){
                                PawnAndCardSelection.setNrStepsPawn1(stepsPawn1Split);
                                PawnAndCardSelection.setNrStepsPawn2(stepsPawn2Split);
                            }
                            TestMoveHandler testMoveHandler = new TestMoveHandler();
                            testMoveHandler.sendMoveToServer();
                        }
                        break;
                    }
                }
            }
        }
        GWT.log(logMsg.toString());
    }
    public static boolean isWithinDistance(Point p1, Point p2) {
        int distance = 15;
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        double dx = x2 - x1;
        double dy = y2 - y1;

        return (dx * dx + dy * dy) <= (distance * distance);
    }
}
