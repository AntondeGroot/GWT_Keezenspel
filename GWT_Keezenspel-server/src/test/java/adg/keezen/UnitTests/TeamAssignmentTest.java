package adg.keezen.UnitTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adg.keezen.GameSession;
import adg.keezen.GameState;
import adg.keezen.KeezenGameOptions;
import com.adg.openapi.model.Player;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Team play, step 1: opposite-seat team assignment. Teams are PAIRS — each player teams up
 * with the player directly opposite (seat + n/2) — so n players make n/2 teams
 * (4→2, 6→3, 8→4). teamId is left null when teams are off or the count is odd.
 */
class TeamAssignmentTest {

  /** Start a game with n players, no shuffle so playerInt == insertion order (seat). */
  private static GameState startWith(int nrPlayers, boolean teamPlay) {
    GameState gs = new GameSession().getGameState();
    gs.setTeamPlay(teamPlay);
    for (int i = 0; i < nrPlayers; i++) {
      gs.addPlayer(new Player(String.valueOf(i), String.valueOf(i)));
    }
    gs.start(false);
    return gs;
  }

  private static Player seat(GameState gs, int seat) {
    return gs.getPlayers().stream()
        .filter(p -> p.getPlayerInt() == seat)
        .findFirst()
        .orElseThrow();
  }

  @Test
  void teamsOff_leavesTeamIdNull() {
    GameState gs = startWith(4, false);
    for (Player p : gs.getPlayers()) {
      assertNull(p.getTeamId(), "seat " + p.getPlayerInt() + " should have no team");
    }
  }

  @Test
  void fourPlayers_makeTwoTeamsOfOppositeSeats() {
    assertTeamsArePairs(startWith(4, true), 2);
  }

  @Test
  void sixPlayers_makeThreeTeams() {
    assertTeamsArePairs(startWith(6, true), 3);
  }

  @Test
  void eightPlayers_makeFourTeams() {
    assertTeamsArePairs(startWith(8, true), 4);
  }

  @Test
  void invalidCounts_teamPlayOn_leaveTeamIdNull() {
    // 2 = a single pair with no opponents; 3 and 5 = odd (can't pair). None are team games,
    // so no teamId is assigned even with the option on.
    for (int count : new int[] {2, 3, 5}) {
      GameState gs = startWith(count, true);
      for (Player p : gs.getPlayers()) {
        assertNull(p.getTeamId(), count + " players should get no team");
      }
    }
  }

  @Test
  void applyOption_teamPlay_setsFlag() {
    GameState gs = new GameSession().getGameState();
    KeezenGameOptions.apply(gs, Map.of("teamPlay", true));
    assertTrue(gs.isTeamPlay());
  }

  /** teamId == seat % teamCount, and each seat i shares its team with the opposite seat i+n/2. */
  private static void assertTeamsArePairs(GameState gs, int expectedTeamCount) {
    int n = gs.getPlayers().size();
    assertEquals(expectedTeamCount, n / 2);
    for (Player p : gs.getPlayers()) {
      int seatNr = p.getPlayerInt();
      assertEquals(Integer.valueOf(seatNr % expectedTeamCount), p.getTeamId(),
          "seat " + seatNr + " teamId");
      Player opposite = seat(gs, (seatNr + n / 2) % n);
      assertEquals(p.getTeamId(), opposite.getTeamId(),
          "seat " + seatNr + " and opposite " + opposite.getPlayerInt() + " should be teammates");
      assertNotEquals(seatNr, opposite.getPlayerInt());
    }
  }
}
