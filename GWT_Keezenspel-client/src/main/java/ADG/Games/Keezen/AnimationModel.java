package ADG.Games.Keezen;

import com.google.gwt.core.client.GWT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class AnimationModel {
    public static ArrayList<PawnAnimationMapping> animationMappings = new ArrayList<>();//todo: change to private
    private static ArrayList<AnimatedPawn> animationSequence = new ArrayList<>();
    private static ArrayList<Pawn> staticPawns = new ArrayList<>();

    public static void reset(){
        animationMappings.clear();
        animationSequence.clear();
        staticPawns.clear();
    }

    public static ArrayList<Pawn> getStaticPawns() {
        return staticPawns;
    }

    public static boolean onlyPawnsToBeKilledAreLeft(){//todo: should be private
        return animationMappings.stream().allMatch(PawnAnimationMapping::isAnimateLast);
    }

    private static boolean shouldBeAnimated(Pawn pawn) {
        if(animationMappings.isEmpty()){
            return false;
        }

        for (PawnAnimationMapping animationMappings1 : animationMappings) {
            if (pawn.equals(animationMappings1.getPawn())) {
                return true;
            }
        }
        return false;
    }

    public static void movePawn(Pawn pawn, LinkedList<TileId> movePawn, boolean animateLast) {
        animationMappings.add(new PawnAnimationMapping(pawn, movePawn, animateLast));
        ArrayList<Pawn> pawns = Board.getPawns();
        for(Pawn pawn_i : pawns){
            if(pawn.equals(pawn_i)){
                pawn_i.setCurrentTileId(movePawn.getLast());
            }
        }
        Board.setPawns(pawns);
    }

    public static ArrayList<AnimatedPawn> getAnimationSequence(){
        return animationSequence;
    }

    public static void createAnimationSequence(){
        ArrayList<Pawn> pawns = (ArrayList<Pawn>) Board.getPawns();
        // sort the pawns vertically so that they don't overlap weirdly when drawn
        pawns.sort(new PawnComparator());
        for(Pawn pawn : pawns){
            if (shouldBeAnimated(pawn)) {
                Iterator<PawnAnimationMapping> iterator = animationMappings.iterator();
                while (iterator.hasNext()) {
                    PawnAnimationMapping animation_Pawn_i = iterator.next();
                    // only animate the killing of a pawn after all other moves of other pawns were animated
                    if(!animation_Pawn_i.isAnimateLast()) {
                        if (pawn.equals(animation_Pawn_i.getPawn())) {
                            if (animation_Pawn_i.getPoints().isEmpty()) {
                                iterator.remove(); // Remove the current element safely
                            } else {
                                LinkedList<Point> points = animation_Pawn_i.getPoints();
                                if (!points.isEmpty()) {
                                    Point p = points.getFirst();
                                    animationSequence.add(new AnimatedPawn(pawn, p));
                                    GWT.log("draw animated : "+ pawn);
                                    points.removeFirst(); // Remove the first element safely
                                }
                            }
                        }
                    }else{
                        GWT.log("draw statically : "+ animation_Pawn_i.getPawn());
                        // draw the pawn that is about to be killed statically
                        animationSequence.add(new AnimatedPawn(animation_Pawn_i.getPawn(), animation_Pawn_i.getPoints().getFirst()));
                        // if no other pawns to be drawn, start drawing this one.
                        if (onlyPawnsToBeKilledAreLeft() && animation_Pawn_i.isAnimateLast()){
                            animation_Pawn_i.setAnimateLast(false);
                        }
                    }
                }
            }else{
                //todo:
                staticPawns.add(pawn);
            }
        }
    }
}
