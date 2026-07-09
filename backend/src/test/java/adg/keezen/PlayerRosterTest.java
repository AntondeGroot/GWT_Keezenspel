package adg.keezen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adg.openapi.model.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Direct unit tests for the extracted roster/seating queries over a live players list + seat map. */
class PlayerRosterTest {

  private final List<Player> players = new ArrayList<>();
  private final Map<String, Integer> seats = new HashMap<>();
  private PlayerRoster roster;

  @BeforeEach
  void setUp() {
    players.clear();
    seats.clear();
    roster = new PlayerRoster(players, seats);
  }

  private Player add(String id, Integer teamId, int seat) {
    Player p = new Player().id(id).teamId(teamId);
    players.add(p);
    seats.put(id, seat);
    return p;
  }

  // ── findById ────────────────────────────────────────────────────────────────
  @Test
  void findsAnExistingPlayer() {
    Player a = add("A", 0, 0);
    assertSame(a, roster.findById("A"));
  }

  @Test
  void returnsNullForAnUnknownPlayer() {
    add("A", 0, 0);
    assertNull(roster.findById("Z"));
  }

  // ── teammateOf ──────────────────────────────────────────────────────────────
  @Test
  void noTeammateWhenPlayerIsUnknown() {
    assertNull(roster.teammateOf("Z"));
  }

  @Test
  void noTeammateWhenPlayerHasNoTeam() {
    add("A", null, 0);
    assertNull(roster.teammateOf("A"));
  }

  @Test
  void findsTheTeammateSharingTheTeamId() {
    add("A", 0, 0);
    add("B", 1, 1);
    add("C", 0, 2); // same team as A
    assertEquals("C", roster.teammateOf("A"));
  }

  @Test
  void noTeammateWhenNobodyElseSharesTheTeam() {
    add("A", 0, 0);
    add("B", 1, 1);
    assertNull(roster.teammateOf("A"));
  }

  // ── sameTeam ────────────────────────────────────────────────────────────────
  @Test
  void sameTeamWhenTeamIdsMatch() {
    add("A", 0, 0);
    add("B", 0, 1);
    assertTrue(roster.sameTeam("A", "B"));
  }

  @Test
  void notSameTeamWhenFirstPlayerIsUnknown() {
    add("B", 0, 0);
    assertFalse(roster.sameTeam("Z", "B"));
  }

  @Test
  void notSameTeamWhenSecondPlayerIsUnknown() {
    add("A", 0, 0);
    assertFalse(roster.sameTeam("A", "Z"));
  }

  @Test
  void notSameTeamWhenAPlayerHasNoTeam() {
    add("A", null, 0);
    add("B", 0, 1);
    assertFalse(roster.sameTeam("A", "B"));
  }

  @Test
  void notSameTeamForDifferentTeams() {
    add("A", 0, 0);
    add("B", 1, 1);
    assertFalse(roster.sameTeam("A", "B"));
  }

  // ── teamMembers ─────────────────────────────────────────────────────────────
  @Test
  void listsAllMembersOfATeam() {
    add("A", 0, 0);
    add("B", 1, 1);
    add("C", 0, 2);
    List<Player> team0 = roster.teamMembers(0);
    assertEquals(2, team0.size());
    assertTrue(team0.stream().allMatch(p -> p.getTeamId() == 0));
  }

  @Test
  void emptyWhenNoPlayerIsOnTheTeam() {
    add("A", 0, 0);
    assertTrue(roster.teamMembers(9).isEmpty());
  }

  // ── nextPlayerId / previousPlayerId ─────────────────────────────────────────
  @Test
  void nextAdvancesThroughSeatOrder() {
    add("A", 0, 0);
    add("B", 1, 1);
    add("C", 0, 2);
    assertEquals("B", roster.nextPlayerId("A"));
  }

  @Test
  void nextWrapsFromTheLastSeatToTheFirst() {
    add("A", 0, 0);
    add("B", 1, 1);
    add("C", 0, 2);
    assertEquals("A", roster.nextPlayerId("C"));
  }

  @Test
  void previousStepsBackThroughSeatOrder() {
    add("A", 0, 0);
    add("B", 1, 1);
    add("C", 0, 2);
    assertEquals("A", roster.previousPlayerId("B"));
  }

  @Test
  void previousWrapsFromTheFirstSeatToTheLast() {
    add("A", 0, 0);
    add("B", 1, 1);
    add("C", 0, 2);
    assertEquals("C", roster.previousPlayerId("A"));
  }

  @Test
  void fallsBackToZeroWhenTheComputedSeatHasNoOwner() {
    add("A", 0, 0);
    add("B", 1, 2); // seat 1 is unassigned (gap), players.size() == 2
    assertEquals("0", roster.nextPlayerId("A")); // next seat = (0+1)%2 = 1 → nobody there
  }
}
