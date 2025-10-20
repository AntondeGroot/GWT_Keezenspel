package ADG.Games.Keezen.UnitTests;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.createGame_With_NPlayers;
import static ADG.Games.Keezen.UnitTests.GameStateUtil.createMoveRequest;
import static ADG.Games.Keezen.UnitTests.GameStateUtil.givePlayerCard;
import static ADG.Games.Keezen.UnitTests.GameStateUtil.placePawnOnNest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.Player;
import com.adg.openapi.model.PositionKey;
import java.util.LinkedList;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PreviousAndNextPlayerTest {

  MoveRequest moveMessage = new MoveRequest();
  MoveResponse moveResponse = new MoveResponse();

  private GameSession engine;
  private GameState gameState;
  private CardsDeckInterface cardsDeck;

  @BeforeEach
  void setUp() {
    engine = new GameSession();
    gameState = engine.getGameState();
    cardsDeck = engine.getCardsDeck();
  }

  @Test
  void withNonSequentialUUIDS_gameStateStillRefersToPreviousPlayerCorrectly() {
    String player1 = UUID.randomUUID().toString();
    String player2 = UUID.randomUUID().toString();
    String player3 = UUID.randomUUID().toString();

    gameState.stop();
    gameState.addPlayer(new Player(player1, "player 1"));
    gameState.addPlayer(new Player(player2, "player 2"));
    gameState.addPlayer(new Player(player3, "player 3"));
    gameState.start();
    moveMessage = new MoveRequest();
    moveResponse = new MoveResponse();

    assertEquals(player3, gameState.previousPlayerId(player1));
    assertEquals(player2, gameState.previousPlayerId(player3));
    assertEquals(player1, gameState.previousPlayerId(player2));
  }


}
