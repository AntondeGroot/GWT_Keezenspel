package ADG.Games.Keezen.dto;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * DTO representing a PawnId object returned by the Keezen Game API.
 * Compatible with GWT overlay types for JSON parsing via JsonUtils.safeEval().
 */
public class PawnIdDTO extends JavaScriptObject {

  // Required protected constructor for GWT overlay types
  protected PawnIdDTO() {}

  /**
   * The ID of the player who owns this pawn.
   */
  public final native String getPlayerId() /*-{ return this.playerId; }-*/;

  /**
   * The number of this pawn (0–3 per player).
   */
  public final native int getPawnNr() /*-{ return this.pawnNr; }-*/;

  // to string methods do not work with these overlay types!
  // this mean you also can't compare them with equals methods
}
