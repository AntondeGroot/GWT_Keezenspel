package ADG.Games.Keezen.ApiTests;

import static org.junit.Assert.assertThrows;

import ADG.Games.Keezen.utils.ApiCallsHelper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;

class ApiGameCreationTest {

  private final ApiCallsHelper apiHelper = new ApiCallsHelper();

  @Test
  void cannotCreateGame_withEmptyRoomName_throws400ErrorCode() {
    // GIVEN a game has started
    assertThrows(HttpClientErrorException.BadRequest.class, () -> {
      apiHelper.createNewGame("", 3);
    });
  }

  @Test
  void cannotCreateGame_withBlankRoomName_throws400ErrorCode() {
    // GIVEN a game has started
    assertThrows(HttpClientErrorException.BadRequest.class, () -> {
      apiHelper.createNewGame(" ", 3);
    });
  }

  @Test
  void cannotCreateGame_withTooShortRoomName_throws400ErrorCode() {
    // GIVEN a game has started
    assertThrows(HttpClientErrorException.BadRequest.class, () -> {
      apiHelper.createNewGame("ab", 3);
    });
  }

  @Test
  void cannotCreateGame_withTooShortEmptyRoomName_throws400ErrorCode() {
    assertThrows(HttpClientErrorException.BadRequest.class, () -> {
      apiHelper.createNewGame("  .", 3);
    });
  }
}
