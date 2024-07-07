package gwtks;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@WebServlet("/app/move")
public class MovingServiceImpl extends RemoteServiceServlet implements MovingService {

    @Override
    public MoveResponse makeMove(MoveMessage moveMessage) throws IllegalArgumentException {
        // Verify that the input is valid.
        if (!MoveVerifier.isValidMove(moveMessage)) {
            throw new IllegalArgumentException(
                    "The selected move was invalid");
        }
        // get the data
        PawnId pawnId1 = moveMessage.getPawnId1();
        PawnId pawnId2 = moveMessage.getPawnId2();
        MoveType moveType = moveMessage.getMoveType();
        int stepsPawn1 = moveMessage.getStepsPawn1();
        int stepsPawn2 = moveMessage.getStepsPawn2();
        //


        // change the gamestate
        GameState gameState = new GameState();
        List<Pawn> pawns = gameState.getPawns();
        // update gamestate

        // send instructions for clients to update
        MoveResponse response = new MoveResponse();
        response.setNextPlayerId(0);
        response.setPawnId1(moveMessage.getPawnId1());
        response.setPawnId2(moveMessage.getPawnId2());

        int next = 0;
        List<TileId> move = new ArrayList<>();

        if(moveType == MoveType.MOVE){
            if(moveMessage.getTileId() != null){
                next = moveMessage.getTileId().getTileNr() + moveMessage.getStepsPawn1();
            }
            int playerIdOfTile = moveMessage.getTileId().getPlayerId();


            if (next > 15) {
                next = next % 15;
                playerIdOfTile++;
                playerIdOfTile = playerIdOfTile % 8;
            }

            move.add(new TileId(playerIdOfTile,next));
            response.setMovePawn1(move);
        }

        if(moveType == MoveType.ONBOARD){
            // check if pawn in on the Nest
            // check if start tile is empty
            // if start is occupied by own pawn, invalid
            // if start is occupied by other player, kill that pawn
            move.add(new TileId(moveMessage.getPawnId1().getPlayerId(),0));
            response.setMovePawn1(move);
        }

        return response;
    }
}