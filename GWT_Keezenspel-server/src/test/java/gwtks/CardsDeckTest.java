package gwtks;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardsDeckTest {
    @AfterEach
    void tearDown() {
        CardsDeck.reset();
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
        CardsDeck.nextRound();
        CardsDeck.dealCards();

        Assert.assertEquals(4,CardsDeck.getCardsForPlayer(0).size());
        CardsDeck.forfeitCardsForPlayer(0);
        CardsDeck.nextRound();
        CardsDeck.dealCards();

        Assert.assertEquals(4,CardsDeck.getCardsForPlayer(0).size());
        CardsDeck.forfeitCardsForPlayer(0);
        CardsDeck.nextRound();
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
            int currentValue = cards.get(i).getCard();
            int nextValue = cards.get(i + 1).getCard();
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
}