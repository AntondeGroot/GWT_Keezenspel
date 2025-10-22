package ADG.Games.Keezen.dto;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * DTO representing a Pawn object returned by the Keezen Game API.
 * Compatible with GWT overlay types for JSON parsing via JsonUtils.safeEval().
 */
public class PawnDTO extends JavaScriptObject {

  // Required protected constructor for GWT overlay types
  protected PawnDTO() {}

  /**
   * The ID of the player who owns this pawn.
   */
  public final native String getPlayerId() /*-{ return this.playerId; }-*/;

  /**
   * The unique identifier object for this pawn (contains playerId + pawnNr).
   */
  public final native PawnIdDTO getPawnId() /*-{ return this.pawnId; }-*/;

  /**
   * The position of the pawn on the board (tile number + player reference).
   */
  public final native PositionKeyDTO getCurrentTileId() /*-{ return this.currentTileId; }-*/;

  /**
   * The starting ("nest") tile for this pawn.
   */
  public final native PositionKeyDTO getNestTileId() /*-{ return this.nestTileId; }-*/;

  /**
   * Optional URI (e.g., pawn image or asset reference).
   */
  public final native String getUri() /*-{ return this.uri; }-*/;
}
