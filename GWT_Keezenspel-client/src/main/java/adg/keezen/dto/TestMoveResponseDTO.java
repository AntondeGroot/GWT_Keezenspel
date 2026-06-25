package adg.keezen.dto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Represents the TestMoveResponse defined in the Keezen OpenAPI spec.
 *
 * <p>Schema reference: TestMoveResponse: type: object description: only returns PositionKeys you
 * need to highlight for possible moves properties: tiles: $ref: '#/components/schemas/MoveSet'
 * (array of PositionKey)
 */
public class TestMoveResponseDTO extends JavaScriptObject {

  // Overlay types always have a protected zero-arg constructor
  protected TestMoveResponseDTO() {}

  /** Returns the list of tile positions (array of PositionKeyDTO) that the player can move to. */
  public final native JsArray<PositionKeyDTO> getTiles() /*-{
    return this.tiles || [];
  }-*/;

  /**
   * Recommended steps for pawn1 in a 7-split (the allocation that lands a pawn deepest in the
   * finish), or -1 when the server made no recommendation. Pawn2's value is the complement.
   */
  public final native int getRecommendedStepsPawn1() /*-{
    return (this.recommendedStepsPawn1 == null) ? -1 : this.recommendedStepsPawn1;
  }-*/;

  public final native int getRecommendedStepsPawn2() /*-{
    return (this.recommendedStepsPawn2 == null) ? -1 : this.recommendedStepsPawn2;
  }-*/;
}
