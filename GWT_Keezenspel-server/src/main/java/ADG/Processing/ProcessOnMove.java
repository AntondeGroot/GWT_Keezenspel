package ADG.Processing;

import static ADG.util.BoardLogic.isPawnOnFinish;
import static ADG.util.CardValueCheck.isJack;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.INVALID_SELECTION;
import static com.adg.openapi.model.MoveResult.PLAYER_DOES_NOT_HAVE_CARD;
import static com.adg.openapi.model.MoveType.MOVE;

import ADG.Games.Keezen.GameState;
import ADG.Log;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PositionKey;
import java.util.LinkedList;
import java.util.Objects;

public class ProcessOnMove {

  public static void process(GameState gs, MoveRequest moveMessage, MoveResponse response) {
    process(gs, moveMessage, response, true);
  }

  public static void process(
      GameState gs, MoveRequest moveMessage, MoveResponse response, boolean goToNextPlayer) {
    Pawn pawn1 = gs.getPawn(moveMessage.getPawn1Id());
    Card card = gs.getCard(moveMessage.getCardId(), moveMessage.getPlayerId());
    if (pawn1 == null || card == null) {
      response.setResult(INVALID_SELECTION);
      return;
    }

    String playerId = moveMessage.getPlayerId();
    PositionKey currentTileId = pawn1.getCurrentTileId();
    int nrSteps = moveMessage.getStepsPawn1();
    int next;
    String playerIdOfTile = currentTileId.getPlayerId();
    Log.info("moveMessage = " + moveMessage);
    LinkedList<PositionKey> moves = new LinkedList<>();
    response.setMoveType(MOVE);
    Log.info("GameState: OnMove: received msg: " + moveMessage);
    PositionKey startTileId;

    if (currentTileId.getTileNr() < 0) {
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    if (!gs.playerHasCard(playerId, card)) {
      response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return;
    }

    if (!isJack(card)) {
      if (moveMessage.getPawn1Id() != null
          && !Objects.equals(moveMessage.getPawn1Id().getPlayerId(), playerId)) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
      if (moveMessage.getPawn2Id() != null
          && !Objects.equals(moveMessage.getPawn2Id().getPlayerId(), playerId)) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
    }

    moves.add(currentTileId);
    next = currentTileId.getTileNr() + moveMessage.getStepsPawn1();

    // regular route
    if (next > 15 && !gs.isPawnOnLastSection(playerId, playerIdOfTile) && !isPawnOnFinish(pawn1)) {
      Log.info("GameState: OnMove: normal route between 0,15 but could move to next section");

      if (currentTileId.getTileNr() < 1) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 1));
      }
      if (currentTileId.getTileNr() < 7) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 7));
      }
      if (currentTileId.getTileNr() < 13) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 13));
      }
      if (currentTileId.getTileNr() < 15) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 15));
      }

      startTileId = new PositionKey(gs.nextPlayerId(playerIdOfTile), 0);
      if (gs.canPassStartTile(pawn1, startTileId)) {
        Log.info(
            "GameState: OnMove: can move past StartTile "
                + new PositionKey(playerIdOfTile + 1, 0));
        Log.info("GameState: OnMove: normal route can move to the next section");
        next = next % 16;
        playerIdOfTile = gs.nextPlayerId(playerIdOfTile);
        if (next > 1) {
          moves.add(new PositionKey(playerIdOfTile, 1));
        }
        if (next > 7) {
          moves.add(new PositionKey(playerIdOfTile, 7));
        }
      } else {
        Log.info("GameState: OnMove: normal route is blocked by a start tile, move backwards");
        if (gs.isExactMoveRequired()) {
          response.setResult(CANNOT_MAKE_MOVE);
          return;
        }
        next = 15 - next % 15;
        moves.add(new PositionKey(playerIdOfTile, 15));
        if (next < 13) {
          moves.add(new PositionKey(playerIdOfTile, 13));
        }
        if (next < 7) {
          moves.add(new PositionKey(playerIdOfTile, 7));
        }
      }

      PositionKey nextTileId = new PositionKey(playerIdOfTile, next);
      moves.add(nextTileId);
      if (gs.canMoveToTile(pawn1, nextTileId)) {
        response.setMovePawn1(moves);
        gs.processMove(
            pawn1, new PositionKey(playerIdOfTile, next), moveMessage, response, goToNextPlayer);
      } else {
        response.setResult(CANNOT_MAKE_MOVE);
      }
      return;
    }

    // normal route within section
    if (next > 0 && next <= 15 && !isPawnOnFinish(pawn1)) {
      Log.info("GameState: OnMove: normal route between 0,15");
      PositionKey nextTileId = new PositionKey(playerIdOfTile, next);

      if (!gs.canMoveToTile(pawn1, nextTileId)) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
      if (nrSteps > 0) {
        if (next > 1 && currentTileId.getTileNr() < 1) {
          moves.add(new PositionKey(playerIdOfTile, 1));
        }
        if (next > 7 && currentTileId.getTileNr() < 7) {
          moves.add(new PositionKey(playerIdOfTile, 7));
        }
        if (next > 13 && currentTileId.getTileNr() < 13) {
          moves.add(new PositionKey(playerIdOfTile, 13));
        }
      } else {
        if (next < 13 && currentTileId.getTileNr() > 13) {
          moves.add(new PositionKey(playerIdOfTile, 13));
        }
        if (next < 7 && currentTileId.getTileNr() > 7) {
          moves.add(new PositionKey(playerIdOfTile, 7));
        }
        if (next < 1 && currentTileId.getTileNr() > 1) {
          moves.add(new PositionKey(playerIdOfTile, 1));
        }
      }

      moves.add(nextTileId);
      response.setMovePawn1(moves);
      gs.processMove(
          pawn1, new PositionKey(playerIdOfTile, next), moveMessage, response, goToNextPlayer);
      return;
    }

    // you go negative
    if (next < 0) {
      Log.info("GameState: OnMove: pawn goes backwards");
      if (currentTileId.getTileNr() > 1) {
        moves.add(new PositionKey(playerIdOfTile, 1));
      }

      startTileId = new PositionKey(playerIdOfTile, 0);
      if (gs.canPassStartTile(pawn1, startTileId)) {
        next = 16 + next;
        playerIdOfTile = gs.previousPlayerId(playerIdOfTile);
        if (next < 13) {
          moves.add(new PositionKey(playerIdOfTile, 13));
        }
      } else {
        Log.info(
            "GameState: OnMove: pawn wants to go backwards but is blocked by a start tile, goes forwards");
        if (gs.isExactMoveRequired()) {
          response.setResult(CANNOT_MAKE_MOVE);
          return;
        }
        next = -next + 2;
      }

      PositionKey nextTileId = new PositionKey(playerIdOfTile, next);
      moves.add(nextTileId);
      if (gs.canMoveToTile(pawn1, nextTileId)) {
        response.setMovePawn1(moves);
        gs.processMove(pawn1, nextTileId, moveMessage, response, goToNextPlayer);
      } else {
        response.setResult(CANNOT_MAKE_MOVE);
      }
      return;
    }

    // when moving backwards and ending exactly on the starttile
    if (next == 0) {
      Log.info("GameState: OnMove: pawn ends exactly on start tile");
      if (currentTileId.getTileNr() > 1) {
        moves.add(new PositionKey(playerIdOfTile, 1));
      }
      if (gs.canMoveToTile(pawn1, new PositionKey(playerIdOfTile, 0))) {
        moves.add(new PositionKey(playerIdOfTile, 0));
        response.setMovePawn1(moves);
        gs.processMove(
            pawn1, new PositionKey(playerIdOfTile, 0), moveMessage, response, goToNextPlayer);
        return;
      }

      if (!gs.tileIsABlockade(new PositionKey(playerIdOfTile, 0))
          && gs.cannotMoveToTileBecauseSamePlayer(pawn1, new PositionKey(playerIdOfTile, 0))) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }

      if (gs.canMoveToTile(pawn1, new PositionKey(playerIdOfTile, 2))) {
        moves.add(new PositionKey(playerIdOfTile, 2));
        response.setMovePawn1(moves);
        gs.processMove(
            pawn1, new PositionKey(playerIdOfTile, 2), moveMessage, response, goToNextPlayer);
        return;
      } else {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
    }

    // pawn is already on finish
    if (isPawnOnFinish(pawn1)) {
      Log.info("GameState: OnMove: pawn is already on the finish");
      if (gs.isPawnTightlyClosedIn(pawn1, currentTileId)) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
      if (gs.isPawnLooselyClosedIn(pawn1, currentTileId)) {
        if (gs.isExactMoveRequired()) {
          response.setResult(CANNOT_MAKE_MOVE);
          return;
        }
        LinkedList<PositionKey> pingpongmoves =
            new LinkedList<>(gs.pingpongMove(pawn1, currentTileId, nrSteps));
        moves.clear();
        moves.addAll(pingpongmoves);
        response.setMovePawn1(moves);
        gs.processMove(pawn1, pingpongmoves.getLast(), moveMessage, response, goToNextPlayer);
        return;
      }

      PositionKey targetTileId = gs.moveAndCheckEveryTile(pawn1, currentTileId, nrSteps);
      int tileHighestTileNr = 0;
      if (nrSteps > 0) {
        tileHighestTileNr = gs.checkHighestTileNrYouCanMoveTo(pawn1, currentTileId, nrSteps);
        if (tileHighestTileNr > targetTileId.getTileNr()) {
          if (gs.isExactMoveRequired()) {
            response.setResult(CANNOT_MAKE_MOVE);
            return;
          }
          Log.info("GameState: OnMove: pawn moves out of the finish");
          moves.add(new PositionKey(playerIdOfTile, tileHighestTileNr));
        }
      }
      if (targetTileId.getTileNr() < 15) {
        moves.add(new PositionKey(targetTileId.getPlayerId(), 15));
      }
      if (targetTileId.getTileNr() < 13) {
        moves.add(new PositionKey(targetTileId.getPlayerId(), 13));
      }
      if (targetTileId.getTileNr() < 7) {
        moves.add(new PositionKey(targetTileId.getPlayerId(), 7));
      }

      moves.add(targetTileId);
      response.setMovePawn1(moves);
      gs.processMove(pawn1, targetTileId, moveMessage, response, goToNextPlayer);
      return;
    }

    if (next > 15 && gs.isPawnOnLastSection(playerId, playerIdOfTile)) {
      Log.info("GameState: OnMove: pawn is on last section and goes into finish");
      if (currentTileId.getTileNr() < 7) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 7));
      }
      if (currentTileId.getTileNr() < 13) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 13));
      }
      if (currentTileId.getTileNr() < 15) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 15));
      }

      PositionKey targetTileId = gs.moveAndCheckEveryTile(pawn1, currentTileId, nrSteps);

      int tileHighestTileNr = gs.checkHighestTileNrYouCanMoveTo(pawn1, currentTileId, nrSteps);
      if (tileHighestTileNr > targetTileId.getTileNr()) {
        if (gs.isExactMoveRequired()) {
          response.setResult(CANNOT_MAKE_MOVE);
          return;
        }
        if (tileHighestTileNr > 15) {
          moves.add(new PositionKey(gs.nextPlayerId(playerIdOfTile), tileHighestTileNr));
          if (targetTileId.getTileNr() < 15) {
            moves.add(new PositionKey(targetTileId.getPlayerId(), 15));
          }
        }
        if (targetTileId.getTileNr() < 13) {
          moves.add(new PositionKey(targetTileId.getPlayerId(), 13));
        }
        if (targetTileId.getTileNr() < 7) {
          moves.add(new PositionKey(targetTileId.getPlayerId(), 7));
        }
      }

      if (gs.cannotMoveToTileBecauseSamePlayer(pawn1, targetTileId)) {
        gs.clearResponse(response);
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }

      moves.add(targetTileId);
      response.setMovePawn1(moves);
      gs.processMove(pawn1, targetTileId, moveMessage, response, goToNextPlayer);
    }
  }
}