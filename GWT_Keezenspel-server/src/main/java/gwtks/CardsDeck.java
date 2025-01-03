package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.*;
import java.util.stream.Collectors;

import static gwtks.GameState.forfeitPlayer;

public class CardsDeck implements IsSerializable {
    private static int roundNr = 0;
    private static ArrayDeque<Card> cardsDeque = new ArrayDeque<>();
    private static ArrayList<PlayerHand> players = new ArrayList<>();
    private static int nrPlayers = 0;
    private static int playerIdStartingRound = 0;
    private static ArrayList<Integer> activePlayers = new ArrayList<>();

    public static void setNrPlayers(int nr_Players) {
        nrPlayers = nr_Players;
        playerIdStartingRound = nr_Players-1;
    }

    public static ArrayList<Integer> getNrOfCardsForAllPlayers(){
        return (ArrayList<Integer>) players.stream()
                .map(player -> player.getHand().size())
                .collect(Collectors.toList());
    }

    public static ArrayList<Card> getCardsForPlayer(int playerId) {
        if(playerId > players.size()-1) {return new ArrayList<>();}

        return new ArrayList<>(players.get(playerId).getHand());
    }

    public static void forfeitCardsForPlayer(int playerId) {
        players.get(playerId).getHand().clear();
    }

    public static void shuffle(){
        ArrayList<Card> cards = new ArrayList<>();
        players = new ArrayList<>();
        activePlayers = GameState.getActivePlayers();
        // create cards
        for (int suit_i = 0; suit_i < activePlayers.size(); suit_i++) {
            for (int card_j = 0; card_j < 13; card_j++) {
                cards.add(new Card(suit_i % 4, card_j));
            }
        }

        // shuffle the cards
        Collections.shuffle(cards);
        cardsDeque = new ArrayDeque<>(cards);
    }

    public static void playerPlaysCard(int playerId, Card card) {
        if(card != null) {
            players.get(playerId).getHand().remove(card);
            if(players.get(playerId).getHand().isEmpty()){
                forfeitPlayer(playerId);
            }
        }
    }

    public static void giveCardToPlayerForTesting(int playerId, Card card){
        players.get(playerId).getHand().remove(0);
        setPlayerCard(playerId, card);
    }

    public static void setPlayerCard(int playerId, Card card){
        players.get(playerId).setCard(card);
    }

    public static void dealCards(){
        int nrCards = (roundNr == 0) ? 5 : 4;

        for (int player_i = 0; player_i < GameState.getNrPlayers(); player_i++) {
            players.add(new PlayerHand(player_i));
        }

        for (int player_i = 0; player_i < GameState.getNrPlayers(); player_i++) {
            for (int j = 0; j < nrCards; j++) {
                if(activePlayers.contains(player_i)){
                    setPlayerCard(player_i, cardsDeque.pop());
                }
            }
        }

        roundNr = (roundNr + 1) % 3;
        nextPlayerId();
    }

    public static boolean playerHasCard(int playerId, Card card ){
        PlayerHand player = players.get(playerId);
        return player.hasCard(card);
    }

    private static void nextPlayerId(){
        playerIdStartingRound = (playerIdStartingRound + 1) % activePlayers.size();
    }

    public static int getPlayerIdStartingRound() {
        return playerIdStartingRound;
    }

    public static void reset(){
        roundNr = 0;
        cardsDeque.clear();
        players.clear();
        nrPlayers = 0;
        playerIdStartingRound = 0;
    }
}
