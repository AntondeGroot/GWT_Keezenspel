package ADG.Games.Keezen.dto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;

/**
 * Represents an array of CardDTO objects. Example JSON: [ { "suit": 2, "value": 6, "uuid": 31 }, {
 * "suit": 0, "value": 12, "uuid": 11 } ]
 */
public final class CardResponseDTO {

  private CardResponseDTO() {
    // static utility class, no instantiation
  }

  /** Parse a JSON array into a JsArray of CardDTOs. */
  public static JsArray<CardDTO> fromJson(String json) {
    return JsonUtils.safeEval(json);
  }
}
