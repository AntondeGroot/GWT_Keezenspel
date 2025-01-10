package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.*;
import java.util.stream.Collectors;

import static ADG.Games.Keezen.GameState.forfeitPlayer;

public class CardsDeck implements IsSerializable {
    private static int roundNr = 0;
    private static ArrayDeque<Card> cardsDeque = new ArrayDeque<>();
//    private static ArrayList<PlayerHand> players = new ArrayList<>();
    private static HashMap<String, PlayerHand> playerHands = new HashMap<>();
    private static ArrayList<Player> players2 = new ArrayList<>();
    private static int nrPlayers = 0;
    private static int playerIdStartingRound = 0;
    private static ArrayList<Integer> activePlayers = new ArrayList<>();

    public static void addPlayers(ArrayList<Player> players) {
        for (Player p : players) {
            playerHands.put(p.getUUID(), new PlayerHand());
        }
        players2.addAll(players);
        playerIdStartingRound = 0;
    }

    public static ArrayList<Integer> getNrOfCardsForAllPlayers(){
        // todo: implement getNrOfCards to show the hands of other players
        return new ArrayList<>();
//        return (ArrayList<Integer>) players.stream()
//                .map(player -> player.getHand().size())
//                .collect(Collectors.toList());
    }

    public static ArrayList<Card> getCardsForPlayer(String playerUUID) {
        return playerHands.get(playerUUID).getHand();
    }

    public static void forfeitCardsForPlayer(String playerId) {
        playerHands.get(playerId).dropCards();
//        players.get(playerId).getHand().clear(); todo: method is different, does it work correctly?
    }

    public static void shuffle(){
        ArrayList<Card> cards = new ArrayList<>();
//        players = new ArrayList<>(); //todo: does this need to be reset?
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

    public static void playerPlaysCard(String playerId, Card card) {
        if(card != null) {
            playerHands.get(playerId).getHand().remove(card);
            if(playerHands.get(playerId).getHand().isEmpty()){
                forfeitPlayer(playerId);
            }
        }
    }

    public static void giveCardToPlayerForTesting(String playerId, Card card){
//        players.get(playerId).getHand().remove(0); // todo; was this ever necessary?
        setPlayerCard(playerId, card);
    }

    public static void setPlayerCard(String playerId, Card card){
        playerHands.get(playerId).setCard(card);
    }

    public static void dealCards(){
        int nrCards = (roundNr == 0) ? 5 : 4;

        //todo: is this reset necessary?
        for(String playerId: playerHands.keySet() ){
            playerHands.get(playerId).dropCards();
        }

        for(Player player: GameState.getPlayers()){
            for (int j = 0; j < nrCards; j++) {
                if(player.isActive()){
                    setPlayerCard(player.getUUID(), cardsDeque.pop());
                }
            }
        }

        roundNr = (roundNr + 1) % 3;
        nextPlayerId();
    }

    public static boolean playerHasCard(String playerId, Card card ){
        return playerHands.get(playerId).hasCard(card);
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
        playerHands.clear();
        nrPlayers = 0;
        playerIdStartingRound = 0;
    }
}
