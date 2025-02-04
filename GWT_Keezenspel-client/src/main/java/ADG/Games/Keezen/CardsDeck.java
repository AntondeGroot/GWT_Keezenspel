package ADG.Games.Keezen;

import ADG.Games.Keezen.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardsDeck {

    private List<Card> cards = new ArrayList<>();
    private ArrayList<Card> playedCards = new ArrayList<>();
    private HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();

    public HashMap<String, Integer> getNrCardsPerPlayer() {
        return nrCardsPerPlayer;
    }

    public void setNrCardsPerPlayer(HashMap<String, Integer> nrCardsPerPlayer) {
        this.nrCardsPerPlayer = nrCardsPerPlayer;
    }

    public void setCards(List<Card> cards){
        this.cards = cards;
    }

    public List<Card> getCards(){
        return cards;
    }

    public void setPlayedCards(ArrayList<Card> playedCards){
        this.playedCards = playedCards;
    }

    public ArrayList<Card> getPlayedCards(){
        return playedCards;
    }

    public Card pickCard(int i){
        if(!cards.isEmpty() && i > -1 && i < cards.size()){
            return cards.get(i);
        }
        if(!cards.isEmpty() && i == -1){
            cards = null;
        }
        return null;
    }

    public void processCardResponse(CardResponse cardResponse){
        setCards(cardResponse.getCards());
        setNrCardsPerPlayer(cardResponse.getNrOfCardsPerPlayer());
        setPlayedCards(cardResponse.getPlayedCards());
    }
}
