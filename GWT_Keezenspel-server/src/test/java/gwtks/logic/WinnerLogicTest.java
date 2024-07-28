package gwtks.logic;

import gwtks.GameState;
import gwtks.GameStateUtil;
import gwtks.PawnId;
import gwtks.TileId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import static gwtks.GameStateUtil.*;

import static gwtks.logic.WinnerLogic.checkForWinners;
import static org.junit.jupiter.api.Assertions.*;

class WinnerLogicTest {
    ArrayList<Integer> winners = new ArrayList<>();
    @BeforeEach
    void setup(){
        GameState gameState = new GameState(3);
    }

    @AfterEach
    void tearDown(){
        GameState.tearDown();
        winners.clear();
    }

    @Test
    void testOneWinner(){
        // GIVEN
        place4PawnsOnFinish(0);

        // WHEN
        checkForWinners(winners);

        // THEN
        assertTrue(winners.contains(0));
    }
    @Test
    void testPlayer2Wins_ThenPlayer1Wins(){
        // GIVEN
        place4PawnsOnFinish(2);
        checkForWinners(winners);

        // WHEN
        place4PawnsOnFinish(1);
        checkForWinners(winners);

        // THEN
        assertEquals(intsToList(new int[]{2,1}), winners);
    }
    @Test
    void testPlayer2Wins_Player0Wins_ThenPlayer1Wins(){
        // GIVEN
        place4PawnsOnFinish(2);
        checkForWinners(winners);
        place4PawnsOnFinish(0);
        checkForWinners(winners);

        // WHEN
        place4PawnsOnFinish(1);
        checkForWinners(winners);

        // THEN
        assertEquals(intsToList(new int[]{2,0,1}), winners);
    }

    private void place4PawnsOnFinish(int playerId){
        createPawnAndPlaceOnBoard(new PawnId(playerId,0) , new TileId(playerId,16));
        createPawnAndPlaceOnBoard(new PawnId(playerId,1) , new TileId(playerId,17));
        createPawnAndPlaceOnBoard(new PawnId(playerId,2) , new TileId(playerId,18));
        createPawnAndPlaceOnBoard(new PawnId(playerId,3) , new TileId(playerId,19));
    }

}