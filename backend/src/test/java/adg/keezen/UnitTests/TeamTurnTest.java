package adg.keezen.UnitTests;

import static adg.keezen.UnitTests.GameStateUtil.place4PawnsOnFinish;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adg.keezen.GameSession;
import adg.keezen.GameState;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Team play, step 4: the hand-off rule and team win.
 *
 * <p>Teams are pairs of opposite seats (4 players → team 0 = players 0 &amp; 2, team 1 = 1 &amp; 3).
 * You may move your own pawns freely, a teammate's only once all your own are home, and never an
 * opponent's. A player who finishes their own pawns first isn't placed — they keep playing their
 * teammate's pawns — and the pair is placed together only when both members are home.
 */
class TeamTurnTest {

  private static GameState teamGame() {
    GameState gs = new GameSession().getGameState();
    gs.setTeamPlay(true);
    for (int i = 0; i < 4; i++) {
      gs.addPlayer(new Player(String.valueOf(i), String.valueOf(i)));
    }
    gs.start(false); // no shuffle → seat == insertion order
    return gs;
  }

  private static Pawn pawnOf(GameState gs, String playerId) {
    return gs.getPawns().stream()
        .filter(p -> playerId.equals(p.getPlayerId()))
        .findFirst()
        .orElseThrow();
  }

  private static Player player(GameState gs, String id) {
    return gs.getPlayers().stream()
        .filter(p -> id.equals(p.getId()))
        .findFirst()
        .orElseThrow();
  }

  // ── hand-off gate ──────────────────────────────────────────────────────────

  @Test
  void ownPawn_isAlwaysAllowed() {
    GameState gs = teamGame();
    assertTrue(gs.isTeamMoveAllowed("0", pawnOf(gs, "0")));
  }

  @Test
  void teammatePawn_isBlockedUntilYourOwnPawnsAreHome() {
    GameState gs = teamGame(); // player 2 is 0's teammate
    assertFalse(gs.isTeamMoveAllowed("0", pawnOf(gs, "2")), "can't play a teammate's pawn yet");
    place4PawnsOnFinish(gs, "0");
    assertTrue(gs.isTeamMoveAllowed("0", pawnOf(gs, "2")), "own pawns home → may play teammate's");
  }

  @Test
  void opponentPawn_isNeverAllowed() {
    GameState gs = teamGame(); // player 1 is an opponent of 0
    assertFalse(gs.isTeamMoveAllowed("0", pawnOf(gs, "1")));
    place4PawnsOnFinish(gs, "0");
    assertFalse(gs.isTeamMoveAllowed("0", pawnOf(gs, "1")), "finishing your own doesn't unlock opponents");
  }

  @Test
  void mayControlPawn_ownAlways_teammateOnceHome_opponentNever() {
    GameState gs = teamGame();
    assertTrue(gs.mayControlPawn("0", pawnOf(gs, "0")), "your own pawn");
    assertFalse(gs.mayControlPawn("0", pawnOf(gs, "2")), "teammate's pawn before your own are home");
    assertFalse(gs.mayControlPawn("0", pawnOf(gs, "1")), "an opponent's pawn");
    place4PawnsOnFinish(gs, "0");
    assertTrue(gs.mayControlPawn("0", pawnOf(gs, "2")), "teammate's pawn once your own are home");
    assertFalse(gs.mayControlPawn("0", pawnOf(gs, "1")), "still never an opponent's");
  }

  @Test
  void mayControlPawn_teamsOff_onlyYourOwn() {
    GameState gs = new GameSession().getGameState();
    for (int i = 0; i < 4; i++) {
      gs.addPlayer(new Player(String.valueOf(i), String.valueOf(i)));
    }
    gs.start(false); // teams off
    assertTrue(gs.mayControlPawn("0", pawnOf(gs, "0")));
    assertFalse(gs.mayControlPawn("0", pawnOf(gs, "1")), "no teams → opponents' pawns off-limits");
  }

  @Test
  void teamsOff_allowsAnyPawn() {
    GameState gs = new GameSession().getGameState();
    for (int i = 0; i < 4; i++) {
      gs.addPlayer(new Player(String.valueOf(i), String.valueOf(i)));
    }
    gs.start(false); // teamPlay stays off
    assertTrue(gs.isTeamMoveAllowed("0", pawnOf(gs, "1")));
  }

  // ── team win ─────────────────────────────────────────────────────────────

  @Test
  void finishingYourOwnPawns_doesNotPlaceYou_whileYourTeammatePlaysOn() {
    GameState gs = teamGame();
    place4PawnsOnFinish(gs, "0");
    gs.checkForWinners(gs.getWinners());
    assertEquals(Integer.valueOf(-1), player(gs, "0").getPlace(), "no place yet — teammate 2 isn't home");
    assertTrue(gs.getWinners().isEmpty());
    assertTrue(player(gs, "0").getIsActive(), "you stay active to play your teammate's pawns");
  }

  @Test
  void theTeamIsPlacedTogether_whenBothMembersAreHome() {
    GameState gs = teamGame();
    place4PawnsOnFinish(gs, "0");
    place4PawnsOnFinish(gs, "2");
    gs.checkForWinners(gs.getWinners());
    assertEquals(Integer.valueOf(1), player(gs, "0").getPlace());
    assertEquals(Integer.valueOf(1), player(gs, "2").getPlace());
    assertTrue(gs.getWinners().containsAll(List.of("0", "2")));
    assertEquals(Integer.valueOf(-1), player(gs, "1").getPlace(), "the other team is untouched");
  }
}
