package ADG.Games.Keezen;


import ADG.Games.Keezen.logic.WinnerLogic;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static ADG.Games.Keezen.GameStateUtil.*;
import static ADG.Games.Keezen.MoveResult.CAN_MAKE_MOVE;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TurnBasedTest {
    MoveMessage moveMessage = new MoveMessage();
    MoveResponse moveResponse = new MoveResponse();
    ArrayList<String> activePlayers = new ArrayList<>();
    HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();

    @BeforeEach
    void setUp() {
        CardsDeck.reset();
        createGame_With_NPlayers(3);
        moveMessage = new MoveMessage();
        moveResponse = new MoveResponse();
        activePlayers.add("0");
        activePlayers.add("1");
        activePlayers.add("2");
    }

    @AfterEach
    void tearDown() {
        GameState.tearDown();
        moveMessage = null;
        moveResponse = null;
        activePlayers.clear();
        CardsDeck.reset();
        nrCardsPerPlayer = new HashMap<>();
        GameState.stop();
    }

    @Test
    void player1Forfeits_OtherPlayersStillActive() {
        // WHEN
        sendForfeitMessage("0");

        // THEN
        activePlayers.remove( "0");
        Assertions.assertEquals(activePlayers, GameState.getActivePlayers());
        Assertions.assertEquals(2, GameState.getActivePlayers().size());
    }
    @Test
    void player1Forfeits_Player1NoCards_OthersStillHaveCards() {
        // WHEN
        sendForfeitMessage("0");

        // THEN
        HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();
        nrCardsPerPlayer.put("0",0);
        nrCardsPerPlayer.put("1",5);
        nrCardsPerPlayer.put("2",5);
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void player1Forfeits_Player2IsNowPlaying() {
        // WHEN
        sendForfeitMessage("0");

        // THEN
        activePlayers.remove("0");
        assertEquals("1", GameState.getPlayerIdTurn());
        assertEquals(activePlayers, GameState.getActivePlayers());
    }
    @Test
    void player1PlaysCard_4_5_5_CardsInTheGame() {
        // WHEN
        sendValidMoveMessage("0");

        // THEN
        HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();
        nrCardsPerPlayer.put("0",4);
        nrCardsPerPlayer.put("1",5);
        nrCardsPerPlayer.put("2",5);
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void player1Forfeits_Player1GetsSkippedInNextTurn() {
        // GIVEN
        sendForfeitMessage("0");
        sendValidMoveMessage("1");

        // WHEN
        sendValidMoveMessage("2");

        // THEN
        assertEquals("1", GameState.getPlayerIdTurn());
    }
    @Test
    void allPlayersForfeit_AllPlayersActiveAgain() {
        // WHEN
        sendForfeitMessage("0");
        sendForfeitMessage("1");
        sendForfeitMessage("2");

        // THEN
        Assert.assertEquals(activePlayers, GameState.getActivePlayers());
        Assert.assertEquals(3, GameState.getActivePlayers().size());
    }
    @Test
    void allPlayersForfeit_AllPlayersHave4Cards() {
        // WHEN
        sendForfeitMessage("0");
        sendForfeitMessage("1");
        sendForfeitMessage("2");

        // THEN
        HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();
        nrCardsPerPlayer.put("0",4);
        nrCardsPerPlayer.put("1",4);
        nrCardsPerPlayer.put("2",4);
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void allPlayersForfeit2Rounds_AllPlayersHave4Cards() {
        // WHEN
        for (int i = 0; i < 2; i++) {
            sendForfeitMessage("0");
            sendForfeitMessage("1");
            sendForfeitMessage("2");
        }

        // THEN
        HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();
        nrCardsPerPlayer.put("0",4);
        nrCardsPerPlayer.put("1",4);
        nrCardsPerPlayer.put("2",4);
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void allPlayersForfeit3Rounds_AllPlayersHave5Cards() {
        // WHEN
        for (int i = 0; i < 3; i++) {
            sendForfeitMessage("0");
            sendForfeitMessage("1");
            sendForfeitMessage("2");
        }

        // THEN
        HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();
        nrCardsPerPlayer.put("0",5);
        nrCardsPerPlayer.put("1",5);
        nrCardsPerPlayer.put("2",5);
        assertEquals(nrCardsPerPlayer, CardsDeck.getNrOfCardsForAllPlayers());
    }
    @Test
    void player2Forfeits_WhenPlayer1HasPlayed_Player3WillPlayNext(){
        // GIVEN
        sendValidMoveMessage("0");
        sendForfeitMessage("1");
        sendValidMoveMessage("2");

        // WHEN
        sendValidMoveMessage("0");

        // THEN
        assertTrue(CardsDeck.getCardsForPlayer("1").isEmpty());
        assertEquals("2",GameState.getPlayerIdTurn());
    }
    @Test
    void allPlayersExcept1Forfeit_RemainingPlayerKeepsPlayingUntilLastCard(){
        // GIVEN
        sendForfeitMessage("0");
        sendForfeitMessage("1");

        for (int i = 0; i < 5; i++) {
            // WHEN
            sendValidMoveMessage("2");

            // THEN
            if(i < 4){
                Assert.assertEquals("playerId turn NOT last round","2", GameState.getPlayerIdTurn());
                Assert.assertEquals("cards remaining for player 2", 4-i, CardsDeck.getCardsForPlayer("2").size());
            }
        }
    }
    @Test
    void playerCanOnlyMoveHisOwnPawn(){

        // WHEN
        sendValidMoveMessage("0");

        // send a valid move for the wrong player
        Pawn pawn = new Pawn(new PawnId("0",0),new TileId("0",6));

        // fake a valid card
        Card card = new Card(0,5);

        // replace a card from the players hand with this card
        CardsDeck.giveCardToPlayerForTesting("1", card);

        // send move message
        MoveMessage moveMessage = new MoveMessage();
        moveMessage.setPlayerId("1");
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MoveType.MOVE);
        moveMessage.setStepsPawn1(card.getCardValue());
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
            sendValidMoveMessage("0");
            sendValidMoveMessage("1");
            sendValidMoveMessage("2");
        }

        // WHEN
        sendValidMoveMessage("0");
        sendValidMoveMessage("1");

        // THEN
        assertEquals(stringsToList(new String[]{"2"}), GameState.getActivePlayers());
    }
    @Test
    void players0and2Finished_OnlyPlayer1PlayingAndForfeiting_DoesNotSwitchToWinner(){
        // GIVEN
        ArrayList<String> winners = new ArrayList<>();
        place4PawnsOnFinish("0");
        WinnerLogic.checkForWinners(winners);
        place4PawnsOnFinish("2");
        WinnerLogic.checkForWinners(winners);

        // WHEN
        sendForfeitMessage("0");
        playRemainingCards("1");
        sendForfeitMessage("2");

        // THEN
        assertEquals("1",GameState.getPlayerIdTurn());
        sendForfeitMessage("1");
        assertEquals("1",GameState.getPlayerIdTurn());
        sendForfeitMessage("1");
        assertEquals("1",GameState.getPlayerIdTurn());
        sendForfeitMessage("1");
        assertEquals("1",GameState.getPlayerIdTurn());
    }
    @Test
    void players1Starts_WhenPlayer1PlaysLastCard_Player2ShouldPlay_Bugfix(){
        // GIVEN
        GameState.setPlayerIdTurn("1");
        for (int i = 0; i < 4; i++) {
            sendValidMoveMessage("1");
            sendValidMoveMessage("2");
            sendValidMoveMessage("0");
        }

        // WHEN
        sendValidMoveMessage("1");

        // THEN
        Assertions.assertEquals("2", GameState.getPlayerIdTurn());
    }

    @Test
    void players2Starts_WhenPlayer2PlaysLastCard_Player0ShouldPlay_Bugfix(){
        // GIVEN
        GameState.setPlayerIdTurn("2");
        for (int i = 0; i < 4; i++) {
            sendValidMoveMessage("2");
            sendValidMoveMessage("0");
            sendValidMoveMessage("1");
        }

        // WHEN
        sendValidMoveMessage("2");

        // THEN
        Assertions.assertEquals("0", GameState.getPlayerIdTurn());
    }

    @Test
    void players2Starts_WhenPlayer2PlaysLastCard_AndPlayer0Forfeits_Player1ShouldPlay(){
        // GIVEN
        GameState.setPlayerIdTurn("2");
        for (int i = 0; i < 4; i++) {
            sendValidMoveMessage("2");
            sendValidMoveMessage("0");
            sendValidMoveMessage("1");
        }

        // WHEN
        sendValidMoveMessage("2");
        sendForfeitMessage("0");

        // THEN
        Assertions.assertEquals("1", GameState.getPlayerIdTurn());
    }

    @Test
    void players0Starts_WhenPlayer0and2Forfeit_Player1PlaysLastCard_Player1ShouldPlay_bugfix(){
        // GIVEN
        GameState.setPlayerIdTurn("0");
        sendForfeitMessage("0");
        sendValidMoveMessage("1");
        sendForfeitMessage("2");

        for (int i = 0; i < 3; i++) {
            sendValidMoveMessage("1");
        }

        // WHEN send last card
        sendValidMoveMessage("1");

        // THEN
        Assertions.assertEquals("1", GameState.getPlayerIdTurn());
    }

    @Test
    void oneRound_player0LastPlayer_nextPlayer1(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN
        sendValidMoveMessage("0");
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        playRemainingCards("0");

        // THEN
        assertEquals("1", GameState.getPlayerIdTurn());
    }
    @Test
    void oneRound_player1LastPlayer_nextPlayer1(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN
        sendValidMoveMessage("0");
        sendValidMoveMessage("1");
        GameState.forfeitPlayer("2");
        GameState.forfeitPlayer("0");
        playRemainingCards("1");

        // THEN
        assertEquals("1", GameState.getPlayerIdTurn());
    }
    @Test
    void oneRound_player2LastPlayer_nextPlayer1_Forfeit(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN
        GameState.forfeitPlayer("0");
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");

        // THEN
        assertEquals("1", GameState.getPlayerIdTurn());
    }
    @Test
    void oneRound_player2LastPlayer_nextPlayer1_byPlaying(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN
        GameState.forfeitPlayer("0");
        GameState.forfeitPlayer("1");
        playRemainingCards("2");
        // THEN
        assertEquals("1", GameState.getPlayerIdTurn());
    }
    @Test
    void twoRounds_player0LastPlayer_nextPlayer2(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN round 1
        GameState.forfeitPlayer("0");
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        // WHEN round 2
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        playRemainingCards("0");

        // THEN
        assertEquals("2", GameState.getPlayerIdTurn());
    }
    @Test
    void twoRounds_player1LastPlayer_nextPlayer2(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN round 1
        GameState.forfeitPlayer("0");
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        // WHEN round 2
        sendValidMoveMessage("1");
        GameState.forfeitPlayer("2");
        GameState.forfeitPlayer("0");
        playRemainingCards("1");

        // THEN
        assertEquals("2", GameState.getPlayerIdTurn());
    }
    @Test
    void twoRounds_player2LastPlayer_nextPlayer2(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN round 1
        GameState.forfeitPlayer("0");
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        // WHEN round 2
        GameState.forfeitPlayer("1");
        sendValidMoveMessage("2");
        GameState.forfeitPlayer("0");
        playRemainingCards("2");
        // THEN
        assertEquals("2", GameState.getPlayerIdTurn());
    }
    @Test
    void threeRounds_player0LastPlayer_nextPlayer0(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN round 1
        GameState.forfeitPlayer("0");
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        // WHEN round 2
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        GameState.forfeitPlayer("0");
        // WHEN round 3
        GameState.forfeitPlayer("2");
        sendValidMoveMessage("0");
        GameState.forfeitPlayer("1");
        playRemainingCards("0");

        // THEN
        assertEquals("0", GameState.getPlayerIdTurn());
    }
    @Test
    void threeRounds_player1LastPlayer_nextPlayer0(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN round 1
        GameState.forfeitPlayer("0");
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        // WHEN round 2
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        GameState.forfeitPlayer("0");
        // WHEN round 3
        GameState.forfeitPlayer("2");
        GameState.forfeitPlayer("0");
        playRemainingCards("1");

        // THEN
        assertEquals("0", GameState.getPlayerIdTurn());
    }
    @Test
    void threeRounds_player2LastPlayer_nextPlayer0(){
        /// GIVEN
        createGame_With_NPlayers(3);

        // WHEN round 1
        GameState.forfeitPlayer("0");
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        // WHEN round 2
        GameState.forfeitPlayer("1");
        GameState.forfeitPlayer("2");
        GameState.forfeitPlayer("0");
        // WHEN round 3
        sendValidMoveMessage("2");
        GameState.forfeitPlayer("0");
        GameState.forfeitPlayer("1");
        playRemainingCards("2");

        // THEN
        assertEquals("0", GameState.getPlayerIdTurn());
    }

    @Test
    void test_whenSplitIsPlayed_nextPlayerIs() {
        /// GIVEN
        createGame_With_NPlayers(3);
        // send a valid move for the wrong player
        Pawn pawn1 = new Pawn(new PawnId("0",1),new TileId("0",6));
        Pawn pawn2 = new Pawn(new PawnId("0",2),new TileId("0",0));
        placePawnOnBoard(pawn1);
        placePawnOnBoard(pawn2);
        // fake a valid card
        Card card = new Card(0,7);
        // replace a card from the players hand with this card
        CardsDeck.giveCardToPlayerForTesting("0", card);

        createSplitMessage(moveMessage, pawn1, 3, pawn2, 4, card);
        // process
        MoveResponse moveResponse = new MoveResponse();
        GameState.processOnSplit(moveMessage, moveResponse);

        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals("1", GameState.getPlayerIdTurn());
    }

    @Test
    void ThreeRoundsArePlayed_NumberOfUniqueCards_39_bugfix(){
         // I discovered that not enough kings and aces were given

        // GIVEN, WHEN
        ArrayList<Card> cards = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            GameState.setPlayerIdTurn("0");
            cards.addAll(CardsDeck.getCardsForPlayer("0"));
            sendForfeitMessage("0");
            cards.addAll(CardsDeck.getCardsForPlayer("1"));
            sendForfeitMessage("1");
            cards.addAll(CardsDeck.getCardsForPlayer("2"));
            sendForfeitMessage("2");
        }

        // THEN
        HashSet<Card> cardSet = new HashSet<>(cards);
        assertEquals(13*3, cards.size());
        assertEquals(13*3, cardSet.size());
    }
}
