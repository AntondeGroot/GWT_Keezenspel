package ADG.services;

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
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MovesApiDelegateImpl implements MovesApiDelegate {

  @Override
  public ResponseEntity<TestMoveResponse> checkMove(
      String sessionId, String playerId, MoveRequest moveRequest) {
    moveRequest.setTempMessageType(CHECK_MOVE);
    ResponseEntity<MoveResponse> result = dispatchMove(sessionId, playerId, moveRequest);
    if (!result.getStatusCode().is2xxSuccessful()) {
      return ResponseEntity.status(result.getStatusCode()).build();
    }
    MoveResponse move = result.getBody();
    TestMoveResponse testMoveResponse = new TestMoveResponse();
    if (move.getMovePawn1() != null && !move.getMovePawn1().isEmpty()) {
      testMoveResponse.addTilesItem(move.getMovePawn1().getLast());
    }
    if (move.getMovePawn2() != null && !move.getMovePawn2().isEmpty()) {
      testMoveResponse.addTilesItem(move.getMovePawn2().getLast());
    }
    return ResponseEntity.ok(testMoveResponse);
  }

  @Override
  public ResponseEntity<MoveResponse> makeMove(
      String sessionId, String playerId, MoveRequest moveRequest) {
    moveRequest.setTempMessageType(MAKE_MOVE);
    return dispatchMove(sessionId, playerId, moveRequest);
  }

  private ResponseEntity<MoveResponse> dispatchMove(
      String sessionId, String playerId, MoveRequest moveRequest) {
    if (!(GameRegistry.getGame(sessionId) instanceof GameSession gameSession)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    GameState gameState = gameSession.getGameState();

    if (!Objects.equals(playerId, gameState.getPlayerIdTurn())) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    Card card = gameState.getCard(moveRequest.getCardId(), moveRequest.getPlayerId());
    Pawn pawn1 = gameState.getPawn(moveRequest.getPawn1Id());
    Pawn pawn2 = gameState.getPawn(moveRequest.getPawn2Id());
    SelectionValidation validation = PawnAndCardSelectionValidation.validate(pawn1, pawn2, card);

    if (!validation.isValid()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    MoveResponse response = new MoveResponse();
    switch (validation.getMoveType()) {
      case MOVE -> gameState.processOnMove(moveRequest, response);
      case SPLIT -> gameState.processOnSplit(moveRequest, response);
      case SWITCH -> gameState.processOnSwitch(moveRequest, response);
      case ON_BOARD -> gameState.processOnBoard(moveRequest, response);
    }

    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}