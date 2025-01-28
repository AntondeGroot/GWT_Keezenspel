package ADG.services;

import ADG.Games.Keezen.*;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

import java.util.Objects;

@SuppressWarnings("serial")
@WebServlet("/app/move")
public class MovingServiceImpl extends RemoteServiceServlet implements MovingService {

    @Override
    public MoveResponse makeMove(MoveMessage message) throws IllegalArgumentException {
        // Verify that the input is valid.
        if (message.getMoveType() == null) {
            throw new IllegalArgumentException(
                    "The selected move was invalid");
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

        return response;
    }


}