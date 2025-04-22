package ADG;

import ADG.Games.Keezen.board.Board;
import ADG.Games.Keezen.util.Cookie;
import ADG.Games.Keezen.Player.Player;
import ADG.Games.Keezen.TileMapping;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    @Test
    void createBoardForTwoPlayers() {
        try (MockedStatic<Cookie> mockedCookie = Mockito.mockStatic(Cookie.class)) {
            mockedCookie.when(Cookie::getPlayerId).thenReturn("test-uuid");

            Board board = new Board();
            board.createBoard(createPlayers(2),300);
            List<TileMapping> mappings =  Board.getTiles();
            assertEquals(24*2, mappings.size());
            // test code that calls Cookie.getPlayerId()
        }catch (Exception ignored){
            fail();
        }

    }

    @Test
    void createBoardForEightPlayers() {
        try (MockedStatic<Cookie> mockedCookie = Mockito.mockStatic(Cookie.class)) {
            mockedCookie.when(Cookie::getPlayerId).thenReturn("test-uuid");

            Board board = new Board();
            board.createBoard(createPlayers(8), 300);
            List<TileMapping> mappings = Board.getTiles();
            assertEquals(24 * 8, mappings.size());
        }catch (Exception ignored){
            fail();
        }
    }

    private ArrayList<Player> createPlayers(int nr){
        ArrayList<Player> players = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            players.add(new Player(String.valueOf(i), String.valueOf(i)));
        }
        return players;
    }
}