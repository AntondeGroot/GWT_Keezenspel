package adg.util;

import static adg.util.BoardLogic.getTileNr;
import static adg.util.CardValueCheck.isAce;
import static adg.util.CardValueCheck.isJack;
import static adg.util.CardValueCheck.isKing;
import static adg.util.CardValueCheck.isSeven;
import static adg.util.Selection.INVALID;
import static adg.util.Selection.VALID;

import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveType;
import com.adg.openapi.model.Pawn;

public class PawnAndCardSelectionValidation {

  public static SelectionValidation validate(Pawn pawn1, Pawn pawn2, Card card) {
    if (pawn1 == null && pawn2 == null) {
      return new SelectionValidation(INVALID);
    }

    if (card == null) {
      return new SelectionValidation(INVALID);
    }

    if (pawn1 != null) {
      // validate on board
      if (getTileNr(pawn1) < 0 && (isKing(card) || isAce(card))) {
        // on board
        return new SelectionValidation(VALID, MoveType.ON_BOARD);
      }
    }

    if (pawn1 != null && pawn2 != null) {
      // validate split
      if (getTileNr(pawn1) >= 0
          && getTileNr(pawn2) >= 0
          && pawn1.getPlayerId().equals(pawn2.getPlayerId())
          && isSeven(card)) {
        return new SelectionValidation(VALID, MoveType.SPLIT);
      }

      // validate switch — a Jack swaps your board pawn with another pawn on the board.
      // pawn2 is protected only on its OWN start tile (tileNr 0 in pawn2's own section).
      // Sitting on another player's start tile is also tileNr 0 but is a normal, switchable
      // spot, so we must compare the tile owner rather than reject every tile 0 (matches
      // ProcessOnSwitch.pawn2IsNotOnOwnStart).
      boolean pawn2OnOwnStart =
          getTileNr(pawn2) == 0
          && pawn2.getCurrentTileId().getPlayerId().equals(pawn2.getPlayerId());
      if (getTileNr(pawn1) >= 0
          && getTileNr(pawn1) <= 15
          && getTileNr(pawn2) >= 0
          && getTileNr(pawn2) <= 15
          && !pawn2OnOwnStart
          && isJack(card)) {
        return new SelectionValidation(VALID, MoveType.SWITCH);
      }

      // two pawns selected but no valid split or switch
      return new SelectionValidation(INVALID);
    }

    if (pawn1 == null) {
      return new SelectionValidation(INVALID);
    }

    // validate move
    if (getTileNr(pawn1) >= 0 && !(isKing(card) || isJack(card))) {
      return new SelectionValidation(VALID, MoveType.MOVE);
    }

    return new SelectionValidation(INVALID);
  }
}
