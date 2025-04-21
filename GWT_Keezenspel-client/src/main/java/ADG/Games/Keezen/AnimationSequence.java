package ADG.Games.Keezen;

import ADG.Games.Keezen.Player.Pawn;

import java.util.ArrayList;
import java.util.LinkedList;

public class AnimationSequence {
    private static final ArrayList<AnimatePawnPoints> sequenceFirst = new ArrayList<>();
    private static final ArrayList<AnimatePawnPoints> sequenceLast = new ArrayList<>();

    public static ArrayList<AnimatePawnPoints> getFirst(){return sequenceFirst;}

    public static ArrayList<AnimatePawnPoints> getLast(){return sequenceLast;}

    public static void reset(){
        sequenceFirst.clear();
        sequenceLast.clear();
    }

    public static void movePawn(Pawn pawn, LinkedList<TileId> movePawn, boolean animateFirst) {
        // move the pawn in the model
        ArrayList<Pawn> pawns = Board.getPawns();
        for(Pawn pawnI : pawns){
            if(pawn.equals(pawnI)){
                pawnI.setCurrentTileId(movePawn.getLast());
            }
        }
        Board.setPawns(pawns);

        // animate the movement of the pawn
        if(animateFirst){
            sequenceFirst.add(new AnimatePawnPoints(pawn, movePawn));
        }else{
            sequenceLast.add(new AnimatePawnPoints(pawn, movePawn));
        }

    }
}
