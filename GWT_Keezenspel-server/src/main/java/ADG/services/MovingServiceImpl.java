package ADG.services;

import static ADG.Games.Keezen.Move.MessageType.MAKE_MOVE;
import static ADG.Games.Keezen.Move.MoveResult.CAN_MAKE_MOVE;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Move.MovingService;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("serial")
@WebServlet("/app/move")
public class MovingServiceImpl extends RemoteServiceServlet implements MovingService {

    ArrayList<MoveResponse> moves = new ArrayList<>();
    Instant saveTime;

    @Override
    public MoveResponse makeMove(String sessionID, MoveMessage message){
        // Verify that the input is valid.
        // the movetype can be null when the player only selected a pawn
        // return an empty response, since nothing will be done and no error occured.
//        if (message.getMoveType() == null) {
//            return new MoveResponse();
//        }
//
//        GameSession session = GameRegistry.getGame(sessionID);
//        GameState gameState = session.getGameState();
//        if(!Objects.equals(message.getPlayerId(), gameState.getPlayerIdTurn())){
//            return new MoveResponse();
//        }
//
//        MoveResponse response = new MoveResponse();
//        response.setPawnId1(message.getPawnId1());
//        response.setPawnId2(message.getPawnId2());
//
//        // change the gamestate
//        switch (message.getMoveType()) {
//            case MOVE : gameState.processOnMove(message, response);
//                break;
//            case ONBOARD : gameState.processOnBoard(message, response);
//                break;
//            case SWITCH: gameState.processOnSwitch(message,response);
//                break;
//            case FORFEIT: gameState.processOnForfeit(message);
//                break;
//            case SPLIT: gameState.processOnSplit(message, response);
//                break;
//            default:
//                break;
//        }
//
//        if(Objects.equals(response.getResult(), CAN_MAKE_MOVE)
//            && Objects.equals(response.getMessageType(), MAKE_MOVE)){
//            saveTime = Instant.now();
//            moves.add(response);
//            if(moves.size() > 1){
//                moves.removeFirst();
//            }
//        }
//
//        return response;
        return new MoveResponse();
    }

    @Override
    public MoveResponse getMove(String sessionID) {
        // check if time in seconds has passed
        // don't show animation when too much time has passed, for example when refreshing browser
        Instant currentTime = Instant.now();
        if(saveTime == null){// todo: make this sessionID dependent
            saveTime = currentTime;
        }

        if (Duration.between(saveTime, currentTime).getSeconds() > 10) {
            return new MoveResponse();
        }

        if(moves.isEmpty()){
            return new MoveResponse();
        }
        return moves.getLast();
    }
}