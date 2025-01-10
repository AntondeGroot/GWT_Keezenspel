package ADG;

import ADG.Games.Keezen.Board;
import ADG.Games.Keezen.Player;
import ADG.Games.Keezen.TileMapping;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    @Test
    void createBoardForTwoPlayers() {
        Board board = new Board();
        board.createBoard(createPlayers(2),300);
        List<TileMapping> mappings =  Board.getTiles();
        assertEquals(24*2, mappings.size());
    }

    @Test
    void createBoardForEightPlayers() {
        Board board = new Board();
        board.createBoard(createPlayers(8),300);
        List<TileMapping> mappings =  Board.getTiles();
        assertEquals(24*8, mappings.size());
    }

    private ArrayList<Player> createPlayers(int nr){
        ArrayList<Player> players = new ArrayList<Player>();
        for (int i = 0; i < nr; i++) {
            players.add(new Player(String.valueOf(i), String.valueOf(i)));
        }
        return players;
    }
}