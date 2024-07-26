package gwtks;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static gwtks.GameStateUtil.givePlayerCard;
import static gwtks.GameStateUtil.sendValidMoveMessage;
import static org.junit.jupiter.api.Assertions.*;

class CardsDeckTest {
    @BeforeEach
    void setup(){
        CardsDeck.reset();
    }

    @AfterEach
    void tearDown() {
        CardsDeck.reset();
        GameState.tearDown();
    }

    @Test
    void fivePlayers_ShuffleDeck_EachHas5Cards() {
        // GIVEN
        CardsDeck.setNrPlayers(5);

        // WHEN
        CardsDeck.shuffle();
        CardsDeck.dealCards();
        int totalCards = 0;
        for (int i = 0; i < 5; i++) {
            totalCards += CardsDeck.getCardsForPlayer(i).size();
        }
        // THEN, all the players have 5 cards in their hand
        assertEquals(5*5, totalCards);
    }

    @Test
    void onePlayer_ShuffleDeck_TheyHave5Cards() {
        // GIVEN
        CardsDeck.setNrPlayers(1);

        // WHEN
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // THEN
        List<Card> cards =  CardsDeck.getCardsForPlayer(0);
        assertFalse(isSortedNumerically(cards));
        assertEquals(5, cards.size());
    }

    @Test
    void dealCards_5_4_4_CardsPerRound() {
        // GIVEN
        CardsDeck.setNrPlayers(1);

        // WHEN
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // THEN
        Assert.assertEquals(5,CardsDeck.getCardsForPlayer(0).size());
        CardsDeck.forfeitCardsForPlayer(0);
        CardsDeck.dealCards();

        Assert.assertEquals(4,CardsDeck.getCardsForPlayer(0).size());
        CardsDeck.forfeitCardsForPlayer(0);
        CardsDeck.dealCards();

        Assert.assertEquals(4,CardsDeck.getCardsForPlayer(0).size());
        CardsDeck.forfeitCardsForPlayer(0);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        Assert.assertEquals(5,CardsDeck.getCardsForPlayer(0).size());
    }

    @Test
    void twoPlayersDoNotHaveTheSameCard() {
        // GIVEN
        CardsDeck.setNrPlayers(2);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN
        List<Card> cards1 = CardsDeck.getCardsForPlayer(0);
        List<Card> cards2 = CardsDeck.getCardsForPlayer(1);

        // THEN
        assertTrue(doNotContainTheSameCards(cards1, cards2));
    }

    @Test
    void playerHasCard(){
        // GIVEN
        CardsDeck.setNrPlayers(1);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN
        List<Card> cards = CardsDeck.getCardsForPlayer(0);

        // THEN
        assertEquals(5, cards.size());
        for (Card card : cards) {
            assertTrue(CardsDeck.playerHasCard(0, card));
        }
    }

    @Test
    void playerForfeitsCards(){
        // GIVEN
        CardsDeck.setNrPlayers(2);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN
        CardsDeck.forfeitCardsForPlayer(0);

        // THEN
        ArrayList<Integer> nrs = new ArrayList<>();
        nrs.add(0);
        nrs.add(5);

        assertEquals(nrs, CardsDeck.getNrOfCardsForAllPlayers());
        assertTrue(CardsDeck.getCardsForPlayer(0).isEmpty());
    }

    public static boolean isSortedNumerically(List<Card> cards) {
        for (int i = 0; i < cards.size() - 1; i++) {
            int currentValue = cards.get(i).getCardValue();
            int nextValue = cards.get(i + 1).getCardValue();
            if (currentValue > nextValue) {
                return false;
            }
        }
        return true;
    }

    public static boolean doNotContainTheSameCards(List<Card> cards1, List<Card> cards2) {
        for (Card card : cards1) {
            if(cards2.contains(card)) {
                return false;
            }
        }
        return true;
    }

    @Test
    void getCardsForPlayer() {
        // GIVEN
        CardsDeck.setNrPlayers(8);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN
        Card card = givePlayerCard(1,-4);

        // THEN
        assertTrue(CardsDeck.getCardsForPlayer(1).contains(card));
        assertTrue(CardsDeck.playerHasCard(1,card));
    }
    @Test
    void oneRound_player0LastPlayer_nextPlayer1(){
        // GIVEN
        new GameState(3);
        CardsDeck.setNrPlayers(3);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN
        sendValidMoveMessage(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        playRemainingCards(0);

        // THEN
        assertEquals(1, CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void oneRound_player1LastPlayer_nextPlayer1(){
        // GIVEN
        new GameState(3);
        CardsDeck.setNrPlayers(3);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN
        sendValidMoveMessage(0);
        sendValidMoveMessage(1);
        GameState.forfeitPlayer(2);
        GameState.forfeitPlayer(0);
        playRemainingCards(1);
        // THEN
        assertEquals(1, CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void oneRound_player2LastPlayer_nextPlayer1_Forfeit(){
        // GIVEN
        new GameState(3);
        CardsDeck.setNrPlayers(3);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);

        // THEN
        assertEquals(1, CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void oneRound_player2LastPlayer_nextPlayer1_byPlaying(){
        // GIVEN
        new GameState(3);
        CardsDeck.setNrPlayers(3);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        playRemainingCards(2);
        // THEN
        assertEquals(1, CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void twoRounds_player0LastPlayer_nextPlayer2(){
        // GIVEN
        new GameState(3);
        CardsDeck.setNrPlayers(3);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN round 1
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        // WHEN round 2
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        playRemainingCards(0);

        // THEN
        assertEquals(2, CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void twoRounds_player1LastPlayer_nextPlayer2(){
        // GIVEN
        new GameState(3);
        CardsDeck.setNrPlayers(3);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN round 1
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        // WHEN round 2
        sendValidMoveMessage(1);
        GameState.forfeitPlayer(2);
        GameState.forfeitPlayer(0);
        playRemainingCards(1);

        // THEN
        assertEquals(2, CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void twoRounds_player2LastPlayer_nextPlayer2(){
        // GIVEN
        new GameState(3);
        CardsDeck.setNrPlayers(3);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN round 1
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        // WHEN round 2
        GameState.forfeitPlayer(1);
        sendValidMoveMessage(2);
        GameState.forfeitPlayer(0);
        playRemainingCards(2);
        // THEN
        assertEquals(2, CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void threeRounds_player0LastPlayer_nextPlayer0(){
        // GIVEN
        new GameState(3);
        CardsDeck.setNrPlayers(3);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN round 1
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        // WHEN round 2
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        GameState.forfeitPlayer(0);
        // WHEN round 3
        GameState.forfeitPlayer(2);
        sendValidMoveMessage(0);
        GameState.forfeitPlayer(1);
        playRemainingCards(0);

        // THEN
        assertEquals(0, CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void threeRounds_player1LastPlayer_nextPlayer0(){
        // GIVEN
        new GameState(3);
        CardsDeck.setNrPlayers(3);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN round 1
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        // WHEN round 2
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        GameState.forfeitPlayer(0);
        // WHEN round 3
        GameState.forfeitPlayer(2);
        GameState.forfeitPlayer(0);
        playRemainingCards(1);

        // THEN
        assertEquals(0, CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void threeRounds_player2LastPlayer_nextPlayer0(){
        /// GIVEN
        new GameState(3);
        CardsDeck.setNrPlayers(3);
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // WHEN round 1
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        // WHEN round 2
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        GameState.forfeitPlayer(0);
        // WHEN round 3
        sendValidMoveMessage(2);
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        playRemainingCards(2);

        // THEN
        assertEquals(0, CardsDeck.getPlayerIdStartingRound());
    }

    private void playRemainingCards(int playerId){
        int nrCards = CardsDeck.getCardsForPlayer(playerId).size();
        for (int i = 0; i < nrCards; i++) {
            sendValidMoveMessage(playerId);
        }
    }
}