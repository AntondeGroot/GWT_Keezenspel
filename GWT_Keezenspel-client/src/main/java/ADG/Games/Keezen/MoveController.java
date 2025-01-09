package ADG.Games.Keezen;

import ADG.*;
import com.google.gwt.core.client.GWT;

import java.util.LinkedList;

public class MoveController {

    public static void movePawn(MoveResponse moveResponse) {
        GWT.log(moveResponse.toString());

        PawnId pawnId1 = moveResponse.getPawnId1();
        PawnId pawnId2 = moveResponse.getPawnId2();
        LinkedList<TileId> movePawn1 = moveResponse.getMovePawn1();
        LinkedList<TileId> movePawn2 = moveResponse.getMovePawn2();
        MoveType moveType = moveResponse.getMoveType();
        Pawn pawn1 = null;
        Pawn pawn2 = null;

        if(movePawn1 == null){return;}

        if(pawnId1 != null) {
            pawn1 = Board.getPawn(pawnId1);
            Board.movePawn(pawn1, movePawn1, false);
            GWT.log(Board.getPawns().toString());
        }

        if(pawnId2 != null){
            pawn2 = Board.getPawn(pawnId2);
            if(moveType == MoveType.MOVE || moveType == MoveType.ONBOARD){
                Board.movePawn(pawn2, movePawn2, true);
            }else{
                Board.movePawn(pawn2, movePawn2, false);
            }
            GWT.log(Board.getPawns().toString());
        }

    }
}
