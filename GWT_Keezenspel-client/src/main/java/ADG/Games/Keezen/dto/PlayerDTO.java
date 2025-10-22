package ADG.Games.Keezen.dto;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * DTO representing a Player object returned by the Keezen Game API.
 * Compatible with GWT overlay types for JSON parsing via JsonUtils.safeEval().
 */
public class PlayerDTO extends JavaScriptObject {

  // Required protected constructor for GWT overlay types
  protected PlayerDTO() {}

  /** Unique identifier of the player. */
  public final native String getId() /*-{ return this.id; }-*/;

  /** Display name of the player. */
  public final native String getName() /*-{ return this.name; }-*/;

  /** Optional profile picture URL. */
  public final native String getProfilePic() /*-{ return this.profilePic; }-*/;

  /** Optional color representing the player on the board. */
  public final native String getColor() /*-{ return this.color; }-*/;

  /** Whether the player is currently active in the game. */
  public final native boolean isActive() /*-{ return !!this.isActive; }-*/;

  /** Whether the player is currently playing (has not finished or forfeited). */
  public final native boolean isPlaying() /*-{ return !!this.isPlaying; }-*/;

  /** The player's finishing place (1 = first, 2 = second, etc., -1 if not yet finished). */
  public final native int getPlace() /*-{ return this.place == null ? -1 : this.place; }-*/;
}
