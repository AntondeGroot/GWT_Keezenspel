package gwtks.services;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import gwtks.*;
import jakarta.servlet.annotation.WebServlet;

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

        MoveResponse response = new MoveResponse();
        response.setNextPlayerId(0);
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
            default:
                break;
        }

        return response;
    }


}