package gwtks;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static gwtks.GameStateUtil.sendForfeitMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TurnBasedTest {
    MoveMessage moveMessage = new MoveMessage();
    MoveResponse moveResponse = new MoveResponse();
    ArrayList<Integer> activePlayers = new ArrayList<>();
    ArrayList<Integer> nrCardsPerPlayer = new ArrayList<>();

    @BeforeEach
    void setUp() {
        GameState gameState = new GameState(3);
        moveMessage = new MoveMessage();
        moveResponse = new MoveResponse();
        activePlayers.add(0);
        activePlayers.add(1);
        activePlayers.add(2);
    }

    @AfterEach
    void tearDown() {
        GameState.tearDown();
        moveMessage = null;
        moveResponse = null;
        activePlayers.clear();
        CardsDeck.reset();
        nrCardsPerPlayer = new ArrayList<>();
    }

    @Test
    void player1Forfeits_OtherPlayersStillActive() {
        // WHEN
        sendForfeitMessage(0);

        // THEN
        activePlayers.remove((Integer) 0);
        Assert.assertEquals(activePlayers, GameState.getActivePlayers());
        Assert.assertEquals(2, GameState.getActivePlayers().size());
    }
    @Test
    void player1Forfeits_Player1NoCards_OthersStillHaveCards() {
        // WHEN
        sendForfeitMessage(0);

        // THEN
        ArrayList<Integer> nrCardsPerPlayer = new ArrayList<>();
        nrCardsPerPlayer.add(0);
        nrCardsPerPlayer.add(5);
        nrCardsPerPlayer.add(5);
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void player1Forfeits_Player2IsNowPlaying() {
        // WHEN
        sendForfeitMessage(0);

        // THEN
        activePlayers.remove((Integer) 0);
        assertEquals(1, GameState.getPlayerIdTurn());
        assertEquals(activePlayers, GameState.getActivePlayers());
    }
    @Test
    void player1PlaysCard_Player2IsNowPlaying() {

        fail("This test is not yet implemented");
    }
    @Test
    void player1PlaysCard_Player1Has1CardLessThanPlayer2() {

        fail("This test is not yet implemented");
    }
    @Test
    void player1Forfeits_Player1GetsSkippedInNextTurn() {
        fail("This test is not yet implemented");
    }
    @Test
    void allPlayersForfeit_AllPlayersActiveAgain() {
        // WHEN
        sendForfeitMessage(0);
        sendForfeitMessage(1);
        sendForfeitMessage(2);

        // THEN
        Assert.assertEquals(activePlayers, GameState.getActivePlayers());
        Assert.assertEquals(3, GameState.getActivePlayers().size());
    }
    @Test
    void allPlayersForfeit_AllPlayersHave4Cards() {
        // WHEN
        sendForfeitMessage(0);
        sendForfeitMessage(1);
        sendForfeitMessage(2);

        // THEN
        nrCardsPerPlayer = nrCardsPerPlayer(new int[]{4,4,4});
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void allPlayersForfeit2Rounds_AllPlayersHave4Cards() {
        // WHEN
        for (int i = 0; i < 2; i++) {
            sendForfeitMessage(0);
            sendForfeitMessage(1);
            sendForfeitMessage(2);
        }

        // THEN
        nrCardsPerPlayer = nrCardsPerPlayer(new int[]{4, 4, 4});
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void allPlayersForfeit3Rounds_AllPlayersHave5Cards() {
        // WHEN
        for (int i = 0; i < 3; i++) {
            sendForfeitMessage(0);
            sendForfeitMessage(1);
            sendForfeitMessage(2);
        }

        // THEN
        nrCardsPerPlayer = nrCardsPerPlayer(new int[]{5,5,5});
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void allPlayersForfeit_DecksGetReshuffled() {
        fail("This test is not yet implemented");
    }
    @Test
    void player1PlaysKingThenPlayer2PlaysKing() {
        fail("This test is not yet implemented");
    }

    private ArrayList<Integer> nrCardsPerPlayer(int[] nrCards){
        ArrayList<Integer> nrCarsperPlayer = new ArrayList<>();
        for (int i = 0; i < nrCards.length; i++) {
            nrCarsperPlayer.add(nrCards[i]);
        }
        return nrCarsperPlayer;
    }
}
