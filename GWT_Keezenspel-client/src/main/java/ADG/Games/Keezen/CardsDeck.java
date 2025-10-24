package ADG.Games.Keezen;

import ADG.Games.Keezen.dto.CardClient;
import ADG.Games.Keezen.util.JsInteropUtil;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardsDeck {

  private List<CardClient> cards = new ArrayList<>();
  private List<CardClient> playedCards = new ArrayList<>();
  private HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();

  public HashMap<String, Integer> getNrCardsPerPlayer() {
    return nrCardsPerPlayer;
  }

  public void setNrCardsPerPlayer(HashMap<String, Integer> nrCardsPerPlayer) {
    this.nrCardsPerPlayer = nrCardsPerPlayer;
  }

  public List<CardClient> getCards() {
    return cards;
  }

  public List<CardClient> getPlayedCards() {
    return playedCards;
  }

  public void setCards(List<CardClient> cards) {
    this.cards = cards;
  }

  public void setPlayedCards(List<CardClient> playedCards) {
    this.playedCards = playedCards;
  }

//  public void processCardResponse(JsArray<CardClient> cardResponse) {
//    setCards(cardResponse);
//    setPlayedCards(cardResponse);
//    setNrCardsPerPlayer(JsInteropUtil.toHashMap(cardResponse));
//  }
}
