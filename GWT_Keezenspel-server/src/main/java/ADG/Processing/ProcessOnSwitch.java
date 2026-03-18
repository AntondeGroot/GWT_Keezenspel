package ADG.Processing;

import static ADG.util.BoardLogic.isPawnOnFinish;
import static ADG.util.BoardLogic.isPawnOnNest;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.CAN_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.INVALID_SELECTION;
import static com.adg.openapi.model.MoveResult.PLAYER_DOES_NOT_HAVE_CARD;
import static com.adg.openapi.model.MoveType.SWITCH;

import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PositionKey;
import java.util.LinkedList;
import java.util.Objects;

public class ProcessOnSwitch {

  public static void process(GameState gs, MoveRequest moveMessage, MoveResponse moveResponse) {
    Pawn pawn1t = gs.getPawn(moveMessage.getPawn1Id());
    Pawn pawn2t = gs.getPawn(moveMessage.getPawn2Id());
    Card card = gs.getCard(moveMessage.getCardId(), moveMessage.getPlayerId());

    if (pawn1t == null || card == null || pawn2t == null) {
      moveResponse.setResult(INVALID_SELECTION);
      return;
    }

    String selectedPawnPlayerId1 = moveMessage.getPawn1Id().getPlayerId();
    String selectedPawnPlayerId2 = moveMessage.getPawn2Id().getPlayerId();
    String playerId = moveMessage.getPlayerId();
    moveResponse.setMoveType(SWITCH);

    if (Objects.equals(selectedPawnPlayerId1, selectedPawnPlayerId2)) {
      moveResponse.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    if (!playerId.equals(selectedPawnPlayerId1) && (!playerId.equals(selectedPawnPlayerId2))) {
      moveResponse.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    if (!gs.playerHasCard(playerId, card)) {
      moveResponse.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return;
    }

    Pawn pawn1 = gs.getPawn(pawn1t);
    Pawn pawn2 = gs.getPawn(pawn2t);

    String tilePlayerId2 = pawn2.getCurrentTileId().getPlayerId();

    if (isPawnOnNest(pawn1)
        || isPawnOnNest(pawn2)
        || isPawnOnFinish(pawn1)
        || isPawnOnFinish(pawn2)) {
      moveResponse.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    int tileNr2 = pawn2.getCurrentTileId().getTileNr();
    if (tilePlayerId2.equals(pawn2.getPlayerId()) && tileNr2 == 0) {
      moveResponse.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    LinkedList<PositionKey> move1 = new LinkedList<>();
    LinkedList<PositionKey> move2 = new LinkedList<>();

    move1.add(pawn1.getCurrentTileId());
    move1.add(pawn2.getCurrentTileId());

    move2.add(pawn2.getCurrentTileId());
    move2.add(pawn1.getCurrentTileId());

    moveResponse.setMovePawn1(move1);
    moveResponse.setMovePawn2(move2);
    moveResponse.setPawn1(pawn1);
    moveResponse.setPawn2(pawn2);
    moveResponse.setResult(CAN_MAKE_MOVE);

    PositionKey tileId1 =
        new PositionKey(
            pawn1.getCurrentTileId().getPlayerId(), pawn1.getCurrentTileId().getTileNr());
    PositionKey tileId2 =
        new PositionKey(
            pawn2.getCurrentTileId().getPlayerId(), pawn2.getCurrentTileId().getTileNr());

    if (gs.isMakeMove(moveMessage)) {
      gs.movePawn(new Pawn(pawn1.getPlayerId(), pawn1.getPawnId(), tileId2, pawn1.getNestTileId()));
      gs.movePawn(new Pawn(pawn2.getPlayerId(), pawn2.getPawnId(), tileId1, pawn2.getNestTileId()));
      gs.finishTurn(playerId, card, true);
    }
  }
}