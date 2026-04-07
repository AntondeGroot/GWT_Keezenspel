package adg.processing;

import static adg.util.CardValueCheck.isAce;
import static adg.util.CardValueCheck.isKing;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.INVALID_SELECTION;
import static com.adg.openapi.model.MoveResult.PLAYER_DOES_NOT_HAVE_CARD;
import static com.adg.openapi.model.MoveType.ON_BOARD;

import adg.keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PositionKey;
import java.util.LinkedList;

public class ProcessOnBoard {

  public static void process(GameState gs, MoveRequest moveMessage, MoveResponse response) {
    Pawn pawn1 = gs.getPawn(moveMessage.getPawn1Id());
    Card card = gs.getCard(moveMessage.getCardId(), moveMessage.getPlayerId());
    String playerId = moveMessage.getPlayerId();
    response.setMoveType(ON_BOARD);

    if (!selectionIsValid(pawn1, card, response)) return;
    if (!playerHasCard(gs, playerId, card, response)) return;
    if (!cardIsAceOrKing(card, response)) return;

    PositionKey currentTileId = gs.getPawn(pawn1).getCurrentTileId();
    PositionKey targetTileId = new PositionKey(playerId, 0);

    if (!pawnIsOnNest(currentTileId, response)) return;
    if (!targetTileIsFree(gs, pawn1, targetTileId, response)) return;

    response.setPawn1(pawn1);
    response.setMovePawn1(buildMovePath(currentTileId, targetTileId));
    gs.processMove(pawn1, targetTileId, moveMessage, response);
  }

  private static boolean selectionIsValid(Pawn pawn1, Card card, MoveResponse response) {
    if (pawn1 == null || card == null) {
      response.setResult(INVALID_SELECTION);
      return false;
    }
    return true;
  }

  private static boolean playerHasCard(
      GameState gs, String playerId, Card card, MoveResponse response) {
    if (!gs.playerHasCard(playerId, card)) {
      response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return false;
    }
    return true;
  }

  private static boolean cardIsAceOrKing(Card card, MoveResponse response) {
    if (!(isAce(card) || isKing(card))) {
      response.setResult(CANNOT_MAKE_MOVE);
      return false;
    }
    return true;
  }

  private static boolean pawnIsOnNest(PositionKey currentTileId, MoveResponse response) {
    if (currentTileId.getTileNr() >= 0) {
      response.setResult(CANNOT_MAKE_MOVE);
      return false;
    }
    return true;
  }

  private static boolean targetTileIsFree(
      GameState gs, Pawn pawn1, PositionKey targetTileId, MoveResponse response) {
    if (!gs.canMoveToTile(pawn1, targetTileId)) {
      response.setResult(CANNOT_MAKE_MOVE);
      return false;
    }
    return true;
  }

  private static LinkedList<PositionKey> buildMovePath(
      PositionKey currentTileId, PositionKey targetTileId) {
    LinkedList<PositionKey> move = new LinkedList<>();
    move.add(currentTileId);
    move.add(targetTileId);
    return move;
  }
}