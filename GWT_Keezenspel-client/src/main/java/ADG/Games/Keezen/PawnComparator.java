package ADG.Games.Keezen;

import ADG.Games.Keezen.Pawn;
import ADG.Games.Keezen.TileId;

import java.util.Comparator;

public class PawnComparator implements Comparator<Pawn> {
    // FOR SORTING THE PAWNS BASED ON THEIR Y POSITION SO THAT THEY ARE DRAWN CORRECTLY
    @Override
    public int compare(Pawn p1, Pawn p2) {
        TileId tile1 = p1.getCurrentTileId();
        TileId tile2 = p2.getCurrentTileId();
        if(tile1 != null && tile2 != null && Board.getPosition(tile1.getPlayerId(), tile1.getTileNr()) != null){
            double pawn1Y = Board.getPosition(tile1.getPlayerId(), tile1.getTileNr()).getY();
            double pawn2Y = Board.getPosition(tile2.getPlayerId(), tile2.getTileNr()).getY();
            return Double.compare(pawn1Y, pawn2Y);
        }
        return 0;
    }
}