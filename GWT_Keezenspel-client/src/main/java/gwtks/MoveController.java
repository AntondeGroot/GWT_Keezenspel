package gwtks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.canvas.client.Canvas;

import java.util.List;

public class MoveController {

    public static void movePawn(Canvas canvas, MoveResponse moveResponse) {
        GWT.log(moveResponse.toString());

        PawnId pawnId1 = moveResponse.getPawnId1();
        PawnId pawnId2 = moveResponse.getPawnId2();
        List<TileId> movePawn1 = moveResponse.getMovePawn1();
        List<TileId> movePawn2 = moveResponse.getMovePawn2();
        Pawn pawn = null;

        for (TileId tileId : movePawn1) {
            GWT.log("made a step");
            pawn = Board.getPawn(pawnId1);
            GWT.log("selected pawn is "+ pawn + " pawnId"+pawnId1);
            if(pawn != null) {
                pawn.setCurrentTileId(tileId);
            }
            Board board = new Board();
            Board.movePawn(pawn, tileId);
            if(canvas!=null){
                board.drawPawns(canvas);
            }
            GWT.log(Board.getPawns().toString());
        }

    }
}
