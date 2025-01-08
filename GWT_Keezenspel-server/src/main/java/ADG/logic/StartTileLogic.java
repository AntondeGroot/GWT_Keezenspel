package ADG.logic;

import ADG.Pawn;
import ADG.PawnId;
import ADG.TileId;

import static ADG.GameState.getPawn;

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
