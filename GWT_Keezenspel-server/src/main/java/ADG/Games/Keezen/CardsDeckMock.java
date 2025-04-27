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
    private final ArrayList<Card> allCardsFromAceToKing = all13Cards();

    public CardsDeckMock() {
    }

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
        // the cards are not random so you do not need to shuffle them
    }

    public boolean playerPlaysCard(String playerId, Card card) {
        // todo: this is both playerPlaysCard and playerHasCardsLeft, also change the mocked cardsdeck
        if(card != null) {
            // playerHands.get(playerId).getHand().remove(card); DO NOT REMOVE CARD FROM PLAYER HAND FOR THE MOCKED DECK
            playedCards.add(card);
            if(playerHands.get(playerId).getHand().isEmpty()){
                return true;
            }
        }
        return false;
    }

    public void giveCardToPlayerForTesting(String playerId, Card card){
        playerHands.get(playerId).getHand().removeFirst();
        setPlayerCard(playerId, card);
    }

    public void setPlayerCard(String playerId, Card card){
        playerHands.get(playerId).addCard(card);
    }

    public void dealCards(){
        playedCards.clear();

        //todo: is this reset necessary?
        for(PlayerHand hand : playerHands.values()) {
            hand.dropCards();
        }

        for(Player player: gameState.getPlayers()){
            if(player.isActive()){
                for(Card card: allCardsFromAceToKing){
                    setPlayerCard(player.getUUID(), card);
                }
            }
        }

        roundNr = (roundNr + 1) % 3;
    }

    public boolean playerHasCard(String playerId, Card card ){
        return playerHands.get(playerId).hasCard(card);
    }

    public void reset(){
        roundNr = 0;
        playerHands.clear();
        playedCards.clear();
    }

    public ArrayList<Card> getPlayedCards() {
        return playedCards;
    }

    private ArrayList<Card> all13Cards(){
        ArrayList<Card> all13Cards = new ArrayList<>();
        for (int cardValue = 1; cardValue <= 13; cardValue++) {
            all13Cards.add(new Card(0, cardValue));
        }
        return all13Cards;
    }
}
