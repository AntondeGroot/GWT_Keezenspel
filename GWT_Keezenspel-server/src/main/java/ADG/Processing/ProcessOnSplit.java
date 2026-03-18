package ADG.Processing;

import static ADG.util.CardValueCheck.isSeven;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.CAN_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.INVALID_SELECTION;
import static com.adg.openapi.model.MoveResult.PLAYER_DOES_NOT_HAVE_CARD;
import static com.adg.openapi.model.MoveType.MOVE;
import static com.adg.openapi.model.MoveType.SPLIT;
import static com.adg.openapi.model.TempMessageType.CHECK_MOVE;

import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.TempMessageType;

public class ProcessOnSplit {

  public static void process(GameState gs, MoveRequest moveMessage, MoveResponse response) {
    Pawn pawn1 = gs.getPawn(moveMessage.getPawn1Id());
    Pawn pawn2 = gs.getPawn(moveMessage.getPawn2Id());
    Card card = gs.getCard(moveMessage.getCardId(), moveMessage.getPlayerId());

    if (pawn1 == null || card == null || pawn2 == null) {
      response.setResult(INVALID_SELECTION);
      return;
    }

    com.adg.openapi.model.MoveType moveType = moveMessage.getMoveType();
    int nrStepsPawn1 = moveMessage.getStepsPawn1();
    int nrStepsPawn2 = moveMessage.getStepsPawn2();
    String playerId = pawn1.getPlayerId();

    if (!playerId.equals(pawn2.getPlayerId())) {
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    if (!isSeven(card)) {
      response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return;
    }

    if ((nrStepsPawn1 + nrStepsPawn2 != 7) && moveType == MOVE) {
      response.setResult(INVALID_SELECTION);
      return;
    }

    MoveRequest moveMessagePawn1 = new MoveRequest();
    MoveRequest moveMessagePawn2 = new MoveRequest();
    // pawn1
    moveMessagePawn1.setPlayerId(playerId);
    moveMessagePawn1.setCardId(card.getUuid());
    moveMessagePawn1.setStepsPawn1(nrStepsPawn1);
    moveMessagePawn1.setPawn1Id(moveMessage.getPawn1Id());
    moveMessagePawn1.setTempMessageType(CHECK_MOVE);
    moveMessagePawn1.setMoveType(SPLIT);
    // pawn2
    moveMessagePawn2.setPlayerId(playerId);
    moveMessagePawn2.setCardId(card.getUuid());
    moveMessagePawn2.setStepsPawn1(nrStepsPawn2);
    moveMessagePawn2.setPawn1Id(moveMessage.getPawn2Id());
    moveMessagePawn2.setTempMessageType(CHECK_MOVE);
    moveMessagePawn2.setMoveType(SPLIT);

    MoveResponse moveResponsePawn1 = new MoveResponse();
    MoveResponse moveResponsePawn2 = new MoveResponse();

    Pawn backupPawn1 =
        new Pawn(
            pawn1.getPlayerId(),
            pawn1.getPawnId(),
            gs.getPawn(moveMessagePawn1.getPawn1Id()).getCurrentTileId(),
            pawn1.getNestTileId());

    ProcessOnMove.process(gs, moveMessagePawn1, moveResponsePawn1);
    if (moveResponsePawn1.getResult().equals(CANNOT_MAKE_MOVE)) {
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    gs.movePawn(
        new Pawn(
            pawn1.getPlayerId(),
            pawn1.getPawnId(),
            moveResponsePawn1.getMovePawn1().getLast(),
            pawn1.getNestTileId()));

    ProcessOnMove.process(gs, moveMessagePawn2, moveResponsePawn2);
    gs.movePawn(
        new Pawn(
            pawn1.getPlayerId(),
            pawn1.getPawnId(),
            backupPawn1.getCurrentTileId(),
            pawn1.getNestTileId()));

    if (moveResponsePawn2.getResult().equals(CANNOT_MAKE_MOVE)) {
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    if (TempMessageType.MAKE_MOVE.equals(moveMessage.getTempMessageType())) {
      if (moveMessage.getStepsPawn1() + moveMessage.getStepsPawn2() != 7) {
        response.setResult(INVALID_SELECTION);
        return;
      }
      moveMessagePawn1.setTempMessageType(TempMessageType.MAKE_MOVE);
      gs.duplicatePlayerCard(playerId, card);
      moveMessagePawn2.setTempMessageType(TempMessageType.MAKE_MOVE);
      ProcessOnMove.process(gs, moveMessagePawn1, moveResponsePawn1, false);
      ProcessOnMove.process(gs, moveMessagePawn2, moveResponsePawn2, true);
    }

    response.setPawn1(gs.getPawn(moveMessage.getPawn1Id()));
    response.setPawn2(gs.getPawn(moveMessage.getPawn2Id()));
    response.setMovePawn1(moveResponsePawn1.getMovePawn1());
    response.setMovePawn2(moveResponsePawn2.getMovePawn1());
    if (moveResponsePawn1.getMovePawnKilledByPawn1() != null) {
      response.setPawnKilledByPawn1(moveResponsePawn1.getPawnKilledByPawn1());
      response.setMovePawnKilledByPawn1(moveResponsePawn1.getMovePawnKilledByPawn1());
    }
    if (moveResponsePawn2.getMovePawnKilledByPawn2() != null) {
      response.setPawnKilledByPawn2(moveResponsePawn2.getPawnKilledByPawn1());
      response.setMovePawnKilledByPawn2(moveResponsePawn2.getMovePawnKilledByPawn1());
    }
    response.setResult(CAN_MAKE_MOVE);
    response.setMoveType(SPLIT);
  }
}