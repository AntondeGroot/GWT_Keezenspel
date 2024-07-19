package gwtks;

import java.util.*;

public class CardsDeck {
    private static int roundNr = 0;
    private static ArrayDeque<Card> cardsDeque = new ArrayDeque<>();
    private static List<Player> players = new ArrayList<>();
    private static int nrPlayers = 0;

    public static void setNrPlayers(int nr_Players) {
        nrPlayers = nr_Players;
    }

    public static List<Card> getCardsForPlayer(int playerId) {
        return new ArrayList<>(players.get(playerId).getHand());
    }

    public static void forfeitCardsForPlayer(int playerId) {
        players.get(playerId).getHand().clear();
    }

    public static void shuffle(){
        List<Card> cards = new ArrayList<>();
        players = new ArrayList<>();

        // create cards
        for (int suit_i = 0; suit_i < nrPlayers; suit_i++) {
            for (int card_j = 0; card_j < 13; card_j++) {
                cards.add(new Card(suit_i % nrPlayers, card_j));
            }
        }

        // shuffle the cards
        Collections.shuffle(cards);
        cardsDeque = new ArrayDeque<>(cards);
    }

    public static void dealCards(){
        int nrCards = (roundNr == 0) ? 5 : 4;

        for (int i = 0; i < nrPlayers; i++) {
            players.add(new Player(i));
        }

        for (int i = 0; i < nrCards; i++) {
            for (int j = 0; j < nrPlayers; j++) {
                Player player = players.get(j);
                player.setCard(cardsDeque.pop());
            }
        }
    }

    public static void nextRound(){
        roundNr = (roundNr + 1) % 3;
    }

    public static boolean playerHasCard(int playerId, Card card ){
        Player player = players.get(playerId);
        return player.hasCard(card);
    }

    public static void reset(){
        roundNr = 0;
        cardsDeque.clear();
        players.clear();
        nrPlayers = 0;
    }
}
