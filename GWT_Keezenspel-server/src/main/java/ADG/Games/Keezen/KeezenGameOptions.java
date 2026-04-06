package ADG.Games.Keezen;

import ADG.Log;
import com.adg.openapi.model.GameOption;
import com.adg.openapi.model.GameOption.TypeEnum;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for all configurable Keezen game options.
 *
 * <p>To add a new option:
 * <ol>
 *   <li>Add a {@link GameOption} entry in {@link #all()}.
 *   <li>Add a {@code case} in {@link #applyOption} that maps the key to the corresponding
 *       {@link GameState} setter.
 * </ol>
 */
public class KeezenGameOptions {

  public static List<GameOption> all() {
    return List.of(
        new GameOption(
            "exactMoveRequired",
            "Exact move required",
            "When enabled, a pawn must land exactly on its destination. "
                + "Direction reversals are not allowed: bouncing off a blocked start tile "
                + "or overshooting the finish lane will be rejected as an illegal move.",
            TypeEnum.BOOLEAN,
            "true")
    );
  }

  public static void apply(GameState gameState, Map<String, Object> options) {
    if (options == null || options.isEmpty()) return;
    for (Map.Entry<String, Object> entry : options.entrySet()) {
      applyOption(gameState, entry.getKey(), entry.getValue());
    }
  }

  private static void applyOption(GameState gameState, String key, Object value) {
    switch (key) {
      case "exactMoveRequired" -> gameState.setExactMoveRequired(toBoolean(value));
      default -> Log.info("KeezenGameOptions: unknown option key '" + key + "', ignoring");
    }
  }

  private static boolean toBoolean(Object value) {
    if (value instanceof Boolean b) return b;
    if (value instanceof String s) return Boolean.parseBoolean(s);
    return false;
  }
}