package ADG.Games.Keezen;

import ADG.Games.Keezen.dto.CardDTO;
import ADG.Games.Keezen.util.JsInteropUtil;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardsDeck {

  private List<CardDTO> cards = new ArrayList<>();
  private List<CardDTO> playedCards = new ArrayList<>();
  private HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();

  public HashMap<String, Integer> getNrCardsPerPlayer() {
    return nrCardsPerPlayer;
  }

  public void setNrCardsPerPlayer(HashMap<String, Integer> nrCardsPerPlayer) {
    this.nrCardsPerPlayer = nrCardsPerPlayer;
  }

  public List<CardDTO> getCards() {
    return cards;
  }

  public List<CardDTO> getPlayedCards() {
    return playedCards;
  }

  public void setCardsFromJsArray(JsArray<CardDTO> jsCards) {
    List<CardDTO> cardList = new ArrayList<>();
    for (int i = 0; i < jsCards.length(); i++) {
      cardList.add(jsCards.get(i));
    }
    this.cards = cardList;
  }

  public void setPlayedCardsFromJsArray(JsArray<CardDTO> jsPlayedCards) {
    List<CardDTO> playedList = new ArrayList<>();
    for (int i = 0; i < jsPlayedCards.length(); i++) {
      playedList.add(jsPlayedCards.get(i));
    }
    this.playedCards = playedList;
  }

  public void processCardResponse(JsArray<CardDTO> cardResponse) {
    setCardsFromJsArray(cardResponse);
    setPlayedCardsFromJsArray(cardResponse);
    setNrCardsPerPlayer(JsInteropUtil.toHashMap(cardResponse));
  }
}
