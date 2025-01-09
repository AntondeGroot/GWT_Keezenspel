package ADG.Games.Keezen;

import ADG.Games.Keezen.Card;
import ADG.Games.Keezen.logic.WinnerLogic;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static ADG.Games.Keezen.GameStateUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class CardsDeckTest {
    @BeforeEach
    void setup(){
        ADG.Games.Keezen.CardsDeck.reset();
    }

    @AfterEach
    void tearDown() {
        ADG.Games.Keezen.CardsDeck.reset();
        GameState.tearDown();
    }

    @Test
    void fivePlayers_ShuffleDeck_EachHas5Cards() {
        // GIVEN WHEN
        createGame_With_NPlayers(5);

        int totalCards = 0;
        for (int i = 0; i < 5; i++) {
            totalCards += ADG.Games.Keezen.CardsDeck.getCardsForPlayer(i).size();
        }
        // THEN, all the players have 5 cards in their hand
        assertEquals(5*5, totalCards);
    }

    @Test
    void onePlayer_ShuffleDeck_TheyHave5Cards() {
        // GIVEN WHEN
        createGame_With_NPlayers(1);

        // THEN
        List<Card> cards =  ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0);
        assertFalse(isSortedNumerically(cards));
        assertEquals(5, cards.size());
    }

    @Test
    void dealCards_5_4_4_CardsPerRound() {
        // GIVEN WHEN
        createGame_With_NPlayers(1);

        // THEN
        Assert.assertEquals(5, ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0).size());
        ADG.Games.Keezen.CardsDeck.forfeitCardsForPlayer(0);
        ADG.Games.Keezen.CardsDeck.dealCards();

        Assert.assertEquals(4, ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0).size());
        ADG.Games.Keezen.CardsDeck.forfeitCardsForPlayer(0);
        ADG.Games.Keezen.CardsDeck.dealCards();

        Assert.assertEquals(4, ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0).size());
        ADG.Games.Keezen.CardsDeck.forfeitCardsForPlayer(0);
        ADG.Games.Keezen.CardsDeck.shuffle();
        ADG.Games.Keezen.CardsDeck.dealCards();

        Assert.assertEquals(5, ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0).size());
    }

    @Test
    void twoPlayersDoNotHaveTheSameCard() {
        // GIVEN WHEN
        createGame_With_NPlayers(2);

        // WHEN
        List<Card> cards1 = ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0);
        List<Card> cards2 = ADG.Games.Keezen.CardsDeck.getCardsForPlayer(1);

        // THEN
        assertTrue(doNotContainTheSameCards(cards1, cards2));
    }

    @Test
    void playerHasCard(){
        // GIVEN WHEN
        createGame_With_NPlayers(1);

        // WHEN
        List<Card> cards = ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0);

        // THEN
        assertEquals(5, cards.size());
        for (Card card : cards) {
            assertTrue(ADG.Games.Keezen.CardsDeck.playerHasCard(0, card));
        }
    }

    @Test
    void playerForfeitsCards(){
        // GIVEN
        createGame_With_NPlayers(2);

        // WHEN
        ADG.Games.Keezen.CardsDeck.forfeitCardsForPlayer(0);

        // THEN
        ArrayList<Integer> nrs = new ArrayList<>();
        nrs.add(0);
        nrs.add(5);

        assertEquals(nrs, ADG.Games.Keezen.CardsDeck.getNrOfCardsForAllPlayers());
        assertTrue(ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0).isEmpty());
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
        createGame_With_NPlayers(8);

        // WHEN
        Card card = givePlayerCard(1,-4);

        // THEN
        assertTrue(ADG.Games.Keezen.CardsDeck.getCardsForPlayer(1).contains(card));
        assertTrue(ADG.Games.Keezen.CardsDeck.playerHasCard(1,card));
    }
    @Test
    void oneRound_player0LastPlayer_nextPlayer1(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN
        sendValidMoveMessage(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        playRemainingCards(0);

        // THEN
        assertEquals(1, ADG.Games.Keezen.CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void oneRound_player1LastPlayer_nextPlayer1(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN
        sendValidMoveMessage(0);
        sendValidMoveMessage(1);
        GameState.forfeitPlayer(2);
        GameState.forfeitPlayer(0);
        playRemainingCards(1);

        // THEN
        assertEquals(1, ADG.Games.Keezen.CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void oneRound_player2LastPlayer_nextPlayer1_Forfeit(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);

        // THEN
        assertEquals(1, ADG.Games.Keezen.CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void oneRound_player2LastPlayer_nextPlayer1_byPlaying(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        playRemainingCards(2);
        // THEN
        assertEquals(1, ADG.Games.Keezen.CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void twoRounds_player0LastPlayer_nextPlayer2(){
        // GIVEN
        createGame_With_NPlayers(3);

        // WHEN round 1
        GameState.forfeitPlayer(0);
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        // WHEN round 2
        GameState.forfeitPlayer(1);
        GameState.forfeitPlayer(2);
        playRemainingCards(0);

        // THEN
        assertEquals(2, ADG.Games.Keezen.CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void twoRounds_player1LastPlayer_nextPlayer2(){
        // GIVEN
        createGame_With_NPlayers(3);

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
        assertEquals(2, ADG.Games.Keezen.CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void twoRounds_player2LastPlayer_nextPlayer2(){
        // GIVEN
        createGame_With_NPlayers(3);

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
        assertEquals(2, ADG.Games.Keezen.CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void threeRounds_player0LastPlayer_nextPlayer0(){
        // GIVEN
        createGame_With_NPlayers(3);

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
        assertEquals(0, ADG.Games.Keezen.CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void threeRounds_player1LastPlayer_nextPlayer0(){
        // GIVEN
        createGame_With_NPlayers(3);

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
        assertEquals(0, ADG.Games.Keezen.CardsDeck.getPlayerIdStartingRound());
    }
    @Test
    void threeRounds_player2LastPlayer_nextPlayer0(){
        /// GIVEN
        createGame_With_NPlayers(3);

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
        assertEquals(0, ADG.Games.Keezen.CardsDeck.getPlayerIdStartingRound());
    }

    @Test
    void playerHasCard2(){
        // GIVEN WHEN
        createGame_With_NPlayers(3);

        // WHEN
        place4PawnsOnFinish(1);
        WinnerLogic.checkForWinners(new ArrayList<>());
        place4PawnsOnFinish(2);
        WinnerLogic.checkForWinners(new ArrayList<>());
        playRemainingCards(0);

        ADG.Games.Keezen.CardsDeck.shuffle();
        ADG.Games.Keezen.CardsDeck.dealCards();

        // THEN
        int totalCards = CardsDeck.getNrOfCardsForAllPlayers().stream().mapToInt(Integer::intValue).sum();
        assertEquals(4,totalCards);
    }
}