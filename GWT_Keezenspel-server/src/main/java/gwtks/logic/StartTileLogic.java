package gwtks.logic;

import gwtks.Pawn;
import gwtks.PawnId;
import gwtks.TileId;

import static gwtks.GameState.getPawn;

public class StartTileLogic {

    public static boolean canPassStartTile(PawnId selectedPawnId, TileId tileId){
        Pawn pawnOnTile = getPawn(tileId);

        if(pawnOnTile == null){
            return true;
        }

        if(selectedPawnId.equals(pawnOnTile.getPawnId())){
            return true;
        }

        if(pawnOnTile.getPlayerId() == tileId.getPlayerId()) {
            return false;
        }

        return true;
    }
}
