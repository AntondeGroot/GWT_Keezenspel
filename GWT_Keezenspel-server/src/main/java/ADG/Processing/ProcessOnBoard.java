package ADG.Processing;

import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;

public class ProcessOnBoard {
  public static void processOnBoard(MoveRequest moveMessage, MoveResponse response) {
    //        Pawn pawn1 = moveMessage.getPawn1();
    //        Card card = moveMessage.getCard();
    //        String playerId = moveMessage.getPlayerId();
    //
    //        // invalid selection
    //        if(pawn1 == null || card == null){
    //            response.setResult(INVALID_SELECTION);
    //            return;
    //        }
    //
    //        // player should have the card he's playing
    //        if(!cardsDeck.playerHasCard(playerId, card)) {
    //            response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
    //            return;
    //        }
    //
    //        // cannot go onboard without an Ace or King
    //        if(!(isAce(card) || isKing(card))){
    //            response.setResult(CANNOT_MAKE_MOVE);
    ////            response.setErrorMessage("You can't move on board without an Ace or King");
    //            return;
    //        }
    //
    //        PositionKey currentTileId = getPawn(pawn1).getCurrentTileId();
    //
    //        PositionKey targetTileId = new PositionKey(playerId,0);
    //        response.setMoveType(ONBOARD);
    //
    //        // when occupied by own pawn
    //        if(!canMoveToTile(pawn1, targetTileId)){
    //            response.setResult(CANNOT_MAKE_MOVE);
    //            return;
    //        }
    //
    //        // when pawn not in the nest
    //        if(currentTileId.getTileNr() >= 0 ){
    //            response.setResult(CANNOT_MAKE_MOVE);
    //            return;
    //        }
    //
    //        LinkedList<PositionKey> move = new LinkedList<>();
    //        move.add(currentTileId);
    //        move.add(targetTileId);
    //
    //        response.setPawn1(pawn1);
    //        response.setMovePawn1(move);
    //        processMove(pawn1, targetTileId, moveMessage, response);
  }
}
