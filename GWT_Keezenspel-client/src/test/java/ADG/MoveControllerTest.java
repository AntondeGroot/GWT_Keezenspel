package ADG;

import ADG.Games.Keezen.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class MoveControllerTest {
    private MoveMessage moveMessage = new MoveMessage();
    private MoveResponse moveResponse = new MoveResponse();

    @BeforeEach
    void setUp() {
        moveMessage = new MoveMessage();
        moveResponse = new MoveResponse();
    }

    @AfterEach
    void tearDown() {
        moveMessage = null;
        moveResponse = null;

    }

    private static Pawn createPawnAndPlaceOnBoard(String playerId, TileId currentTileId){
        // for creating pawns for different players
        PawnId pawnId1 = new PawnId(playerId,0);
        Pawn pawn1 = new Pawn(pawnId1, new TileId(playerId, -1));
        pawn1.setCurrentTileId(currentTileId);
        LinkedList<TileId> moves = new LinkedList<>();
        moves.add(currentTileId);
        Board.movePawn(pawn1, moves,false);
        return pawn1;
    }

    private ArrayList<Player> createPlayers(int nr){
        ArrayList<Player> players = new ArrayList<Player>();
        for (int i = 0; i < nr; i++) {
            players.add(new Player(String.valueOf(i),String.valueOf(i)));
        }
        return players;
    }

}