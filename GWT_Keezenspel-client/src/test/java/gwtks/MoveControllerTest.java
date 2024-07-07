package gwtks;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveControllerTest {

    @Test
    void movePawn2Tiles() {
        // GIVEN
        TileId endTile = new TileId(0,2);
        PawnId pawnId = new PawnId(0,0);

        Board board = new Board();
        board.createBoard(2,300);

        List<TileId> moves = new ArrayList<>();
        moves.add(new TileId(0,2));
        MoveResponse moveResponse = new MoveResponse();
        moveResponse.setPawnId1(pawnId);
        moveResponse.setMovePawn1(moves);

        // WHEN
        MoveController.movePawn(null, moveResponse);

        // THEN
        Pawn pawn = Board.getPawn(pawnId);
        assertEquals(pawn.getCurrentTileId().getTileNr(), endTile.getTileNr());
        assertEquals(Board.getPawns().size(),4*2);
    }
}