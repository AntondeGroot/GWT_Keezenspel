package ADG.util;

import static ADG.util.BoardLogic.getTileNr;
import static ADG.util.CardValueCheck.isAce;
import static ADG.util.CardValueCheck.isJack;
import static ADG.util.CardValueCheck.isKing;
import static ADG.util.CardValueCheck.isSeven;
import static ADG.util.Selection.INVALID;
import static ADG.util.Selection.VALID;

import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveType;
import com.adg.openapi.model.Pawn;

public class PawnAndCardSelectionValidation {

  public static SelectionValidation validate(Pawn pawn1, Pawn pawn2, Card card) {
    if (pawn1 == null && pawn2 == null) {
      return new SelectionValidation(INVALID);
    }

    if(card == null) {
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
      if (getTileNr(pawn1) >= 0 &&
          getTileNr(pawn2) >= 0 &&
          pawn1.getPlayerId().equals(pawn2.getPlayerId()) &&
          isSeven(card)) {
        return new SelectionValidation(VALID, MoveType.SPLIT);
      }

      // validate switch
      if (getTileNr(pawn1) >= 0 && getTileNr(pawn1) <= 15 &&
          getTileNr(pawn2) > 0 && getTileNr(pawn2) <= 15 && // can not stand on his own start block
          isJack(card)) {
        return new SelectionValidation(VALID, MoveType.SWITCH);
      }
    }

    if (pawn1 == null) {
      return new SelectionValidation(INVALID);
    }

    // validate move
    if (getTileNr(pawn1) >= 0 &&
        !(isKing(card) || isJack(card))) {
      return new SelectionValidation(VALID, MoveType.MOVE);
    }

    return new SelectionValidation(INVALID);
  }
}
