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
//        if (!MoveVerifier.isValidName(input)) {
//            // If the input is not valid, throw an IllegalArgumentException back to
//            // the client.
//            throw new IllegalArgumentException(
//                    "Name must be at least 4 characters long");
//        }

        // change the gamestate
        GameState gameState = new GameState();
        List<Pawn> pawns = gameState.getPawns();
//        for (Pawn pawn : pawns) {
//            if(pawn.getPlayerId() == 1 || pawn.getCurrentPositionTileNr() == -1){
//                pawn.setCurrentPositionTileNr(0);
//            }
//        }
        // send instructions for clients to update
        MoveResponse response = new MoveResponse();
        response.setNextPlayerId(0);
//        response.setPawnId1(new PawnId(0,0));
        // fake data
        List<TileId> move = new ArrayList<>();
        move.add(new TileId(0,0));
        move.add(new TileId(0,1));
        move.add(new TileId(0,2));
        response.setMovePawn1(move);

        return response;
    }
}