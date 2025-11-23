package ADG.services;

import static com.adg.openapi.model.MoveResult.CAN_MAKE_MOVE;
import static com.adg.openapi.model.TempMessageType.CHECK_MOVE;
import static com.adg.openapi.model.TempMessageType.MAKE_MOVE;

import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.util.PawnAndCardSelectionValidation;
import ADG.util.SelectionValidation;
import com.adg.openapi.api.MovesApiDelegate;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
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
    System.out.println("server received move request" + moveRequest);
    if (!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)) {
      System.out.println("there was no session found with id: " + sessionId);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    GameState gameState = gameSession.getGameState();

    if (!Objects.equals(playerId, gameState.getPlayerIdTurn())) {
      System.out.println("It was not the player's turn with id: " + playerId + ", it was: "+gameState.getPlayerIdTurn());
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    Card card = gameState.getCard(moveRequest.getCardId(), moveRequest.getPlayerId());
    Pawn pawn1 = gameState.getPawn(moveRequest.getPawn1Id());
    Pawn pawn2 = gameState.getPawn(moveRequest.getPawn2Id());
    System.out.println("pawn1: " + pawn1);
    System.out.println("pawn2: " + pawn2);
    System.out.println("card: " + card);
    SelectionValidation validation = PawnAndCardSelectionValidation.validate(pawn1, pawn2, card);
    System.out.println("card validation resulted in : " + validation.isValid());
    if (!validation.isValid()) {
      System.out.println("card validation failed");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    MoveResponse response = new MoveResponse();
    moveRequest.setTempMessageType(CHECK_MOVE);
    switch (validation.getMoveType()) {
      case MOVE -> gameState.processOnMove(moveRequest, response);
      case SPLIT -> gameState.processOnSplit(moveRequest, response);
      case SWITCH -> gameState.processOnSwitch(moveRequest, response);
      case ON_BOARD -> gameState.processOnBoard(moveRequest, response);
    }

    TestMoveResponse testMoveResponse = new TestMoveResponse();
    if (response.getMovePawn1() != null && !response.getMovePawn1().isEmpty()) {
      testMoveResponse.addTilesItem(response.getMovePawn1().getLast());
    }
    if (response.getMovePawn2() != null && !response.getMovePawn2().isEmpty()) {
      testMoveResponse.addTilesItem(response.getMovePawn2().getLast());
    }
    return new ResponseEntity<>(testMoveResponse, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<MoveResponse> makeMove(String sessionId,
      String playerId,
      MoveRequest moveRequest) {

    if (!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)) {
      System.out.println("there was no session found with id: " + sessionId);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    GameState gameState = gameSession.getGameState();

    if (!Objects.equals(playerId, gameState.getPlayerIdTurn())) {
      System.out.println("It was not the player's turn with id: " + playerId + ", it was: "+gameState.getPlayerIdTurn());
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    MoveResponse response = new MoveResponse();
    //todo: uncomment?
//    response.setPawn1(moveRequest.getPawn1Id());
//    response.setPawn2(moveRequest.getPawn2Id());

    // change the gamestate
    Card card = gameState.getCard(moveRequest.getCardId(), moveRequest.getPlayerId());
    Pawn pawn1 = gameState.getPawn(moveRequest.getPawn1Id());
    Pawn pawn2 = gameState.getPawn(moveRequest.getPawn2Id());
    SelectionValidation validation = PawnAndCardSelectionValidation.validate(pawn1, pawn2, card);

    if (!validation.isValid()) {
      System.out.println("card validation failed");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    moveRequest.setTempMessageType(MAKE_MOVE);
    switch (validation.getMoveType()) {
      case MOVE -> gameState.processOnMove(moveRequest, response);
      case SPLIT -> gameState.processOnSplit(moveRequest, response);
      case SWITCH -> gameState.processOnSwitch(moveRequest, response);
      case ON_BOARD -> gameState.processOnBoard(moveRequest, response);
    }

    // if you want to make a move for real && it is possible to do so
    // todo: move to gamestate itself
    if (Objects.equals(response.getResult(), CAN_MAKE_MOVE)) {

      ArrayList<MoveResponse> moves = new ArrayList<>();
      moves.add(response);
      if (moves.size() > 1) {
        moves.removeFirst();
      }
//      gameSession.saveMoves(moves);
    }

    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
