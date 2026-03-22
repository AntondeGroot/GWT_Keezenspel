package ADG.Games.Keezen.ApiUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proxies /chat/** requests to the chat server running on port 4100.
 *
 * <p>In production, nginx routes /chat/ directly to the GameRoom service. In local/test
 * environments (no nginx), this controller forwards requests so Selenium tests can reach the
 * ChatServerMock via the same origin.
 */
@RestController
@RequestMapping("/chat")
public class ChatProxyController {

  private static final String CHAT_SERVER_BASE_URL = "http://localhost:4100";
  private final HttpClient httpClient = HttpClient.newHttpClient();

  @GetMapping("/{sessionId}")
  public ResponseEntity<String> getChatMessages(@PathVariable("sessionId") String sessionId) {
    return proxy(sessionId, "GET", null);
  }

  @PostMapping("/{sessionId}")
  public ResponseEntity<String> sendChatMessage(
      @PathVariable("sessionId") String sessionId, @RequestBody String body) {
    return proxy(sessionId, "POST", body);
  }

  private ResponseEntity<String> proxy(String sessionId, String method, String body) {
    String targetUrl = CHAT_SERVER_BASE_URL + "/chat/" + sessionId;
    try {
      HttpRequest.Builder builder = HttpRequest.newBuilder()
          .uri(URI.create(targetUrl))
          .header("Content-Type", "application/json");

      if ("POST".equals(method)) {
        builder.POST(body != null
            ? HttpRequest.BodyPublishers.ofString(body)
            : HttpRequest.BodyPublishers.noBody());
      } else {
        builder.GET();
      }

      HttpResponse<String> response = httpClient.send(
          builder.build(), HttpResponse.BodyHandlers.ofString());

      return ResponseEntity.status(response.statusCode())
          .header("Content-Type", "application/json")
          .header("Access-Control-Allow-Origin", "*")
          .body(response.body());

    } catch (IOException | InterruptedException e) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
  }
}