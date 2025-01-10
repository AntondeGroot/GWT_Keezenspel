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
    @Test
    void movePawn2Tiles() {
        // todo: the client no longer creates the pawns, the server creates them. Maybe this test should be changed, or moved/split to client/server
        fail("Not yet implemented, maybe not needed");
//        // GIVEN
//        TileId endTile = new TileId("0",2);
//        PawnId pawnId = new PawnId("0",0);
//
//        Board board = new Board();
//        board.createBoard(createPlayers(2),300);
//
//        LinkedList<TileId> moves = new LinkedList<>();
//        moves.add(new TileId("0",2));
//        MoveResponse moveResponse = new MoveResponse();
//        moveResponse.setPawnId1(pawnId);
//        moveResponse.setMovePawn1(moves);
//
//        // WHEN
//        MoveController.movePawn(moveResponse);
//
//        // THEN
//        Pawn pawn = Board.getPawn(pawnId);
//        assertEquals(pawn.getCurrentTileId().getTileNr(), endTile.getTileNr());
//        assertEquals(Board.getPawns().size(),4*2);
    }
    @Test
    void movingPawnKillsOtherPlayer() {
        // todo: the client no longer creates the pawns, the server creates them. Maybe this test should be changed, or moved/split to client/server
        fail("Not yet implemented, maybe not needed");
//        Board board = new Board();
//        board.createBoard(createPlayers(2),300);
//        LinkedList<TileId> moves1 = new LinkedList<>();
//        LinkedList<TileId> moves2 = new LinkedList<>();
//
//        Pawn pawn1 = createPawnAndPlaceOnBoard("1", new TileId("0",4));
//        Pawn pawn2 = createPawnAndPlaceOnBoard("0", new TileId("0",5));
//
//        moves1.add(pawn1.getCurrentTileId());
//        moves1.add(pawn2.getCurrentTileId());
//        moves2.add(pawn2.getCurrentTileId());
//        moves2.add(pawn2.getNestTileId());
//
//        MoveResponse moveResponse = new MoveResponse();
//        moveResponse.setPawnId1(pawn1.getPawnId());
//        moveResponse.setPawnId2(pawn2.getPawnId());
//        moveResponse.setMovePawn1(moves1);
//        moveResponse.setMovePawn2(moves2);
//
//        // WHEN
//        MoveController.movePawn(moveResponse);
//
//        // THEN pawn 2 should be killed
//        Pawn pawn = Board.getPawn(pawn2.getPawnId());
//        assertEquals(pawn.getNestTileId(), pawn.getCurrentTileId());
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