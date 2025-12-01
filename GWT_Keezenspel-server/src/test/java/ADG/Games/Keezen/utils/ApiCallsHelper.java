package ADG.Games.Keezen.utils;

import com.adg.openapi.model.NewGameRequest;
import com.adg.openapi.model.Player;
import java.util.List;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class ApiCallsHelper {

  private final RestTemplate restTemplate;
  private final String baseUrl = "http://localhost:4200";

  public ApiCallsHelper() {
    this.restTemplate = new RestTemplate();
  }

  // ---------- GAMES ----------
  public List<Map<String, Object>> getAllGames() {
    String url = baseUrl + "/games";
    ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, null, List.class);
    return response.getBody();
  }

  public String createNewGame(String roomName, int maxPlayers) {
    // Create the request body
    NewGameRequest request = new NewGameRequest();
    request.setRoomName(roomName);
    request.setMaxPlayers(maxPlayers);

    // Build the HTTP entity with headers
    String url = baseUrl + "/games";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<NewGameRequest> entity = new HttpEntity<>(request, headers);

    // Perform the POST request
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

    // Return the response body as a Map (can be replaced by a custom class)
    return response.getBody().get("sessionId").toString();
  }

  public void startGame(String sessionId) {
    restTemplate.postForEntity(baseUrl + "/games/" + sessionId + "/", null, Void.class);
  }

  public void stopGame(String sessionId) {
    restTemplate.delete(baseUrl + "/games/" + sessionId + "/");
  }

  // ---------- PLAYERS ----------
  public List<Map<String, Object>> getAllPlayersInGame(String sessionId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/games/{sessionId}/players")
            .buildAndExpand(sessionId)
            .toString();
    ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, null, List.class);
    return response.getBody();
  }

  public String addPlayerToGame(String sessionId, Player player) {
    String url = baseUrl + "/games/" + sessionId + "/players";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<Map> response =
        restTemplate.postForEntity(url, new HttpEntity<>(player, headers), Map.class);
    return response.getBody().get("playerId").toString();
  }

  // ---------- MOVES ----------
  public Map<String, Object> makeMove(
      String sessionId, String playerId, Map<String, Object> moveRequest) {
    String url = baseUrl + "/moves/" + sessionId + "/" + playerId;
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(moveRequest, headers);
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
    return response.getBody();
  }

  public Map<String, Object> checkMove(
      String sessionId, String playerId, Map<String, Object> moveRequest) {
    String url = baseUrl + "/moves/" + sessionId + "/" + playerId + "/test";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(moveRequest, headers);
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
    return response.getBody();
  }

  // ---------- CARDS ----------
  public Map<String, Object> getPubliclyAvailableCardInformation(String sessionId) {
    String url = baseUrl + "/cards/" + sessionId;
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
    return response.getBody();
  }

  public List<Map<String, Object>> getPlayerCards(String sessionId, String playerId) {
    String url = baseUrl + "/cards/" + sessionId + "/" + playerId;
    ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, null, List.class);
    return response.getBody();
  }

  public void playerForfeits(String sessionId, String playerId) {
    restTemplate.delete(baseUrl + "/cards/" + sessionId + "/" + playerId);
  }

  // ---------- GAME STATES ----------
  public Map<String, Object> getGameState(String sessionId) {
    String url = baseUrl + "/gamestates/" + sessionId;
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
    return response.getBody();
  }

  public Map<String, Object> getGameDetails(String sessionId) {
    String url = baseUrl + "/games/" + sessionId;
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
    return response.getBody();
  }
}
