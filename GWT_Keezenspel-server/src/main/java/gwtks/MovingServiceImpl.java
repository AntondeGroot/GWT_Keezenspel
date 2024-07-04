package gwtks;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet("/app/move")
public class MovingServiceImpl extends RemoteServiceServlet implements MovingService {

    @Override
    public MoveResponse makeMove(MoveMessage input) throws IllegalArgumentException {
        // Verify that the input is valid.
//        if (!MoveVerifier.isValidName(input)) {
//            // If the input is not valid, throw an IllegalArgumentException back to
//            // the client.
//            throw new IllegalArgumentException(
//                    "Name must be at least 4 characters long");
//        }
//        GameState gameState = new GameState();
        MoveResponse response = new MoveResponse();
        response.setNextUserId(input.getUserId()+100);

        return response;
    }
}