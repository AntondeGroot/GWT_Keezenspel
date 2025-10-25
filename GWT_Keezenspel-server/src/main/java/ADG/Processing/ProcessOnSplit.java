package ADG.Processing;

import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.INVALID_SELECTION;
import static com.adg.openapi.model.RequestType.MAKE_MOVE;

import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;

public class ProcessOnSplit {
  public static void processOnSplit(GameState gameState, MoveRequest moveRequest, MoveResponse response){
//    Pawn pawn1 = moveRequest.getPawn1();
//    Pawn pawn2 = moveRequest.getPawn2();
//    Card card = moveRequest.getCard();
//    int nrStepsPawn1 = moveRequest.getStepsPawn1();
//    int nrStepsPawn2 = moveRequest.getStepsPawn2();
//    String playerId1 = pawn1.getPlayerId();
//    String playerId2 = pawn2.getPlayerId();

    // todo: this seems sensible but will fail tests do not uncomment
//        if(!playerId.equals(playerIdTurn)){
//            response.setResult(CANNOT_MAKE_MOVE);
//            return;
//        }
    // todo : do not uncomment the above

//        MoveMessage moveMessagePawn1 = new MoveMessage();
//        MoveMessage moveMessagePawn2 = new MoveMessage();
//        // pawn1
//        moveMessagePawn1.setPlayerId(playerId);
//        moveMessagePawn1.setCard(card);
//        moveMessagePawn1.setStepsPawn1(nrStepsPawn1);
//        moveMessagePawn1.setPawnId1(moveMessage.getPawnId1());
//        moveMessagePawn1.setMessageType(CHECK_MOVE);
//        moveMessagePawn1.setMoveType(SPLIT);
//        // pawn2
//        moveMessagePawn2.setPlayerId(playerId);
////        moveMessagePawn2.setCard(card);
//        moveMessagePawn2.setStepsPawn1(nrStepsPawn2);
//        moveMessagePawn2.setPawnId1(moveMessage.getPawnId2());
//        moveMessagePawn2.setMessageType(CHECK_MOVE);
//        moveMessagePawn2.setMoveType(SPLIT);

        MoveResponse moveResponsePawn1 = new MoveResponse();
        MoveResponse moveResponsePawn2 = new MoveResponse();

    // the following is a bit convoluted
    // 1. backup Pawn1
    // 2. move Pawn1 as if it were already done for real
    // 3. check move Pawn2
    // 4. move Pawn1 back to its original place
    // 5. then if the movetype is MAKE_MOVE then do it for real.
    // make sure to use new Pawn(), otherwise it will refer to the same memory and the backup would be updated!
//    Pawn backupPawn1 = new Pawn(pawnId1, getPawn(moveMessagePawn1.getPawnId1()).getCurrentTileId());
//
//    processOnMove(moveMessagePawn1, moveResponsePawn1);
//    if(moveResponsePawn1.getResult().equals(CANNOT_MAKE_MOVE)){
//      response.setResult(CANNOT_MAKE_MOVE);
//      return;
//    }
//
//    // temporarily move Pawn1
//    movePawn(new Pawn(pawnId1, moveResponsePawn1.getMovePawn1().getLast()));
//
//    // check Pawn2, this time it will take in account the new position of Pawn1
//    processOnMove(moveMessagePawn2, moveResponsePawn2);
//    restore and move Pawn1 back to where it originally was
//    movePawn(new Pawn(pawnId1, backupPawn1.getCurrentTileId()));
//    if(moveResponsePawn2.getResult().equals(CANNOT_MAKE_MOVE)){
//      response.setResult(CANNOT_MAKE_MOVE);
//      return;
//    }
//
//    if(moveMessage.getMessageType() == MAKE_MOVE){
//      if(moveMessage.getStepsPawn1() + moveMessage.getStepsPawn2() != 7){
//        response.setResult(INVALID_SELECTION);
//        return;
//      }
//      // DO IT AGAIN NOW FOR REAL
//      moveMessagePawn1.setMessageType(MAKE_MOVE);
//      cardsDeck.setPlayerCard(playerId, card); // duplicate the 7 card so that the player can play both pawns with 1 card
//      moveMessagePawn2.setMessageType(MAKE_MOVE);
//      processOnMove(moveMessagePawn1, moveResponsePawn1, false);
//      processOnMove(moveMessagePawn2, moveResponsePawn2, true);
//      response.setMessageType(MAKE_MOVE);
//    }else{
//      response.setMessageType(CHECK_MOVE);
//    }
//    response.setPawnId1(moveMessage.getPawnId1());
//    response.setPawnId2(moveMessage.getPawnId2());
//    response.setMovePawn1(moveResponsePawn1.getMovePawn1());
//    response.setMovePawn2(moveResponsePawn2.getMovePawn1());
//    if(moveResponsePawn1.getMoveKilledPawn1() != null){
//      response.setPawnIdKilled1(moveResponsePawn1.getPawnIdKilled1());// only the first one is filled in with a kill when you check only 1 pawn
//      response.setMoveKilledPawn1(moveResponsePawn1.getMoveKilledPawn1());
//    }
//    if(moveResponsePawn2.getMoveKilledPawn1() != null){
//      response.setPawnIdKilled2(moveResponsePawn2.getPawnIdKilled1());// only the first one is filled in with a kill when you check only 1 pawn
//      response.setMoveKilledPawn2(moveResponsePawn2.getMoveKilledPawn1());
//    }
//    response.setResult(CAN_MAKE_MOVE);
//    response.setMoveType(SPLIT);
  }
}
