package ADG.Games.Keezen;

import ADG.*;
import com.google.gwt.core.client.GWT;

import java.util.LinkedList;

public class MoveController {

    public static void movePawn(MoveResponse moveResponse) {
        GWT.log(moveResponse.toString());

        PawnId pawnId1 = moveResponse.getPawnId1();
        PawnId pawnId2 = moveResponse.getPawnId2();
        PawnId pawnIdKilled1 = moveResponse.getPawnIdKilled1();
        PawnId pawnIdKilled2 = moveResponse.getPawnIdKilled2();

        LinkedList<TileId> movePawn1 = moveResponse.getMovePawn1();
        LinkedList<TileId> movePawn2 = moveResponse.getMovePawn2();
        LinkedList<TileId> moveKilledPawn1 = moveResponse.getMoveKilledPawn1();
        LinkedList<TileId> moveKilledPawn2 = moveResponse.getMoveKilledPawn2();

        if(movePawn1 == null){return;}

        if(pawnId1 != null) {
            Pawn pawn1 = Board.getPawn(pawnId1);
            AnimationModel.movePawn(pawn1, movePawn1, false);
        }

        if(pawnId2 != null) {
            Pawn pawn2 = Board.getPawn(pawnId2);
            AnimationModel.movePawn(pawn2, movePawn2, false);
        }

        if(pawnIdKilled1 != null){
            Pawn pawnKilled1 = Board.getPawn(pawnIdKilled1);
            AnimationModel.movePawn(pawnKilled1, moveKilledPawn1, true);
        }

        if(pawnIdKilled2 != null){
            Pawn pawnKilled2 = Board.getPawn(pawnIdKilled2);
            AnimationModel.movePawn(pawnKilled2, moveKilledPawn2, true);
        }

    }
}
