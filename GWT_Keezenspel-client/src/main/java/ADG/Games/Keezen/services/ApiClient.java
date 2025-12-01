package ADG.Games.Keezen.services;

import ADG.Games.Keezen.dto.CardDTO;
import ADG.Games.Keezen.dto.GameStateDTO;
import ADG.Games.Keezen.dto.MoveResponseDTO;
import ADG.Games.Keezen.dto.TestMoveResponseDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Window;

public class ApiClient {

  private static final String BASE_URL = "http://localhost:4200";

  // === GAMES ===
  public void getAllGames(ApiCallback<JSONArray> callback) {
    get("/games", response -> JSONParser.parseStrict(response).isArray(), callback);
  }

  public void createNewGame(JSONObject newGameRequest, ApiCallback<JSONObject> callback) {
    post(
        "/games",
        newGameRequest,
        response -> JSONParser.parseStrict(response).isObject(),
        callback);
  }

  public void getGame(String sessionId, ApiCallback<JSONObject> callback) {
    get("/games/" + sessionId, response -> JSONParser.parseStrict(response).isObject(), callback);
  }

  public void startGame(String sessionId, ApiCallback<Void> callback) {
    post("/games/" + sessionId + "/", null, r -> null, callback);
  }

  public void stopGame(String sessionId, ApiCallback<Void> callback) {
    delete("/games/" + sessionId + "/", r -> null, callback);
  }

  // === PLAYERS ===
  public void getAllPlayersInGame(String sessionId, ApiCallback<JSONArray> callback) {
    get(
        "/games/" + sessionId + "/players",
        response -> JSONParser.parseStrict(response).isArray(),
        callback);
  }

  public void addPlayerToGame(String sessionId, JSONObject playerJson, ApiCallback<Void> callback) {
    post("/games/" + sessionId + "/players", playerJson, r -> null, callback);
  }

  // === MOVES ===
  public void makeMove(
      String sessionId,
      String playerId,
      JSONObject moveJson,
      ApiCallback<MoveResponseDTO> callback) {
    post(
        "/moves/" + sessionId + "/" + playerId,
        moveJson,
        json -> JsonUtils.<MoveResponseDTO>safeEval(json),
        callback);
  }

  public void checkMove(
      String sessionId,
      String playerId,
      JSONObject moveJson,
      ApiCallback<TestMoveResponseDTO> callback) {
    post(
        "/moves/" + sessionId + "/" + playerId + "/test",
        moveJson,
        json -> JsonUtils.<TestMoveResponseDTO>safeEval(json),
        callback);
  }

  // === CARDS ===
  // just an array of CardDTO, use safeEval
  public void getPlayerCards(
      String sessionId, String playerId, ApiCallback<JsArray<CardDTO>> callback) {
    get(
        "/cards/" + sessionId + "/" + playerId,
        json -> JsonUtils.<JsArray<CardDTO>>safeEval(json),
        callback);
  }

  public void playerForfeits(String sessionId, String playerId, ApiCallback<Void> callback) {
    delete("/cards/" + sessionId + "/" + playerId, r -> null, callback);
  }

  // === GAMESTATE ===
  public void getGameState(String sessionId, ApiCallback<JSONObject> callback) {
    get(
        "/gamestates/" + sessionId,
        response -> JSONParser.parseStrict(response).isObject(),
        callback);
  }

  // Overload with stateVersion query
  public void getGameState(
      String sessionId, Long clientVersion, ApiCallback<GameStateDTO> callback) {
    String path = "/gamestates/" + sessionId;
    if (clientVersion != null) {
      path += "?stateVersion=" + clientVersion;
    }

    // Use JsonUtils.safeEval() for overlay types (GameStateDTO)
    get(path, JsonUtils::<GameStateDTO>safeEval, callback);
  }

  // === GENERIC HTTP HELPERS ===
  private <T> void get(String path, JsonParser<T> parser, ApiCallback<T> callback) {
    sendRequest(RequestBuilder.GET, path, null, parser, callback);
  }

  private <T> void post(
      String path, JSONObject payload, JsonParser<T> parser, ApiCallback<T> callback) {
    sendRequest(RequestBuilder.POST, path, payload, parser, callback);
  }

  private <T> void delete(String path, JsonParser<T> parser, ApiCallback<T> callback) {
    sendRequest(RequestBuilder.DELETE, path, null, parser, callback);
  }

  private <T> void sendRequest(
      RequestBuilder.Method method,
      String path,
      JSONObject data,
      JsonParser<T> parser,
      ApiCallback<T> callback) {

    String url = BASE_URL + path;
    RequestBuilder builder = new RequestBuilder(method, URL.encode(url));
    builder.setHeader("Accept", "application/json");
    if (data != null) {
      builder.setHeader("Content-Type", "application/json");
    }

    try {
      builder.sendRequest(
          data == null ? null : data.toString(),
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request req, Response res) {
              int status = res.getStatusCode();
              GWT.log("HTTP status: " + status + ", body: '" + res.getText() + "'");
              // === NOT MODIFIED === some browsers GWT clients may consider 304 as succesful, so
              // this would not trigger
              // if it were placed after the 200> x <300 check
              if (status == 304) {
                // No body expected — directly notify the callback
                callback.onHttpError(304, "Not Modified");
                return;
              }
              // === SUCCESS ===
              if (status >= 200 && status < 300) {
                // 200 OK with optional body
                String text = res.getText();

                GWT.log("Anton HTTP status: " + status + ", body: '" + text + "'");

                if (text == null || text.trim().isEmpty()) {
                  // Allow 204 or empty 200 responses silently
                  callback.onHttpError(status, "Empty body (OK or No Content)");
                  return;
                }

                try {
                  T parsed = parser.parse(text);
                  callback.onSuccess(parsed);
                } catch (Exception e) {
                  callback.onFailure(e);
                }
                return;
              }

              // === OTHER ERRORS ===
              callback.onHttpError(status, res.getStatusText());
            }

            @Override
            public void onError(Request req, Throwable ex) {
              callback.onFailure(ex);
            }
          });
    } catch (RequestException e) {
      callback.onFailure(e);
    }
  }

  // === Functional interfaces ===
  @FunctionalInterface
  public interface JsonParser<T> {

    T parse(String json);
  }

  public interface ApiCallback<T> {

    void onSuccess(T result);

    default void onFailure(Throwable caught) {
      Window.alert("API error: " + caught.getMessage());
    }

    // Optional override for HTTP responses with specific codes
    default void onHttpError(int statusCode, String statusText) {
      onFailure(new Exception("HTTP " + statusCode + ": " + statusText));
    }
  }
}
