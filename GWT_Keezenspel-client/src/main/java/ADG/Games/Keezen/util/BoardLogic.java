package ADG.Games.Keezen.util;

import ADG.Games.Keezen.dto.PawnClient;

public class BoardLogic {

  public static boolean isPawnOnNest(PawnClient pawn) {
    if (isInvalidPawn(pawn)) {
      return false;
    }
    return pawn.getCurrentTileId().getTileNr() < 0;
  }

  public static boolean isPawnOnFinish(PawnClient pawn) {
    if (isInvalidPawn(pawn)) {
      return false;
    }
    return pawn.getCurrentTileId().getTileNr() >= 16;
  }

  public static boolean pawnIsOnNormalBoard(PawnClient pawn) {
    return !isPawnOnNest(pawn) && !isPawnOnFinish(pawn);
  }

  private static boolean isInvalidPawn(PawnClient pawn) {
    return pawn == null || pawn.getCurrentTileId() == null;
  }

  public static Integer getTileNr(PawnClient pawn) {
    return pawn.getCurrentTileId().getTileNr();
  }
}
