package ADG.Games.Keezen;

import ADG.Games.Keezen.Player.Pawn;

import java.util.ArrayList;
import java.util.LinkedList;

public class AnimationModel {
    public static ArrayList<PawnAnimationMapping> animationMappings = new ArrayList<>();//todo: change to private
    private static final ArrayList<Pawn> staticPawns = new ArrayList<>();

    public static void reset(){
        animationMappings.clear();
        staticPawns.clear();
    }

    public static boolean onlyPawnsToBeKilledAreLeft(){//todo: should be private
        return animationMappings.stream().allMatch(PawnAnimationMapping::isAnimateLast);
    }

    public static void movePawn(Pawn pawn, LinkedList<TileId> movePawn, boolean animateLast) {
        animationMappings.add(new PawnAnimationMapping(pawn, movePawn, animateLast));
        ArrayList<Pawn> pawns = Board.getPawns();
        for(Pawn pawnI : pawns){
            if(pawn.equals(pawnI)){
                pawnI.setCurrentTileId(movePawn.getLast());
            }
        }
        Board.setPawns(pawns);
    }
}
