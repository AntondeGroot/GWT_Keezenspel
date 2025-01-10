package ADG.Games.Keezen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import static ADG.Games.Keezen.GameStateUtil.*;

import static ADG.Games.Keezen.logic.WinnerLogic.checkForWinners;
import static org.junit.jupiter.api.Assertions.*;

class WinnerLogicTest {
    ArrayList<String> winners = new ArrayList<>();
    @BeforeEach
    void setup(){
        createGame_With_NPlayers(3);
    }

    @AfterEach
    void tearDown(){
        GameState.tearDown();
        winners.clear();
    }

    @Test
    void testOneWinner(){
        // GIVEN
        place4PawnsOnFinish("0");

        // WHEN
        checkForWinners(winners);

        // THEN
        assertTrue(winners.contains("0"));
    }
    @Test
    void testPlayer2Wins_ThenPlayer1Wins(){
        // GIVEN
        place4PawnsOnFinish("2");
        checkForWinners(winners);

        // WHEN
        place4PawnsOnFinish("1");
        checkForWinners(winners);

        // THEN
        assertEquals(stringsToList(new String[]{"2","1"}), winners);
    }
    @Test
    void testPlayer2Wins_Player0Wins_ThenPlayer1Wins(){
        // GIVEN
        place4PawnsOnFinish("2");
        checkForWinners(winners);
        place4PawnsOnFinish("0");
        checkForWinners(winners);

        // WHEN
        place4PawnsOnFinish("1");
        checkForWinners(winners);

        // THEN
        assertEquals(stringsToList(new String[]{"2","0","1"}), winners);
    }
}