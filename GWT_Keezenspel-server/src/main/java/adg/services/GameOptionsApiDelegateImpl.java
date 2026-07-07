package adg.services;

import adg.keezen.GameOptionTranslations;
import adg.keezen.KeezenGameOptions;
import com.adg.openapi.api.GameOptionsApiDelegate;
import com.adg.openapi.model.GameOption;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class GameOptionsApiDelegateImpl implements GameOptionsApiDelegate {

  @Override
  public ResponseEntity<List<GameOption>> getGameOptions() {
    String locale = resolveLocale();
    // Resolve each option's label/description on the backend (like /game-name) so the
    // GameRoom just displays them. Each call returns fresh GameOption instances, so
    // mutating them in place is safe. labelKey/descriptionKey are left intact.
    List<GameOption> options = KeezenGameOptions.all();
    for (GameOption o : options) {
      o.setLabel(GameOptionTranslations.resolve(o.getLabelKey(), o.getLabel(), locale));
      o.setDescription(
          GameOptionTranslations.resolve(o.getDescriptionKey(), o.getDescription(), locale));
    }
    return ResponseEntity.ok(options);
  }

  /**
   * The requested locale: the {@code ?locale=} query param (as /game-name uses), else the
   * first subtag of the Accept-Language header, else null → English fallback.
   */
  private static String resolveLocale() {
    if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
      return null;
    }
    HttpServletRequest request = attrs.getRequest();
    String param = request.getParameter("locale");
    if (param != null && !param.isBlank()) {
      return param;
    }
    String acceptLanguage = request.getHeader("Accept-Language");
    if (acceptLanguage != null && !acceptLanguage.isBlank()) {
      // e.g. "nl-NL,en;q=0.9" -> "nl"
      return acceptLanguage.split(",")[0].split(";")[0].split("-")[0].trim();
    }
    return null;
  }
}
