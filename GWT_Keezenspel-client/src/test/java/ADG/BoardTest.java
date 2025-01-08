package ADG;

import ADG.Pawn;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    @Test
    void createBoardForTwoPlayers() {
        Board board = new Board();
        board.createBoard(2,300);
        List<TileMapping> mappings =  Board.getTiles();
        assertEquals(24*2, mappings.size());
    }

    @Test
    void createBoardForEightPlayers() {
        Board board = new Board();
        board.createBoard(8,300);
        List<TileMapping> mappings =  Board.getTiles();
        assertEquals(24*8, mappings.size());
    }

    @Test
    void createDistinctPawnsFor2Players() {
        Board board = new Board();
        board.createBoard(2,300);
        List<ADG.Pawn> pawns =  Board.getPawns();
        Set<ADG.Pawn> pawnSet = new HashSet<>(pawns);
        assertEquals(2*4, pawns.size());
        assertEquals(2*4, pawnSet.size());
    }

    @Test
    void createDistinctPawnsFor8Players() {
        Board board = new Board();
        board.createBoard(8,300);
        List<ADG.Pawn> pawns =  Board.getPawns();
        Set<Pawn> pawnSet = new HashSet<>(pawns);
        assertEquals(8*4, pawns.size());
        assertEquals(8*4, pawnSet.size());
    }
}