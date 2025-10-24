package ADG.Games.Keezen.animations;


import ADG.Games.Keezen.TileId;
import ADG.Games.Keezen.dto.PawnDTO;
import java.util.ArrayList;
import java.util.LinkedList;

public class AnimationSequence {

  private static final ArrayList<AnimatePawnPoints> sequenceFirst = new ArrayList<>();
  private static final ArrayList<AnimatePawnPoints> sequenceLast = new ArrayList<>();

  public static ArrayList<AnimatePawnPoints> getFirst() {
    return sequenceFirst;
  }

  public static ArrayList<AnimatePawnPoints> getLast() {
    return sequenceLast;
  }

  public static void reset() {
    sequenceFirst.clear();
    sequenceLast.clear();
  }

  public static void movePawn(PawnDTO pawn, LinkedList<TileId> movePawn, boolean animateFirst) {
    // animate the movement of the pawn
    if (animateFirst) {
      sequenceFirst.add(new AnimatePawnPoints(pawn, movePawn));
    } else {
      sequenceLast.add(new AnimatePawnPoints(pawn, movePawn));
    }

  }
}
