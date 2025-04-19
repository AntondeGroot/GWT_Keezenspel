package ADG.Games.Keezen;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Cards.CardResponse;
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

    public void processCardResponse(CardResponse cardResponse){
        setCards(cardResponse.getCards());
        setNrCardsPerPlayer(cardResponse.getNrOfCardsPerPlayer());
        setPlayedCards(cardResponse.getPlayedCards());
    }
}
