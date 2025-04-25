package ADG.Games.Keezen;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Player.Player;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.*;

public class CardsDeck implements CardsDeckInterface, IsSerializable {
    private int roundNr;
    private ArrayDeque<Card> cardsDeque = new ArrayDeque<>();
    private final ArrayList<Card> playedCards = new ArrayList<>();
    private final HashMap<String, PlayerHand> playerHands = new HashMap<>();
    private ArrayList<String> activePlayers = new ArrayList<>();
    private GameState gameState;

    public CardsDeck() {}

    public void setGameState(GameState gameState){
        this.gameState = gameState;
    }

    public void addPlayers(ArrayList<Player> players) {
        for (Player p : players) {
            playerHands.put(p.getUUID(), new PlayerHand());
        }
    }

    public HashMap<String, Integer> getNrOfCardsForAllPlayers(){
        HashMap<String, Integer> nrOfCards = new HashMap<>();
        for(Map.Entry<String, PlayerHand> p : playerHands.entrySet()){
            nrOfCards.put(p.getKey(), p.getValue().getHand().size());
        }
        return nrOfCards;
    }

    public ArrayList<Card> getCardsForPlayer(String playerUUID) {
        if(playerHands.containsKey(playerUUID)){
            return playerHands.get(playerUUID).getHand();
        }else{
            return new ArrayList<>();
        }
    }

    public void forfeitCardsForPlayer(String playerId) {
        playedCards.addAll(playerHands.get(playerId).getHand());
        playerHands.get(playerId).dropCards();
//        players.get(playerId).getHand().clear(); todo: method is different, does it work correctly?
    }

    public void shuffleIfFirstRound(){
        if(roundNr != 0){
            return;
        }

        ArrayList<Card> cards = new ArrayList<>();
        activePlayers = gameState.getActivePlayers();
        // create cards
        int uniqueCardNr = 0;
        for (int suit = 0; suit < activePlayers.size(); suit++) {
            for (int cardValue = 1; cardValue < 14; cardValue++) {
                cards.add(new Card(suit % 4, cardValue, uniqueCardNr));
                uniqueCardNr++;
            }
        }

        // shuffle the cards
        Collections.shuffle(cards);
        cardsDeque = new ArrayDeque<>(cards);
    }

    public boolean playerPlaysCard(String playerId, Card card) {
        if(card != null) {
            playerHands.get(playerId).getHand().remove(card);
            playedCards.add(card);
            if(playerHands.get(playerId).getHand().isEmpty()){
                return true;
            }
        }
        return false;
    }

    public void giveCardToPlayerForTesting(String playerId, Card card){
        // this way you can replace one card by another, play a card in a Test, and then know based on
        // the game whether the player should have 5 or 4 cards in their hand left.
        playerHands.get(playerId).getHand().removeFirst();
        setPlayerCard(playerId, card);
    }

    public void setPlayerCard(String playerId, Card card){
        playerHands.get(playerId).addCard(card);
    }

    public void dealCards(){
        int nrCards;
        if(roundNr == 0){
            // new round so reset played cards stack
            playedCards.clear();
            nrCards = 5;
        }else{
            nrCards = 4;
        }

        //todo: is this reset necessary?
        for(String playerId: playerHands.keySet() ){
            playerHands.get(playerId).dropCards();
        }

        for (int j = 0; j < nrCards; j++) {
            for(Player player: gameState.getPlayers()){
                if(player.isActive()){
                    setPlayerCard(player.getUUID(), cardsDeque.pop());
                }
            }
        }

        roundNr = (roundNr + 1) % 3;
    }

    public boolean playerHasCard(String playerId, Card card ){
        return playerHands.get(playerId).hasCard(card);
    }

    public boolean playerDoesNotHaveCard(String playerId, Card card ){
        return !playerHasCard(playerId, card);
    }

    public void reset(){
        roundNr = 0;
        cardsDeque.clear();
        playerHands.clear();
        playedCards.clear();
    }

    public ArrayList<Card> getPlayedCards() {
        return playedCards;
    }
}
