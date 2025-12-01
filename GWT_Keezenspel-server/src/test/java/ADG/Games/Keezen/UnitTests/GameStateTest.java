package ADG.Games.Keezen.UnitTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.Player;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameStateTest {

  private GameState gameState;

  @BeforeEach
  void setUp() {
    GameSession engine = new GameSession("GameStateTest");
    gameState = engine.getGameState();

    gameState.stop();
    gameState.tearDown();
  }

  @AfterEach
  void tearDown() {
    gameState.tearDown();
  }

  @Test
  void createDistinctPawnsFor2Players() {
    gameState.addPlayer(new Player("0", "0"));
    gameState.addPlayer(new Player("1", "1"));
    gameState.start();

    List<Pawn> pawns = gameState.getPawns();
    Set<Pawn> pawnSet = new HashSet<>(pawns);
    assertEquals(2 * 4, pawns.size());
    assertEquals(2 * 4, pawnSet.size());
  }

  @Test
  void createDistinctPawnsFor8Players() {
    for (int i = 0; i < 8; i++) {
      gameState.addPlayer(new Player(String.valueOf(i), String.valueOf(i)));
    }
    gameState.start();
    List<Pawn> pawns = gameState.getPawns();
    Set<Pawn> pawnSet = new HashSet<>(pawns);
    assertEquals(8 * 4, pawns.size());
    assertEquals(8 * 4, pawnSet.size());
  }

  private ArrayList<Player> createPlayers(int nr) {
    ArrayList<Player> players = new ArrayList<>();
    for (int i = 0; i < nr; i++) {
      players.add(new Player(String.valueOf(i), String.valueOf(i)));
    }
    return players;
  }
}
