package gwtks;

import gwtks.logic.WinnerLogic;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static gwtks.GameStateUtil.*;
import static gwtks.GameStateUtil.place4PawnsOnFinish;
import static org.junit.Assert.*;

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
    void player1PlaysCard_4_5_5_CardsInTheGame() {
        // WHEN
        sendValidMoveMessage(0);

        // THEN
        nrCardsPerPlayer = intsToList(new int[]{4,5,5});
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void player1Forfeits_Player1GetsSkippedInNextTurn() {
        // GIVEN
        sendForfeitMessage(0);
        sendValidMoveMessage(1);

        // WHEN
        sendValidMoveMessage(2);

        // THEN
        assertEquals(1, GameState.getPlayerIdTurn());
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
        nrCardsPerPlayer = intsToList(new int[]{4,4,4});
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
        nrCardsPerPlayer = intsToList(new int[]{4, 4, 4});
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
        nrCardsPerPlayer = intsToList(new int[]{5,5,5});
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void player2Forfeits_WhenPlayer1HasPlayed_Player3WillPlayNext(){
        // GIVEN
        sendValidMoveMessage(0);
        sendForfeitMessage(1);
        sendValidMoveMessage(2);

        // WHEN
        sendValidMoveMessage(0);

        // THEN
        assertTrue(CardsDeck.getCardsForPlayer(1).isEmpty());
        assertEquals(2,GameState.getPlayerIdTurn());
    }
    @Test
    void allPlayersExcept1Forfeit_RemainingPlayerKeepsPlayingUntilLastCard(){
        // GIVEN
        sendForfeitMessage(0);
        sendForfeitMessage(1);

        for (int i = 0; i < 5; i++) {
            // WHEN
            sendValidMoveMessage(2);

            // THEN
            if(i < 4){
                assertEquals("playerId turn NOT last round",2, GameState.getPlayerIdTurn());
                assertEquals("cards remaining for player 2"+i,4-i, CardsDeck.getCardsForPlayer(2).size());
            }
        }
    }
    @Test
    void playerCanOnlyMoveHisOwnPawn(){
        new GameState(3);

        // WHEN
        sendValidMoveMessage(0);

        // send a valid move for the wrong player
        Pawn pawn = new Pawn(new PawnId(0,0),new TileId(0,6));

        // fake a valid card
        Card card = new Card(0,5);

        // replace a card from the players hand with this card
        CardsDeck.giveCardToPlayerForTesting(1, card);

        // send move message
        MoveMessage moveMessage = new MoveMessage();
        moveMessage.setPlayerId(1);
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MoveType.MOVE);
        moveMessage.setStepsPawn1(card.getCardValue()+1);
        moveMessage.setCard(card);
        moveMessage.setMessageType(MessageType.MAKE_MOVE);

        // process
        MoveResponse moveResponse = new MoveResponse();
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        assertNull(moveResponse.getMovePawn1());
        assertEquals(MoveResult.PLAYER_DOES_NOT_HAVE_CARD, moveResponse.getResult());
    }
    @Test
    void playersPlayAllTheirCards_ExceptLastPlayer_OnlyLastPlayerIsActive(){
        // GIVEN
        for (int i = 0; i < 4; i++) {
            sendValidMoveMessage(0);
            sendValidMoveMessage(1);
            sendValidMoveMessage(2);
        }

        // WHEN
        sendValidMoveMessage(0);
        sendValidMoveMessage(1);

        // THEN
        assertEquals(intsToList(new int[]{2}), GameState.getActivePlayers());
    }
    @Test
    void players0and2Finished_OnlyPlayer1PlayingAndForfeiting_DoesNotSwitchToWinner(){
        // GIVEN
        ArrayList<Integer> winners = new ArrayList<>();
        place4PawnsOnFinish(0);
        WinnerLogic.checkForWinners(winners);
        place4PawnsOnFinish(2);
        WinnerLogic.checkForWinners(winners);

        // WHEN
        sendForfeitMessage(0);
        playRemainingCards(1);
        sendForfeitMessage(2);

        // THEN
        assertEquals(1,GameState.getPlayerIdTurn());
        sendForfeitMessage(1);
        assertEquals(1,GameState.getPlayerIdTurn());
        sendForfeitMessage(1);
        assertEquals(1,GameState.getPlayerIdTurn());
        sendForfeitMessage(1);
        assertEquals(1,GameState.getPlayerIdTurn());
    }
}
