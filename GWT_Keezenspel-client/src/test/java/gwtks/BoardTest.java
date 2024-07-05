package gwtks;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void createBoardForTwoPlayers() {
        Board board = new Board();
        board.createBoard(2,300);
        List<TileMapping> mappings =  Board.getMappings();
        assertEquals(24*2, mappings.size());
    }

    @Test
    void createBoardForEightPlayers() {
        Board board = new Board();
        board.createBoard(8,300);
        List<TileMapping> mappings =  Board.getMappings();
        assertEquals(24*8, mappings.size());
    }
}