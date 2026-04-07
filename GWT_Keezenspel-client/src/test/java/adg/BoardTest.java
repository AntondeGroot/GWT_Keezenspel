package adg;

import static org.junit.jupiter.api.Assertions.*;

import adg.keezen.dto.PlayerClient;
import adg.keezen.TileMapping;
import adg.keezen.board.Board;
import adg.keezen.util.Cookie;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class BoardTest {
  @Test
  void createBoardForTwoPlayers() {
    try (MockedStatic<Cookie> mockedCookie = Mockito.mockStatic(Cookie.class)) {
      mockedCookie.when(Cookie::getPlayerId).thenReturn("test-uuid");

      Board board = new Board();
      board.createBoard(createPlayers(2), 300);
      List<TileMapping> mappings = Board.getTiles();
      assertEquals(24 * 2, mappings.size());
      // test code that calls Cookie.getPlayerId()
    } catch (Exception ignored) {
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
    } catch (Exception ignored) {
      fail();
    }
  }

  private ArrayList<PlayerClient> createPlayers(int nr) {
    ArrayList<PlayerClient> players = new ArrayList<>();
    for (int i = 0; i < nr; i++) {
      players.add(new PlayerClient(String.valueOf(i), String.valueOf(i)));
    }
    return players;
  }
}
