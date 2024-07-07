package gwtks;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@WebServlet("/app/move")
public class MovingServiceImpl extends RemoteServiceServlet implements MovingService {

    @Override
    public MoveResponse makeMove(MoveMessage input) throws IllegalArgumentException {
        // Verify that the input is valid.
        if (!MoveVerifier.isValidMove(input)) {
            throw new IllegalArgumentException(
                    "The selected move was invalid");
        }

        // change the gamestate
        GameState gameState = new GameState();
        List<Pawn> pawns = gameState.getPawns();
        // update gamestate

        // send instructions for clients to update
        MoveResponse response = new MoveResponse();
        response.setNextPlayerId(0);
        response.setPawnId1(input.getPawnId1());

        List<TileId> move = new ArrayList<>();
        int next = input.getTileId().getTileNr() + input.getStepsPawn1();
        int playerIdTile = input.getTileId().getPlayerId();


        if (next > 15) {
            next = next % 15;
            playerIdTile++;
            playerIdTile = playerIdTile % 8;
        }

        move.add(new TileId(playerIdTile,next));
        response.setMovePawn1(move);

        return response;
    }
}