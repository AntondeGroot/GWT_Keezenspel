package adg.keezen;

import adg.keezen.dto.CardClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardsDeck {

  private List<CardClient> cards = new ArrayList<>();
  private List<CardClient> playedCards = new ArrayList<>();
  private HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();

  public HashMap<String, Integer> getNrCardsPerPlayer() {
    return new HashMap<>(nrCardsPerPlayer);
  }

  public void setNrCardsPerPlayer(HashMap<String, Integer> nrCardsPerPlayer) {
    this.nrCardsPerPlayer = new HashMap<>(nrCardsPerPlayer);
  }

  public List<CardClient> getCards() {
    return new ArrayList<>(cards);
  }

  public List<CardClient> getPlayedCards() {
    return new ArrayList<>(playedCards);
  }

  public void setCards(List<CardClient> cards) {
    this.cards = new ArrayList<>(cards);
  }

  public void setPlayedCards(List<CardClient> playedCards) {
    this.playedCards = new ArrayList<>(playedCards);
  }

  //  public void processCardResponse(JsArray<CardClient> cardResponse) {
  //    setCards(cardResponse);
  //    setPlayedCards(cardResponse);
  //    setNrCardsPerPlayer(JsInteropUtil.toHashMap(cardResponse));
  //  }
}
