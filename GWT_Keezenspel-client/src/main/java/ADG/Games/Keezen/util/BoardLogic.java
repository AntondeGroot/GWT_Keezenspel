package ADG.Games.Keezen.util;

import ADG.Games.Keezen.dto.PawnDTO;

public class BoardLogic {
  public static boolean isPawnOnNest(PawnDTO pawn){
    if(isInvalidPawn(pawn)){return false;}
    return pawn.getCurrentTileId().getTileNr() < 0;
  }

  public static boolean isPawnOnFinish(PawnDTO pawn){
    if(isInvalidPawn(pawn)){return false;}
    return pawn.getCurrentTileId().getTileNr() >= 16;
  }

  public static boolean pawnIsOnNormalBoard(PawnDTO pawn){
    return !isPawnOnNest(pawn) && !isPawnOnFinish(pawn);
  }

  private static boolean isInvalidPawn(PawnDTO pawn) {
    return pawn == null || pawn.getCurrentTileId() == null;
  }

  public static Integer getTileNr(PawnDTO pawn){
    return pawn.getCurrentTileId().getTileNr();
  }
}
