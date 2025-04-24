package ADG.Games.Keezen.logic;

import ADG.Games.Keezen.Player.Pawn;

public class BoardLogic {
  public static boolean isPawnOnNest(Pawn pawn){
    if(isInvalidPawn(pawn)){return false;}
    return pawn.getCurrentTileId().getTileNr() < 0;
  }

  public static boolean isPawnOnFinish(Pawn pawn){
    if(isInvalidPawn(pawn)){return false;}
    return pawn.getCurrentTileId().getTileNr() >= 16;
  }

  public static boolean pawnIsOnNormalBoard(Pawn pawn){
    return !isPawnOnNest(pawn) && !isPawnOnFinish(pawn);
  }

  private static boolean isInvalidPawn(Pawn pawn) {
    return pawn == null || pawn.getCurrentTileId() == null;
  }
}
