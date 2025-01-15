package ADG.Games.Keezen.handlers;

import ADG.Games.Keezen.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import ADG.Games.Keezen.Card;

import java.util.List;

public class CanvasClickHandler {

    public static void handleCanvasClick(ClickEvent event, int x, int y) {
        if(y>600){
            handleOnCardsDeckClick(x,y);
        }else{
            handleOnBoardClick(x,y);
        }
    }

    private static void handleOnCardsDeckClick(int x, int y){
        GWT.log("Clicked on CardsDeck : "+x+","+y);
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

        GWT.log("cardNr = "+cardNr);
        if(cardNr > -1){
            Card card = CardsDeck.pickCard(cardNr);
            PawnAndCardSelection.setCardNr(cardNr); // to distinguish when you have multiple identical cards
            if(card == null){return;}

            PawnAndCardSelection.setCard(card);
            PawnAndCardSelection.setCardNr(cardNr);

            if(PawnAndCardSelection.getPawn1() != null && PawnAndCardSelection.getCard() != null){
                TestMoveHandler testMoveHandler = new TestMoveHandler();
                testMoveHandler.sendMoveToServer();
            }
        }
        else{
            PawnAndCardSelection.reset();
        }
    }

    private static void handleOnBoardClick(int x, int y){
        List<TileMapping> tiles = Board.getTiles();
        List<Pawn> pawns = Board.getPawns();
        GWT.log("Clicked on Board : "+x+","+y);
        for (Pawn pawn : pawns) {
            TileId tileOfPawn = pawn.getCurrentTileId();
            for (TileMapping tile : tiles) {
                if (tile.getTileId().equals(tileOfPawn)) {
                    if(isWithinDistance(tile.getPosition(), new Point(x,y))){
                        PawnAndCardSelection.addPawn(pawn);
                        // for debugging
                        GWT.log("You clicked on pawn"+pawn.getPawnId()+" position: "+tile.getPosition());
                        if(PawnAndCardSelection.getPawn1() != null && PawnAndCardSelection.getCard() != null){
                            TestMoveHandler testMoveHandler = new TestMoveHandler();
                            testMoveHandler.sendMoveToServer();
                        }
                        break;
                    }
                }
            }
        }


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
