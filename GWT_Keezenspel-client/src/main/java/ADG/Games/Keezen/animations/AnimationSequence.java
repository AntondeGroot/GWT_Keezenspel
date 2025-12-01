package ADG.Games.Keezen.animations;

import ADG.Games.Keezen.TileId;
import ADG.Games.Keezen.dto.PawnClient;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AnimationSequence {

  private static final List<AnimatePawnPoints> sequenceMovePawn1 = new ArrayList<>();
  private static final List<AnimatePawnPoints> sequenceMovePawn2 = new ArrayList<>();
  private static final List<AnimatePawnPoints> sequenceKilledByPawn1 = new ArrayList<>();
  private static final List<AnimatePawnPoints> sequenceKilledByPawn2 = new ArrayList<>();

  public static List<AnimatePawnPoints> getFirst() {
    return sequenceMovePawn1;
  }

  public static List<AnimatePawnPoints> getLast() {
    return sequenceMovePawn2;
  }

  public static void reset() {
    //    sequenceMovePawn1.clear();
    //    sequenceMovePawn2.clear();
  }

  public static void movePawn(PawnClient pawn, LinkedList<TileId> movePawn, boolean animateFirst) {
    // animate the movement of the pawn
    //    if (animateFirst) {
    //      sequenceMovePawn1.add(new AnimatePawnPoints(pawn, movePawn));
    //    } else {
    //      sequenceMovePawn2.add(new AnimatePawnPoints(pawn, movePawn));
    //    }

  }
}
