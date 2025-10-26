package ADG.Games.Keezen.util;

import ADG.Games.Keezen.dto.CardClient;
import ADG.Games.Keezen.dto.PawnClient;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * Utility class to build JSON objects for MoveRequest payloads.
 * Matches the OpenAPI definition for /moves/{sessionId}/{playerId}.
 */
public class MoveRequestJsonBuilder {

  private final JSONObject root;

  public MoveRequestJsonBuilder() {
    root = new JSONObject();
  }

  public MoveRequestJsonBuilder withPlayerId(String playerId) {
    root.put("playerId", new JSONString(playerId));
    return this;
  }

  public MoveRequestJsonBuilder withCardId(CardClient card) {
    if (card == null) {
      return this;
    }
    root.put("cardId", new JSONNumber(card.getUuid()));
    return this;
  }

  public MoveRequestJsonBuilder withMoveType(String moveType) {
    // enum: switch, onBoard, move, split
    root.put("moveType", new JSONString(moveType));
    return this;
  }

  public MoveRequestJsonBuilder withStepsPawn1(int steps) {
    root.put("stepsPawn1", new JSONNumber(steps));
    return this;
  }

  public MoveRequestJsonBuilder withStepsPawn2(int steps) {
    root.put("stepsPawn2", new JSONNumber(steps));
    return this;
  }

  public MoveRequestJsonBuilder withTempMessageType(String tempType) {
    // enum: CHECK_MOVE, MAKE_MOVE
    root.put("tempMessageType", new JSONString(tempType));
    return this;
  }

  public MoveRequestJsonBuilder withPawn1(PawnClient pawn) {
    if (pawn == null) {
      return this;
    }
    root.put("pawn1Id", createPawnId(pawn.getPlayerId(), pawn.getPawnNr()));
    return this;
  }

  public MoveRequestJsonBuilder withPawn2(PawnClient pawn) {
    if (pawn == null) {
      return this;
    }
    root.put("pawn2Id", createPawnId(pawn.getPlayerId(), pawn.getPawnNr()));
    return this;
  }

  private JSONObject createPawnId(String playerId, int pawnNr) {
    JSONObject pawnId = new JSONObject();
    pawnId.put("playerId", new JSONString(playerId));
    pawnId.put("pawnNr", new JSONNumber(pawnNr));
    return pawnId;
  }

  /** Returns the final JSON object for sending in an HTTP request. */
  public JSONObject build() {
    return root;
  }

  /** Returns the JSON as a String (for debugging or manual transmission). */
  public String toJsonString() {
    return root.toString();
  }
}
