package ADG.Processing;

import static ADG.util.BoardLogic.isPawnOnFinish;
import static ADG.util.BoardLogic.isPawnOnNest;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.CAN_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.INVALID_SELECTION;
import static com.adg.openapi.model.MoveResult.PLAYER_DOES_NOT_HAVE_CARD;
import static com.adg.openapi.model.MoveType.SWITCH;
import static com.adg.openapi.model.RequestType.MAKE_MOVE;

import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PositionKey;
import java.util.LinkedList;
import java.util.Objects;

public class ProcessOnSwitch {
  public static void processOnSwitch(GameState gameState, MoveRequest moveMessage, MoveResponse moveResponse){

        Pawn pawnId1 = moveMessage.getPawn1();
        Pawn pawnId2 = moveMessage.getPawn2();
        Card card = moveMessage.getCard();

        // invalid selection
        if(pawnId1 == null || card == null || pawnId2 == null){
            moveResponse.setResult(INVALID_SELECTION);
            return;
        }

        String selectedPawnPlayerId1 = moveMessage.getPawn1().getPlayerId();
        String selectedPawnPlayerId2 = moveMessage.getPawn2().getPlayerId();
        String playerId = moveMessage.getPlayerId();
        // todo: this seems sensible but will fail tests
//        if(!playerId.equals(playerIdTurn)){
//            moveResponse.setResult(CANNOT_MAKE_MOVE);
//            return;
//        }
      //todo: do not uncomment above
        moveResponse.setMoveType(SWITCH);

        // You can't switch with yourself
        if(Objects.equals(selectedPawnPlayerId1, selectedPawnPlayerId2)){
            moveResponse.setResult(CANNOT_MAKE_MOVE);
            return;
        }
        // You can't switch two opponents
        if(!playerId.equals(selectedPawnPlayerId1) && (!playerId.equals(selectedPawnPlayerId2))){
            moveResponse.setResult(CANNOT_MAKE_MOVE);
            return;
        }
        if(!cardsDeck.playerHasCard(playerId, card)) {
            moveResponse.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        Pawn pawn1 = getPawn(pawnId1);
        Pawn pawn2 = getPawn(pawnId2);

        String tilePlayerId2 = pawn2.getCurrentTileId().getPlayerId();
        // pawns cannot move from Finish or from Nest
        if(isPawnOnNest(pawn1) || isPawnOnNest(pawn2) ||
            isPawnOnFinish(pawn1) || isPawnOnFinish(pawn2)){
            moveResponse.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        // player1 can move from start
        // player2 cannot be taken from start
        int tileNr2 = pawn2.getCurrentTileId().getTileNr();
        if(tilePlayerId2.equals(pawn2.getPlayerId()) && tileNr2 == 0){
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

        PositionKey tileId1 = new PositionKey(pawn1.getCurrentTileId());
        PositionKey tileId2 = new PositionKey(pawn2.getCurrentTileId());
        // switch in gamestate
        // only use the card when not testing
        if(moveMessage.getMessageType() == MAKE_MOVE){
            movePawn(new Pawn(pawnId1,tileId2));
            movePawn(new Pawn(pawnId2,tileId1));
            Boolean playerHasNoCardsLeft = cardsDeck.playerPlaysCard(playerId, card);
            if(playerHasNoCardsLeft){
                forfeitPlayer(playerId);
            }else{
                nextActivePlayer();
            }
        }
  }
}
