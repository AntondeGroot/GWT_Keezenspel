package ADG.Games.Keezen.animations;

import ADG.Games.Keezen.TileId;

import java.util.List;

public class StepsAnimation {
    public static List<TileId> tileIdsToBeHighlighted;

    public static void updateStepsAnimation(List<TileId> tileIds){
        StepsAnimation.tileIdsToBeHighlighted = tileIds;
    }

    public static void resetStepsAnimation(){
        tileIdsToBeHighlighted = null;
    }
}
