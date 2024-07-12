package gwtks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.canvas.client.Canvas;

import java.util.LinkedList;
import java.util.List;

public class MoveController {

    public static void movePawn(MoveResponse moveResponse) {
        GWT.log(moveResponse.toString());

        PawnId pawnId1 = moveResponse.getPawnId1();
        PawnId pawnId2 = moveResponse.getPawnId2();
        LinkedList<TileId> movePawn1 = moveResponse.getMovePawn1();
        LinkedList<TileId> movePawn2 = moveResponse.getMovePawn2();
        Pawn pawn1 = null;

        if(movePawn1 == null){return;}

        for (TileId tileId : movePawn1) {
            GWT.log("made a step");
            pawn1 = Board.getPawn(pawnId1);
            GWT.log("selected pawn is "+ pawn1 + " pawnId"+pawnId1);
            if(pawn1 != null) {
                Board board = new Board();
                Board.movePawn(pawn1, movePawn1);
                pawn1.setCurrentTileId(tileId);
            }

            GWT.log(Board.getPawns().toString());
        }

    }
}
