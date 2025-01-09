package ADG.Games.Keezen.logic;

import ADG.Games.Keezen.Pawn;
import ADG.Games.Keezen.PawnId;
import ADG.Games.Keezen.TileId;

import static ADG.Games.Keezen.GameState.getPawn;

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
