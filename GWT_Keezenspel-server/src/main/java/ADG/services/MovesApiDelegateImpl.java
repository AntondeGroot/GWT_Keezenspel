package ADG.services;

import static ADG.Processing.ProcessOnBoard.processOnBoard;
import static ADG.Processing.ProcessOnMove.processOnMove;
import static ADG.Processing.ProcessOnSplit.processOnSplit;
import static ADG.Processing.ProcessOnSwitch.processOnSwitch;
import static com.adg.openapi.model.MoveResult.CAN_MAKE_MOVE;

import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.util.PawnAndCardSelectionValidation;
import ADG.util.SelectionValidation;
import com.adg.openapi.api.MovesApiDelegate;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.TestMoveResponse;
import java.util.ArrayList;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MovesApiDelegateImpl implements MovesApiDelegate {

  @Override
  public ResponseEntity<TestMoveResponse> checkMove(String sessionId,
      String playerId,
      MoveRequest moveRequest) {

    if(!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    GameState gameState = gameSession.getGameState();

    if(!Objects.equals(playerId, gameState.getPlayerIdTurn())){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    SelectionValidation validation = PawnAndCardSelectionValidation.validate(moveRequest);
    if(!validation.isValid()){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    TestMoveResponse testMoveResponse = new TestMoveResponse();
    gameState.processTestMove(moveRequest, testMoveResponse);
  }

  @Override
  public ResponseEntity<MoveResponse> makeMove(String sessionId,
      String playerId,
      MoveRequest moveRequest) {

    if(!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    GameState gameState = gameSession.getGameState();

    if(!Objects.equals(playerId, gameState.getPlayerIdTurn())){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    MoveResponse response = new MoveResponse();
    response.setPawn1(moveRequest.getPawn1());
    response.setPawn2(moveRequest.getPawn2());

    // change the gamestate
    SelectionValidation validation = PawnAndCardSelectionValidation.validate(moveRequest);

    if(!validation.isValid()){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    switch(validation.getMoveType()){
      case MOVE -> processOnMove(gameState, ,response);
      case SPLIT -> processOnSplit(gameState,,response);
      case SWITCH -> processOnSwitch(gameState,,response);
      case ON_BOARD -> processOnBoard(gameState, ,response);

    // if you want to make a move for real && it is possible to do so
    // todo: move to gamestate itself
    if(Objects.equals(response.getResult(), CAN_MAKE_MOVE)){

      ArrayList<MoveResponse> moves = new ArrayList<>();
      moves.add(response);
      if(moves.size() > 1){
        moves.removeFirst();
      }
      gameSession.saveMoves(moves);
    }

    return response;
    }
}
