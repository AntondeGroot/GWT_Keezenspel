package ADG.Games.Keezen;

import ADG.Games.Keezen.Card;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardsDeck {

    private static List<Card> cards = new ArrayList<>();
    private static HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();

    public static boolean areCardsDifferent(List<Card> otherCards){
        return !cards.equals(otherCards);
    }

    public static HashMap<String, Integer> getNrCardsPerPlayer() {
        return nrCardsPerPlayer;
    }

    public static void setNrCardsPerPlayer(HashMap<String, Integer> nrCardsPerPlayer) {
        CardsDeck.nrCardsPerPlayer = nrCardsPerPlayer;
    }

    public static void setCards(List<Card> cards){
        CardsDeck.cards = cards;
    }

    public static List<Card> getCards(){
        return cards;
    }

    public static Card pickCard(int i){
        if(!cards.isEmpty() && i > -1 && i < cards.size()){
            return cards.get(i);
        }
        if(!cards.isEmpty() && i == -1){
            cards = null;
        }
        return null;
    }
}
