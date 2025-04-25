package ADG.Games.Keezen;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Player.Player;
import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CardsDeckMock implements CardsDeckInterface, IsSerializable {
    private int roundNr;
    private final ArrayList<Card> playedCards = new ArrayList<>();
    private final HashMap<String, PlayerHand> playerHands = new HashMap<>();
    private GameState gameState;

    public CardsDeckMock() {}

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
    }

    public void shuffleIfFirstRound(){
        if(roundNr != 0){
            return;
        }

//        ArrayList<Card> cards = new ArrayList<>();
//        activePlayers = gameState.getActivePlayers();
        // create cards
        // ignored

        // shuffle the cards
        // ignored
    }

    public boolean playerPlaysCard(String playerId, Card card) {return true;}

    public void giveCardToPlayerForTesting(String playerId, Card card){}

    public void setPlayerCard(String playerId, Card card){
        playerHands.get(playerId).addCard(card);
    }

    public void dealCards(){
        playedCards.clear();

        //todo: is this reset necessary?
        for(String playerId: playerHands.keySet() ){
            playerHands.get(playerId).dropCards();
        }

        for(Player player: gameState.getPlayers()){
            if(player.isActive()){
                for (int cardValue = 1; cardValue < 14; cardValue++) {
                    setPlayerCard(player.getUUID(), new Card(0, cardValue, 1));
                }
            }
        }

        roundNr = (roundNr + 1) % 3;
    }

    public boolean playerHasCard(String playerId, Card card ){
        return true;
    }

    public void reset(){
        roundNr = 0;
    }

    public ArrayList<Card> getPlayedCards() {
        return playedCards;
    }
}
