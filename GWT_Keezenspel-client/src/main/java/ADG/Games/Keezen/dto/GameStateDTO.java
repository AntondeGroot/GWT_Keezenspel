package ADG.Games.Keezen.dto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

/**
 * DTO representing a GameState object returned by the Keezen Game API. Compatible with GWT overlay
 * types for JSON parsing via JsonUtils.safeEval().
 */
public class GameStateDTO extends JavaScriptObject {

  // Required protected constructor for GWT overlay types
  protected GameStateDTO() {}

  /** The ID of the player whose turn it is. */
  public final native String getCurrentPlayerId() /*-{ return this.currentPlayerId; }-*/;

  /** Array of Pawn objects in the current game state. */
  public final native JsArray<PawnDTO> getPawns() /*-{ return this.pawns; }-*/;

  /** Array of Player objects in the current game state. */
  public final native JsArray<PlayerDTO> getPlayers() /*-{ return this.players; }-*/;

  /** Array of winner identifiers. */
  public final native JsArrayString getWinners() /*-{ return this.winners; }-*/;

  /** Version number of this game state. */
  public final native double getVersion() /*-{ return this.version; }-*/;
}
