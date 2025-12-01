package ADG.Games.Keezen.dto;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * DTO representing a PositionKey object returned by the Keezen Game API. Compatible with GWT
 * overlay types for JSON parsing via JsonUtils.safeEval().
 */
public class PositionKeyDTO extends JavaScriptObject {

  // Required protected constructor for GWT overlay types
  protected PositionKeyDTO() {}

  /** The ID of the player associated with this board position. */
  public final native String getPlayerId() /*-{ return this.playerId; }-*/;

  /** The tile number on the board for this position. */
  public final native int getTileNr() /*-{ return this.tileNr; }-*/;
}
