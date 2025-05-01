package ADG.Games.Keezen.UnitTests;

import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class WinnerLogicTest {
    ArrayList<String> winners = new ArrayList<>();

    private GameState gameState;
    private CardsDeckInterface cardsDeck;

    @BeforeEach
    void setup(){
        GameSession session = new GameSession();
        gameState = session.getGameState();
        cardsDeck = session.getCardsDeck();
        createGame_With_NPlayers(gameState, 3);
    }

    @AfterEach
    void tearDown(){
        gameState.tearDown();
        winners.clear();
    }

    @Test
    void testOneWinner(){
        // GIVEN
        place4PawnsOnFinish(gameState, "0");

        // WHEN
        gameState.checkForWinners(winners);

        // THEN
        assertTrue(winners.contains("0"));
    }
    @Test
    void testPlayer2Wins_ThenPlayer1Wins(){
        // GIVEN
        place4PawnsOnFinish(gameState, "2");
        gameState.checkForWinners(winners);

        // WHEN
        place4PawnsOnFinish(gameState, "1");
        gameState.checkForWinners(winners);

        // THEN
        assertEquals(stringsToList(new String[]{"2","1"}), winners);
    }
    @Test
    void testPlayer2Wins_Player0Wins_ThenPlayer1Wins(){
        // GIVEN
        place4PawnsOnFinish(gameState, "2");
        gameState.checkForWinners(winners);
        place4PawnsOnFinish(gameState, "0");
        gameState.checkForWinners(winners);

        // WHEN
        place4PawnsOnFinish(gameState, "1");
        gameState.checkForWinners(winners);

        // THEN
        assertEquals(stringsToList(new String[]{"2","0","1"}), winners);
    }
}