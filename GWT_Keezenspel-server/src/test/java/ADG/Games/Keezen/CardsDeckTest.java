package ADG.Games.Keezen;

import ADG.Games.Keezen.CardsDeck;
import ADG.Games.Keezen.Card;
import ADG.Games.Keezen.logic.WinnerLogic;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ADG.Games.Keezen.GameStateUtil.*;
import static ADG.Games.Keezen.GameStateUtil.*;
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
        // GIVEN WHEN
        createGame_With_NPlayers(5);

        int totalCards = 0;
        for (int i = 0; i < 5; i++) {
            totalCards += CardsDeck.getCardsForPlayer(String.valueOf(i)).size();
        }
        // THEN, all the players have 5 cards in their hand
        assertEquals(5*5, totalCards);
    }

    @Test
    void onePlayer_ShuffleDeck_TheyHave5Cards() {
        // GIVEN WHEN
        createGame_With_NPlayers(1);

        // THEN
        List<Card> cards =  CardsDeck.getCardsForPlayer("0");
        assertFalse(isSortedNumerically(cards));
        assertEquals(5, cards.size());
    }

    @Test
    void dealCards_5_4_4_CardsPerRound() {
        // GIVEN WHEN
        createGame_With_NPlayers(1);

        // THEN
        Assert.assertEquals(5, CardsDeck.getCardsForPlayer("0").size());
        CardsDeck.forfeitCardsForPlayer("0");
        CardsDeck.dealCards();

        Assert.assertEquals(4, CardsDeck.getCardsForPlayer("0").size());
        CardsDeck.forfeitCardsForPlayer("0");
        CardsDeck.dealCards();

        Assert.assertEquals(4, CardsDeck.getCardsForPlayer("0").size());
        CardsDeck.forfeitCardsForPlayer("0");
        CardsDeck.shuffle();
        CardsDeck.dealCards();

        Assert.assertEquals(5, CardsDeck.getCardsForPlayer("0").size());
    }

    @Test
    void twoPlayersDoNotHaveTheSameCard() {
        // GIVEN WHEN
        createGame_With_NPlayers(2);

        // WHEN
        List<Card> cards1 = CardsDeck.getCardsForPlayer("0");
        List<Card> cards2 = CardsDeck.getCardsForPlayer("1");

        // THEN
        assertTrue(doNotContainTheSameCards(cards1, cards2));
    }

    @Test
    void playerHasCard(){
        // GIVEN WHEN
        createGame_With_NPlayers(1);

        // WHEN
        List<Card> cards = CardsDeck.getCardsForPlayer("0");

        // THEN
        assertEquals(5, cards.size());
        for (Card card : cards) {
            assertTrue(CardsDeck.playerHasCard("0", card));
        }
    }

    @Test
    void playerForfeitsCards(){
        // GIVEN
        createGame_With_NPlayers(2);

        // WHEN
        CardsDeck.forfeitCardsForPlayer("0");

        // THEN
        HashMap<String, Integer> nrs = new HashMap<>();
        nrs.put("0",0);
        nrs.put("1",5);

        assertEquals(nrs, CardsDeck.getNrOfCardsForAllPlayers());
        assertTrue(CardsDeck.getCardsForPlayer("0").isEmpty());
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
        assertTrue(CardsDeck.getCardsForPlayer("1").contains(card));
        assertTrue(CardsDeck.playerHasCard("1",card));
    }

    @Test
    void playerHasCard2(){
        // GIVEN WHEN
        createGame_With_NPlayers(3);

        // WHEN
        place4PawnsOnFinish("1");
        WinnerLogic.checkForWinners(new ArrayList<>());
        place4PawnsOnFinish("2");
        WinnerLogic.checkForWinners(new ArrayList<>());
        playRemainingCards("0");

        CardsDeck.shuffle();
        CardsDeck.dealCards();

        // THEN
        int totalCards = CardsDeck.getNrOfCardsForAllPlayers()
                .values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        assertEquals(4,totalCards);
    }

    @Test
    void PlayedCardsDoNotResetDuringFirstRound() {
        createGame_With_NPlayers(3);
        GameState.setPlayerIdTurn("0");
        for (int i = 0; i < 4; i++) {
            sendValidMoveMessage("0");
            sendValidMoveMessage("1");
            sendValidMoveMessage("2");
        }
        assertEquals(3*4, CardsDeck.getPlayedCards().size());

        GameState.tearDown();
        CardsDeck.reset();
    }

    @Test
    void PlayedCardsDoNotResetAfterFirstRound() {
        createGame_With_NPlayers(3);
        GameState.setPlayerIdTurn("0");
        for (int i = 0; i < 5; i++) {
            sendValidMoveMessage("0");
            sendValidMoveMessage("1");
            sendValidMoveMessage("2");
        }
        assertEquals(3*5, CardsDeck.getPlayedCards().size());

        GameState.tearDown();
        CardsDeck.reset();
    }
    @Test
    void PlayedCardsDoNotResetAfterSecondRound() {
        createGame_With_NPlayers(3);
        GameState.setPlayerIdTurn("0");
        for (int i = 0; i < 9; i++) {
            sendValidMoveMessage("0");
            sendValidMoveMessage("1");
            sendValidMoveMessage("2");
        }
        assertEquals(3*5+3*4, CardsDeck.getPlayedCards().size());

        GameState.tearDown();
        CardsDeck.reset();
    }
    @Test
    void PlayedCardsResetAfterThirdRound() {
        createGame_With_NPlayers(3);
        GameState.setPlayerIdTurn("0");
        for (int i = 0; i < 13; i++) {
            sendValidMoveMessage("0");
            sendValidMoveMessage("1");
            sendValidMoveMessage("2");
        }
        assertEquals(0, CardsDeck.getPlayedCards().size());

        GameState.tearDown();
        CardsDeck.reset();
    }
}