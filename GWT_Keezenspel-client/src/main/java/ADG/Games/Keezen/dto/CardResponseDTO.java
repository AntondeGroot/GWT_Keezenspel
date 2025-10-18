package ADG.Games.Keezen.dto;

import ADG.Games.Keezen.Cards.Card;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;

public class CardResponseDTO extends JavaScriptObject {
  protected CardResponseDTO() {} // required by GWT overlay types

  public final native String getPlayerId() /*-{ return this.playerUUID; }-*/;
  public final native JsArray<CardDTO> getCards() /*-{ return this.cards; }-*/;
  public final native JsArray<CardDTO> getPlayedCards() /*-{ return this.playedCards; }-*/;

  // If your API uses an object like {"player1":4, "player2":5}, represent it as a JSON string map
  public final native JavaScriptObject getNrOfCardsPerPlayer() /*-{ return this.nrOfCardsPerPlayer; }-*/;

  public static CardResponseDTO fromJson(String json) {
    return JsonUtils.safeEval(json);
  }
}

