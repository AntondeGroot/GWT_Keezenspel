package ADG.Games.Keezen.dto;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * DTO representing a card object returned by the Keezen Game API. Compatible with GWT overlay types
 * for JSON parsing via JsonUtils.safeEval().
 */
public class CardDTO extends JavaScriptObject {

  protected CardDTO() {
    // Required protected constructor for GWT overlay types
  }

  /** The suit index (0–3). */
  public final native int getSuit() /*-{ return this.suit; }-*/;

  /** The face value (1 = Ace, 2–10, 11 = Jack, 12 = Queen, 13 = King). */
  public final native int getValue() /*-{ return this.value; }-*/;

  /** Unique identifier for duplicate cards (for games with more than 4 players). */
  public final native int getUuid() /*-{ return this.uuid; }-*/;
}
