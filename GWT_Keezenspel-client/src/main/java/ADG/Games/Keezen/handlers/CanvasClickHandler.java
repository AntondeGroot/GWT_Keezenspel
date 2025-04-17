package ADG.Games.Keezen.handlers;

import ADG.Games.Keezen.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;

import java.util.List;

public class CanvasClickHandler {
    public static void handleOnBoardClick(Point point, PawnAndCardSelection pawnAndCardSelection){
        double x = point.getX();
        double y = point.getY();

        StringBuilder logMsg = new StringBuilder();
        List<TileMapping> tiles = Board.getTiles();
        List<Pawn> pawns = Board.getPawns();
        logMsg.append("clicked on Board : (").append(x).append(",").append(y).append(")\n");
        for (Pawn pawn : pawns) {
            TileId tileOfPawn = pawn.getCurrentTileId();
            for (TileMapping tile : tiles) {
                if (tile.getTileId().equals(tileOfPawn)) {
                    if(isWithinDistance(tile.getPosition(), new Point(x,y))){
                        pawnAndCardSelection.addPawn(pawn);
                        // for debugging
                        logMsg.append("You clicked on pawn").append(pawn.getPawnId()).append(" position: ").append(tile.getPosition());
                        if(pawnAndCardSelection.getPawn1() != null && pawnAndCardSelection.getCard() != null){
                            TestMoveHandler.sendMoveToServer(pawnAndCardSelection.createTestMoveMessage());
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
