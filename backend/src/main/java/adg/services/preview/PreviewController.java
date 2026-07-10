package adg.services.preview;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dev-only HTML landing pages that create pre-seeded game scenarios so you can open a specific
 * board state without playing through the game.
 *
 * <p>Usage:
 * <ol>
 *   <li>Navigate to /keezen/preview to see the scenario list.
 *   <li>Click a scenario to create a fresh game session and choose which player to open as.
 *   <li>Open multiple browser tabs for multi-player testing.
 * </ol>
 *
 * <p>Routing only: the scenario catalog and seeding live in {@link PreviewScenarios}, the page
 * rendering in {@link PreviewHtml}.
 */
@RestController
@RequestMapping("/preview")
public class PreviewController {

  @GetMapping("")
  public ResponseEntity<String> list() {
    return html(PreviewHtml.listPage(PreviewScenarios.all()));
  }

  @GetMapping("/{scenario}")
  public ResponseEntity<String> open(@PathVariable("scenario") String scenario) {
    List<PreviewScenarios.PlayerLink> links = PreviewScenarios.seed(scenario);
    if (links == null) {
      return ResponseEntity.notFound().build();
    }
    return html(PreviewHtml.scenarioPage(scenario, PreviewScenarios.labelFor(scenario), links));
  }

  private static ResponseEntity<String> html(String body) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
        .body(body);
  }
}
