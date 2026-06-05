package adg.keezen.UnitTests;

import static org.junit.jupiter.api.Assertions.*;

import adg.keezen.GameRegistry;
import com.adg.openapi.model.GameInfo;
import com.adg.openapi.model.GameInfo.StatusEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class GameRegistryTest {

  // GameRegistry holds a static map — track every ID we create so @AfterEach can clean up.
  private final List<String> created = new ArrayList<>();

  private String track(String id) {
    created.add(id);
    return id;
  }

  @AfterEach
  void cleanup() {
    created.forEach(GameRegistry::removeGame);
    created.clear();
  }

  // ── createNewGame() ───────────────────────────────────────────────────────

  @Test
  void createNewGame_returnsNonNullId() {
    String id = track(GameRegistry.createNewGame());
    assertNotNull(id);
    assertFalse(id.isBlank());
  }

  @Test
  void createNewGame_createdGameIsRetrievable() {
    String id = track(GameRegistry.createNewGame());
    assertNotNull(GameRegistry.getGame(id));
  }

  // ── createNewGame(sessionId) ──────────────────────────────────────────────

  @Test
  void createNewGame_withSessionId_storesUnderThatId() {
    String id = track(UUID.randomUUID().toString());
    GameRegistry.createNewGame(id);
    assertNotNull(GameRegistry.getGame(id));
  }

  @Test
  void createNewGame_withSessionId_returnsProvidedId() {
    String id = UUID.randomUUID().toString();
    assertEquals(id, track(GameRegistry.createNewGame(id)));
  }

  // ── createNewGame(sessionId, roomName, maxPlayers) ────────────────────────

  @Test
  void createNewGame_withRoomNameAndMaxPlayers_storesGame() {
    String id = track(UUID.randomUUID().toString());
    GameRegistry.createNewGame(id, "TestRoom", 4);
    assertNotNull(GameRegistry.getGame(id));
  }

  @Test
  void createNewGame_withRoomNameAndMaxPlayers_returnsProvidedId() {
    String id = UUID.randomUUID().toString();
    assertEquals(id, track(GameRegistry.createNewGame(id, "Room", 3)));
  }

  @Test
  void createNewGame_withRoomNameAndMaxPlayers_appearsInGetAllGames() {
    String id = track(UUID.randomUUID().toString());
    GameRegistry.createNewGame(id, "MyRoom", 3);

    boolean found = GameRegistry.getAllGames().stream()
        .anyMatch(g -> id.equals(g.getId()) && "MyRoom".equals(g.getRoomName()));
    assertTrue(found, "Newly created game should appear in getAllGames with the correct room name");
  }

  // ── createTestGame ────────────────────────────────────────────────────────

  @Test
  void createTestGame_storesGame() {
    String id = track(UUID.randomUUID().toString());
    GameRegistry.createTestGame(id);
    assertNotNull(GameRegistry.getGame(id));
  }

  @Test
  void createTestGame_returnsProvidedId() {
    String id = UUID.randomUUID().toString();
    assertEquals(id, track(GameRegistry.createTestGame(id)));
  }

  // ── getGame ───────────────────────────────────────────────────────────────

  @Test
  void getGame_unknownId_returnsNull() {
    assertNull(GameRegistry.getGame("does-not-exist-" + UUID.randomUUID()));
  }

  // ── getAllGames ───────────────────────────────────────────────────────────

  @Test
  void getAllGames_startedGame_statusIsInProgress() {
    String id = track(UUID.randomUUID().toString());
    GameRegistry.createNewGame(id, "StartedRoom", 3);
    GameStateUtil.createGame_With_NPlayers(GameRegistry.getGame(id).getGameState(), 3);

    GameInfo info = GameRegistry.getAllGames().stream()
        .filter(g -> id.equals(g.getId()))
        .findFirst()
        .orElseThrow();

    assertEquals(StatusEnum.IN_PROGRESS, info.getStatus());
  }

  @Test
  void getAllGames_newGame_statusIsWaiting() {
    String id = track(UUID.randomUUID().toString());
    GameRegistry.createNewGame(id, "WaitingRoom", 3);

    GameInfo info = GameRegistry.getAllGames().stream()
        .filter(g -> id.equals(g.getId()))
        .findFirst()
        .orElseThrow();

    assertEquals(StatusEnum.WAITING, info.getStatus());
  }

  @Test
  void getAllGames_includesMaxPlayers() {
    String id = track(UUID.randomUUID().toString());
    GameRegistry.createNewGame(id, "Room", 6);

    GameInfo info = GameRegistry.getAllGames().stream()
        .filter(g -> id.equals(g.getId()))
        .findFirst()
        .orElseThrow();

    assertEquals(6, info.getMaxNrPlayers());
  }

  // ── removeGame ────────────────────────────────────────────────────────────

  @Test
  void removeGame_gameIsNoLongerAccessible() {
    String id = UUID.randomUUID().toString();
    GameRegistry.createNewGame(id);
    GameRegistry.removeGame(id);
    assertNull(GameRegistry.getGame(id));
  }

  @Test
  void removeGame_gameNoLongerAppearsInGetAllGames() {
    String id = UUID.randomUUID().toString();
    GameRegistry.createNewGame(id, "ToDelete", 3);
    GameRegistry.removeGame(id);

    boolean stillPresent = GameRegistry.getAllGames().stream()
        .anyMatch(g -> id.equals(g.getId()));
    assertFalse(stillPresent);
  }
}