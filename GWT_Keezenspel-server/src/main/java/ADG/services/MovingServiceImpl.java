package ADG.services;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.Move.MessageType;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Move.MoveResult;
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
    public MoveResponse makeMove(MoveMessage message){
        // Verify that the input is valid.
        // the movetype can be null when the player only selected a pawn
        // return an empty response, since nothing will be done and no error occured.
        if (message.getMoveType() == null) {
            return new MoveResponse();
        }

        if(!Objects.equals(message.getPlayerId(), GameState.getPlayerIdTurn())){
            throw new IllegalArgumentException("It was not your turn to make a move");
        }

        MoveResponse response = new MoveResponse();
        response.setPawnId1(message.getPawnId1());
        response.setPawnId2(message.getPawnId2());

        // change the gamestate
        switch (message.getMoveType()) {
            case MOVE : GameState.processOnMove(message, response);
                break;
            case ONBOARD : GameState.processOnBoard(message, response);
                break;
            case SWITCH: GameState.processOnSwitch(message,response);
                break;
            case FORFEIT: GameState.processOnForfeit(message);
                break;
            case SPLIT: GameState.processOnSplit(message, response);
                break;
            default:
                break;
        }

        if(response.getResult().equals(MoveResult.CAN_MAKE_MOVE) && response.getMessageType().equals(
            MessageType.MAKE_MOVE)){
            saveTime = Instant.now();
            moves.add(response);
            if(moves.size() > 1){
                moves.removeFirst();
            }
        }

        return response;
    }

    @Override
    public MoveResponse getMove() {
        // check if time in seconds has passed
        // don't show animation when too much time has passed, for example when refreshing browser
        Instant currentTime = Instant.now();
        if(saveTime == null){
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