package ADG.Processing;

import static ADG.util.BoardLogic.isPawnOnFinish;
import static ADG.util.CardValueCheck.isJack;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.PLAYER_DOES_NOT_HAVE_CARD;
import static com.adg.openapi.model.MoveType.MOVE;

import ADG.Log;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class ProcessOnMove {
  public static void processOnMove(MoveRequest moveMessage, MoveResponse response) {
    processOnMove(moveMessage, response, true);
  }

  public static void processOnMove(MoveRequest moveMessage, MoveResponse response, boolean goToNextPlayer){
        // only don't go to next player when playing a SPLIT card, since you have to make processOnMove twice
        Pawn pawn1 = moveMessage.getPawn1();
        Card card = moveMessage.getCard();
        String playerId = moveMessage.getPlayerId();
        // todo: this seems sensible but will fail tests
//        if(!playerId.equals(playerIdTurn)){
//            response.setResult(CANNOT_MAKE_MOVE);
//            return;
//        }
        PositionKey currentTileId = pawn1.getCurrentTileId();
        int nrSteps = moveMessage.getStepsPawn1();
        int next;
        String playerIdOfTile = currentTileId.getPlayerId();
        Log.info("moveMessage = "+moveMessage);
        LinkedList<PositionKey> moves = new LinkedList<>();
        response.setMoveType(MOVE);
        Log.info("GameState: OnMove: received msg: " + moveMessage);
        PositionKey startTileId;

        // You cannot move from nest tiles
        if(currentTileId.getTileNr() < 0){
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        // Player must have the card he wants to play
        if(!cardsDeck.playerHasCard(playerId, card)) {
            response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        // Player cannot move an opponents pawn without playing a Jack
        if(!isJack(card)){
            if(!Objects.equals(moveMessage.getPawn1().getPlayerId(), playerId)){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            if(moveMessage.getPawn2() != null && !Objects.equals(moveMessage.getPawn2().getPlayerId(), playerId)){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
        }

        moves.add(currentTileId);
        next = currentTileId.getTileNr() + moveMessage.getStepsPawn1();

         // regular route
        if (next > 15 &&
                !isPawnOnLastSection(playerId, playerIdOfTile) &&
                !isPawnOnFinish(pawn1) ) {
            Log.info("GameState: OnMove: normal route between 0,15 but could move to next section");
            // check

            if(currentTileId.getTileNr() < 1){moves.add(new PositionKey(currentTileId.getPlayerId(), 1));}
            if(currentTileId.getTileNr() < 7){moves.add(new PositionKey(currentTileId.getPlayerId(), 7));}
            if(currentTileId.getTileNr() < 13){moves.add(new PositionKey(currentTileId.getPlayerId(), 13));}
            if(currentTileId.getTileNr() < 15){moves.add(new PositionKey(currentTileId.getPlayerId(), 15));}

            startTileId = new PositionKey(nextPlayerId(playerIdOfTile), 0);
            if (canPassStartTile(pawn1, startTileId)){
                Log.info("GameState: OnMove: can move past StartTile "+new PositionKey(playerIdOfTile+1,0));
                Log.info("GameState: OnMove: normal route can move to the next section");
                next = next % 16;
                playerIdOfTile = nextPlayerId(playerIdOfTile);
                if(next > 1){moves.add(new PositionKey(playerIdOfTile, 1));}
                if(next > 7){moves.add(new PositionKey(playerIdOfTile, 7));}
            }else { // or turn back
                Log.info("GameState: OnMove: normal route is blocked by a start tile, move backwards");
                next = 15 - next%15;
                moves.add(new PositionKey(playerIdOfTile, 15));
                if(next < 13){moves.add(new PositionKey(playerIdOfTile, 13));}
                if(next < 7){moves.add(new PositionKey(playerIdOfTile, 7));}
            }

            PositionKey nextTileId = new PositionKey(playerIdOfTile, next);
            moves.add(nextTileId);
            if(canMoveToTile(pawn1, nextTileId)){
                response.setMovePawn1(moves);
                processMove(pawn1, new PositionKey(playerIdOfTile,next), moveMessage, response, goToNextPlayer);
            }else{
                response.setResult(CANNOT_MAKE_MOVE);
            }
            return;
        }

        // normal route within section
        if(next > 0 &&
                next <= 15 &&
                !isPawnOnFinish(pawn1)){
            Log.info("GameState: OnMove: normal route between 0,15");
            // check if you can kill an opponent
            PositionKey nextTileId = new PositionKey(playerIdOfTile, next);

            // in case you end up on your own pawn
            if(!canMoveToTile(pawn1,nextTileId)){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            if(nrSteps > 0) {
                if (next > 1 && currentTileId.getTileNr() < 1) {
                    moves.add(new PositionKey(playerIdOfTile, 1));
                }
                if (next > 7 && currentTileId.getTileNr() < 7) {
                    moves.add(new PositionKey(playerIdOfTile, 7));
                }
                if (next > 13 && currentTileId.getTileNr() < 13) {
                    moves.add(new PositionKey(playerIdOfTile, 13));
                }
            }else{
                if(next < 13 && currentTileId.getTileNr() > 13){moves.add(new PositionKey(playerIdOfTile, 13));}
                if(next < 7 && currentTileId.getTileNr() > 7){moves.add(new PositionKey(playerIdOfTile, 7));}
                if(next < 1 && currentTileId.getTileNr() > 1){moves.add(new PositionKey(playerIdOfTile, 1));}
            }

            moves.add(nextTileId);
            response.setMovePawn1(moves);

            processMove(pawn1, new PositionKey(playerIdOfTile,next), moveMessage, response, goToNextPlayer);

            return;
        }

        // you go negative
        if(next < 0){
            Log.info("GameState: OnMove: pawn goes backwards");
            if(currentTileId.getTileNr() > 1){moves.add(new PositionKey(playerIdOfTile, 1));}

            // check if you can pass || otherwise turn back i.e. forward
            startTileId = new PositionKey(playerIdOfTile, 0);
            if (canPassStartTile(pawn1, startTileId)){
                next = 16 + next;
                playerIdOfTile = previousPlayerId(playerIdOfTile);
                if(next < 13){moves.add(new PositionKey(playerIdOfTile, 13));}
            }else { // or turn back (forwards since next is negative)
                Log.info("GameState: OnMove: pawn wants to go backwards but is blocked by a start tile, goes forwards");
                next = -next+2; // +1 : you can't move on tile 0 and would then move on tile 1 twice.
            }

            PositionKey nextTileId = new PositionKey(playerIdOfTile, next);
            moves.add(nextTileId);
            if(canMoveToTile(pawn1, nextTileId)){
                response.setMovePawn1(moves);
                processMove(pawn1, nextTileId, moveMessage, response, goToNextPlayer);
            }else{
                response.setResult(CANNOT_MAKE_MOVE);
            }
            return;
        }

        // when moving backwards and ending exactly on the starttile
        if(next == 0){
            Log.info("GameState: OnMove: pawn ends exactly on start tile");
            if(currentTileId.getTileNr() > 1){moves.add(new PositionKey(playerIdOfTile, 1));}
            if (canMoveToTile(pawn1, new PositionKey(playerIdOfTile,0))) {
                moves.add(new PositionKey(playerIdOfTile, 0));
                response.setMovePawn1(moves);
                processMove(pawn1, new PositionKey(playerIdOfTile,0), moveMessage, response, goToNextPlayer);
                return;
            }

            // if your own pawn is on start, but it is not a blockade: your move is invalid
            if(!tileIsABlockade(new PositionKey(playerIdOfTile,0)) && cannotMoveToTileBecauseSamePlayer(pawn1, new PositionKey(playerIdOfTile,0))){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            // move forwards to tile 2 if you can
            if(canMoveToTile(pawn1, new PositionKey(playerIdOfTile, 2))){
                moves.add(new PositionKey(playerIdOfTile, 2));
                response.setMovePawn1(moves);
                processMove(pawn1, new PositionKey(playerIdOfTile,2), moveMessage, response, goToNextPlayer);
                return;
            }else{
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
        }

        // pawn is already on finish
        if (isPawnOnFinish(pawn1)){
            Log.info("GameState: OnMove: pawn is already on the finish");
            // moving is not possible when the pawn is directly between two other pawns
            if (isPawnTightlyClosedIn(pawn1, currentTileId)){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            // moving between pawns on the finish tile
            if(isPawnLooselyClosedIn(pawn1, currentTileId)){
                ArrayList<PositionKey> pingpongmoves = pingpongMove(pawn1, currentTileId, nrSteps);
                moves.clear();
                moves.addAll(pingpongmoves);// todo is this necessary?
                response.setMovePawn1(moves);
                processMove(pawn1, pingpongmoves.getLast(), moveMessage, response, goToNextPlayer);
                return;
            }

            PositionKey targetTileId = moveAndCheckEveryTile(pawn1, currentTileId, nrSteps);
            int tileHighestTileNr = 0;
            if(nrSteps > 0){
                tileHighestTileNr = checkHighestTileNrYouCanMoveTo(pawn1, currentTileId, nrSteps);
                if (tileHighestTileNr > targetTileId.getTileNr()) {
                    Log.info("GameState: OnMove: pawn moves out of the finish");
                    moves.add(new PositionKey(playerIdOfTile, tileHighestTileNr));
                }
            }
            if (targetTileId.getTileNr() < 15) {moves.add(new PositionKey(targetTileId.getPlayerId(), 15));}
            if (targetTileId.getTileNr() < 13) {moves.add(new PositionKey(targetTileId.getPlayerId(), 13));}
            if (targetTileId.getTileNr() < 7) {moves.add(new PositionKey(targetTileId.getPlayerId(), 7));}

            moves.add(targetTileId);
            response.setMovePawn1(moves);
            processMove(pawn1, targetTileId, moveMessage, response, goToNextPlayer);
            return;
        }

        if(next > 15 &&
                isPawnOnLastSection(playerId, playerIdOfTile)){
            Log.info("GameState: OnMove: pawn is on last section and goes into finish");
            if(currentTileId.getTileNr() < 7){moves.add(new PositionKey(currentTileId.getPlayerId(), 7));}
            if(currentTileId.getTileNr() < 13){moves.add(new PositionKey(currentTileId.getPlayerId(), 13));}
            if(currentTileId.getTileNr() < 15){moves.add(new PositionKey(currentTileId.getPlayerId(), 15));}

            PositionKey targetTileId = moveAndCheckEveryTile(pawn1, currentTileId, nrSteps);

            int tileHighestTileNr = checkHighestTileNrYouCanMoveTo(pawn1, currentTileId, nrSteps);
            if(tileHighestTileNr > targetTileId.getTileNr()){
                if(tileHighestTileNr > 15){
                    // move to finish
                    moves.add(new PositionKey(nextPlayerId(playerIdOfTile), tileHighestTileNr));
                    // possibly move back out of finish
                    // otherwise if 16 was taken, then it would move (0,15) (1,15) (0,15) and then correctly back
                    if(targetTileId.getTileNr() < 15){moves.add(new PositionKey(targetTileId.getPlayerId(), 15));}
                }
                if(targetTileId.getTileNr() < 13){moves.add(new PositionKey(targetTileId.getPlayerId(), 13));}
                if(targetTileId.getTileNr() < 7){moves.add(new PositionKey(targetTileId.getPlayerId(), 7));}
            }

            if(cannotMoveToTileBecauseSamePlayer(pawn1, targetTileId)){
                clearResponse(response);
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }

            moves.add(targetTileId);
            response.setMovePawn1(moves);
            processMove(pawn1, targetTileId, moveMessage, response, goToNextPlayer);
        }
  }
  public int checkHighestTileNrYouCanMoveTo(PawnId pawnId, TileId tileId, int nrSteps) {
    int direction = 1;
    int tileNrToCheck = tileId.getTileNr();

    if (nrSteps < 0) {
      direction = -1;
      nrSteps = -nrSteps;
    }

    for (int i = 0; i < nrSteps; i++) {
      tileNrToCheck = tileNrToCheck + direction;

      if (!canMoveToTile(pawnId, new PositionKey(pawnId.getPlayerId(), tileNrToCheck))) {
        return tileNrToCheck - 1;
      }
    }
    return tileNrToCheck;
  }

  /**
   * @param selectedPawn
   * @param nextTileId
   * @Return True if it ends on own position
   * @Return False it it ends on own other pawn of same player
   * @Return False if it ends on blockaded starting tile
   *
   */
  private boolean canMoveToTile(Pawn selectedPawn, PositionKey nextTileId){
    if(nextTileId.getTileNr() > 19){
      return false;
    }
    Pawn pawn = getPawn(nextTileId);
    if(pawn != null) {
      Log.info("found pawn on start tile: "+pawn);
      if(pawn.getPawnId().equals(selectedPawn.getPawnId())){
        return true;
      }

      if (Objects.equals(pawn.getPlayerId(), selectedPawn.getPlayerId())) {
        return false;
      }
      if (Objects.equals(pawn.getPlayerId(), nextTileId.getPlayerId()) && nextTileId.getTileNr() == 0){
        return false;
      }
    }
    return true;
  }

  private boolean cannotMoveToTileBecauseSamePlayer(PawnId selectedPawnId, PositionKey nextTileId){
    Pawn pawn = getPawn(nextTileId);
    if(pawn != null) {
      if (Objects.equals(pawn.getPlayerId(), selectedPawnId.getPlayerId()) && !pawn.getPawnId().equals(selectedPawnId)) {
        return true;
      }
    }
    return false;
  }

  public boolean canPassStartTile(PawnId selectedPawnId, PositionKey tileId){
    Pawn pawnOnTile = getPawn(tileId);

    if(pawnOnTile == null){
      return true;
    }

    if(selectedPawnId.equals(pawnOnTile.getPawnId())){
      return true;
    }

    if(Objects.equals(pawnOnTile.getPlayerId(), tileId.getPlayerId())) {
      return false;
    }

    return true;
  }

  public PositionKey moveAndCheckEveryTile(Pawn pawn, PositionKey tileId, int nrSteps){
    int direction = 1;
    int tileNrToCheck = tileId.getTileNr();

    if (nrSteps<0){
      direction = -1;
      nrSteps = - nrSteps;
    }

    for (int i = 0; i < nrSteps; i++) {
      tileNrToCheck = tileNrToCheck + direction;
      if(tileNrToCheck > 15){// only check tiles when they are on the finish
        if(!canMoveToTile(pawn, new PositionKey(pawn.getPlayerId(), tileNrToCheck))) {
          direction = - direction;
          tileNrToCheck = tileNrToCheck + 2*direction;
        }
      }
    }

    if(tileNrToCheck <= 15){// when back on the last section, change the playerId of the section
      return new PositionKey(previousPlayerId(pawn.getPlayerId()), tileNrToCheck);
    }

    return new PositionKey(pawn.getPlayerId(), tileNrToCheck);

  }

  public ArrayList<PositionKey> pingpongMove(Pawn pawn, PositionKey tileId , int nrSteps){
    // it is already guaranteed that a pawn is loosely closed in on the finish tiles
    ArrayList<PositionKey> moves = new ArrayList<>();
    String playerId = pawn.getPlayerId();
    int direction = 1;
    int tileNrToCheck = tileId.getTileNr();
    moves.add(tileId);
    if (nrSteps < 0) {
      direction = -1;
      nrSteps = Math.abs(nrSteps);
    }

    for (int i = 0; i < nrSteps; i++) {
      tileNrToCheck = tileNrToCheck + direction;
      if(!canMoveToTile(pawn, new PositionKey(playerId, tileNrToCheck))) {
        moves.add(new PositionKey(playerId, tileNrToCheck-direction));
        direction = - direction;
        tileNrToCheck = tileNrToCheck + 2*direction;
      }
    }
    moves.add(new PositionKey(playerId, tileNrToCheck));

    // an extra check to see if the first two moves are identical. this can happen when you do -4 steps and are
    // closed in from behind or try to move forward but are blocked that way.
    if(moves.size() >= 2){
      if(moves.get(0).equals(moves.get(1))){
        moves.removeFirst();
      }
    }
    return moves;
  }

  //todo: this should no longer matter with our own rules
  private boolean isPawnLooselyClosedIn(Pawn pawn, TileId tileId){
    int tileNr = tileId.getTileNr();
    String playerId = pawn.getPlayerId();

    if(tileNr <= 16){
      return false;
    }

    for (int i = tileId.getTileNr(); i > 16; i--) {
      if(!canMoveToTile(pawn, new PositionKey(playerId, i-1))){
        return true;
      }
    }

    return false;
  }

  // todo: this should no longer matter with our own rules
  private boolean isPawnTightlyClosedIn(Pawn pawn, PositionKey tileId){
    String playerId = pawn.getPlayerId();
    if(tileId.getTileNr() == 19
        && !canMoveToTile(pawn, new PositionKey(playerId, 18))){
      return true;
    }

    if(tileId.getTileNr() == 18
        && !canMoveToTile(pawn, new PositionKey(playerId, 19))
        && !canMoveToTile(pawn, new PositionKey(playerId, 17))){
      return true;
    }

    if(tileId.getTileNr() == 17
        && !canMoveToTile(pawn, new PositionKey(playerId, 18))
        && !canMoveToTile(pawn,new PositionKey(playerId, 16))){
      return true;
    }

    return false;
  }

  public boolean tileIsABlockade(PositionKey selectedStartTile){
    Pawn pawnOnStart = getPawn(selectedStartTile);
    if(pawnOnStart == null){
      return false;
    }
    if(Objects.equals(pawnOnStart.getPawnId().getPlayerId(), selectedStartTile.getPlayerId())){
      return true;
    }
    return false;
  }
}
