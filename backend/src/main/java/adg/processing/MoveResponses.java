package adg.processing;

import com.adg.openapi.model.MoveRejectionReason;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.MoveResult;

/**
 * Helpers for marking a {@link MoveResponse} as rejected. Each returns {@code false} so a guard
 * clause reads as {@code if (bad) return reject(response, result, reason);}.
 */
final class MoveResponses {

  private MoveResponses() {}

  static boolean reject(MoveResponse response, MoveResult result, MoveRejectionReason reason) {
    response.setResult(result);
    response.setRejectionReason(reason);
    return false;
  }

  static boolean reject(
      MoveResponse response, MoveResult result, MoveRejectionReason reason, Integer detail) {
    response.setRejectionDetail(detail);
    return reject(response, result, reason);
  }
}
