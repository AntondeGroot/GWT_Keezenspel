package adg.services;

import adg.keezen.GameRegistry;
import adg.keezen.GameSession;
import adg.keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.Player;
import com.adg.openapi.model.PositionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dev-only HTML landing pages that create pre-seeded game scenarios so you can
 * open a specific board state without playing through the game.
 *
 * <p>Usage:
 * <ol>
 *   <li>Navigate to /keezen/preview to see the scenario list.
 *   <li>Click a scenario to create a fresh game session and choose which player to open as.
 *   <li>Open multiple browser tabs for multi-player testing.
 * </ol>
 */
@RestController
@RequestMapping("/preview")
public class PreviewController {

  private record Scenario(String path, String label) {}

  private record PlayerLink(String label, String url, boolean isPrimary) {}

  private static final String[] PLAYER_NAMES = {
      "Player 1", "Player 2", "Player 3", "Player 4",
      "Player 5", "Player 6", "Player 7", "Player 8"
  };

  private static final List<Scenario> SCENARIOS = List.of(
      new Scenario("2-players", "2 players · mid-game, pawns spread on the board"),
      new Scenario("3-players", "3 players · early game"),
      new Scenario("4-players", "4 players · mid-game"),
      new Scenario("5-players", "5 players · mid-game"),
      new Scenario("8-players", "8 players · mid-game, full board with pawns everywhere"),
      new Scenario("medals",    "3 players · 1 winner with a medal, others still active"),
      new Scenario("cards",     "3 players · active player holds every special card")
  );

  // ── Listing page ──────────────────────────────────────────────────────────

  @GetMapping("")
  public ResponseEntity<String> list() {
    var rows = new StringBuilder();
    for (Scenario s : SCENARIOS) {
      rows.append("<tr>")
          .append("<td class='url'>").append(esc("/preview/" + s.path())).append("</td>")
          .append("<td>").append(esc(s.label())).append("</td>")
          .append("<td><a href='preview/").append(esc(s.path())).append("' class='btn primary'>Open</a></td>")
          .append("</tr>");
    }
    String body = "<h1>Keezen Scenario Previews</h1>"
        + "<p class='hint'>Each button creates a fresh game session &mdash; "
        + "open multiple browser tabs for multi-player testing.</p>"
        + "<table>"
        + "<thead><tr><th>Path</th><th>Scenario</th><th></th></tr></thead>"
        + "<tbody>" + rows + "</tbody>"
        + "</table>";
    return html(page("Keezen Previews", body));
  }

  // ── Scenario pages ────────────────────────────────────────────────────────

  @GetMapping("/{scenario}")
  public ResponseEntity<String> open(@PathVariable("scenario") String scenario) {
    List<PlayerLink> links = switch (scenario) {
      case "2-players" -> setup2Players();
      case "3-players" -> setup3Players();
      case "4-players" -> setup4Players();
      case "5-players" -> setup5Players();
      case "8-players" -> setup8Players();
      case "medals"    -> setupMedals();
      case "cards"     -> setupCards();
      default          -> null;
    };
    if (links == null) return ResponseEntity.notFound().build();

    String desc = SCENARIOS.stream()
        .filter(s -> s.path().equals(scenario))
        .map(Scenario::label)
        .findFirst().orElse(scenario);

    var actions = new StringBuilder();
    for (PlayerLink l : links) {
      String cls = l.isPrimary() ? "btn primary" : "btn ghost";
      actions.append("<a href='").append(esc(l.url())).append("' class='").append(cls).append("'>")
             .append(esc(l.label())).append("</a>");
    }

    String body = "<h1>Scenario: " + esc(scenario) + "</h1>"
        + "<p class='desc'>" + esc(desc) + "</p>"
        + "<div class='actions'>" + actions + "</div>"
        + "<a href='../preview' class='back'>&#8592; back to list</a>";
    return html(page("Preview: " + scenario, body));
  }

  // ── Scenario builders ─────────────────────────────────────────────────────

  private List<PlayerLink> setup2Players() {
    return setupNPlayers(2, (gs, deck) -> {
      movePawn(gs, "p0", 0, "p1",  8);
      movePawn(gs, "p0", 1, "p0",  3);
      movePawn(gs, "p1", 0, "p0", 11);
      movePawn(gs, "p1", 1, "p1",  5);
    });
  }

  private List<PlayerLink> setup3Players() {
    return setupNPlayers(3, (gs, deck) -> {
      movePawn(gs, "p0", 0, "p1",  7);
      movePawn(gs, "p0", 1, "p2", 12);
      movePawn(gs, "p1", 0, "p0",  5);
      movePawn(gs, "p1", 1, "p1", 14);
      movePawn(gs, "p2", 0, "p2",  3);
    });
  }

  private List<PlayerLink> setup4Players() {
    return setupNPlayers(4, (gs, deck) -> {
      movePawn(gs, "p0", 0, "p1",  9);
      movePawn(gs, "p0", 1, "p3",  4);
      movePawn(gs, "p1", 0, "p2",  7);
      movePawn(gs, "p1", 1, "p0", 13);
      movePawn(gs, "p1", 2, "p3", 11);
      movePawn(gs, "p2", 0, "p1", 14);
      movePawn(gs, "p3", 0, "p0",  6);
      movePawn(gs, "p3", 1, "p2",  2);
    });
  }

  private List<PlayerLink> setup5Players() {
    return setupNPlayers(5, (gs, deck) -> {
      movePawn(gs, "p0", 0, "p1",  8);
      movePawn(gs, "p0", 1, "p3",  5);
      movePawn(gs, "p1", 0, "p2", 11);
      movePawn(gs, "p1", 1, "p4",  3);
      movePawn(gs, "p2", 0, "p0", 14);
      movePawn(gs, "p2", 1, "p3",  9);
      movePawn(gs, "p2", 2, "p1",  2);
      movePawn(gs, "p3", 0, "p4",  7);
      movePawn(gs, "p4", 0, "p0",  5);
    });
  }

  private List<PlayerLink> setup8Players() {
    return setupNPlayers(8, (gs, deck) -> {
      movePawn(gs, "p0", 0, "p1",  8);  movePawn(gs, "p0", 1, "p3",  3);
      movePawn(gs, "p1", 0, "p2", 12);  movePawn(gs, "p1", 1, "p4",  0);
      movePawn(gs, "p1", 2, "p6",  7);
      movePawn(gs, "p2", 0, "p7", 14);
      movePawn(gs, "p3", 0, "p0",  5);  movePawn(gs, "p3", 1, "p1", 13);
      movePawn(gs, "p5", 0, "p5",  2);  movePawn(gs, "p5", 1, "p6",  9);
      movePawn(gs, "p5", 2, "p7",  1);
      movePawn(gs, "p6", 0, "p0", 10);  movePawn(gs, "p6", 1, "p2",  6);
      movePawn(gs, "p7", 0, "p3", 11);
    });
  }

  private List<PlayerLink> setupMedals() {
    return setupNPlayers(3, (gs, deck) -> {
      // p0 wins: all pawns in finish lane.
      for (int n = 0; n < 4; n++) movePawn(gs, "p0", n, "p0", 16 + n);
      gs.checkForWinners(gs.getWinners());
      gs.removeWinnerFromActivePlayerList();

      // Advance turn to p1.
      gs.setPlayerIdTurn("p1");
      gs.getPlayers().forEach(p -> p.setIsPlaying(p.getId().equals("p1")));

      // Seed p1 and p2 board positions.
      movePawn(gs, "p1", 0, "p2",  8);
      movePawn(gs, "p1", 1, "p0",  3);
      movePawn(gs, "p2", 0, "p1", 11);
    });
  }

  private List<PlayerLink> setupCards() {
    return setupNPlayers(3, (gs, deck) -> {
      // Two board pawns for p0 so each special card is contextually relevant
      // (Ace/King: nest pawns available; Seven/Four/Queen: board pawn to move;
      //  Jack: opponents p1 and p2 are on the normal board).
      movePawn(gs, "p0", 0, "p1", 8);
      movePawn(gs, "p0", 1, "p2", 5);
      movePawn(gs, "p1", 0, "p0", 11);
      movePawn(gs, "p2", 0, "p1",  4);

      // Clear p0's dealt hand and give every special card.
      deck.forfeitCardsForPlayer("p0");
      deck.setPlayerCard("p0", new Card().value( 1).suit(0).uuid( 1));  // Ace
      deck.setPlayerCard("p0", new Card().value( 4).suit(0).uuid( 4));  // Four
      deck.setPlayerCard("p0", new Card().value( 7).suit(0).uuid( 7));  // Seven
      deck.setPlayerCard("p0", new Card().value(11).suit(0).uuid(11));  // Jack
      deck.setPlayerCard("p0", new Card().value(12).suit(0).uuid(12));  // Queen
      deck.setPlayerCard("p0", new Card().value(13).suit(0).uuid(13));  // King
    });
  }

  // ── Infrastructure ────────────────────────────────────────────────────────

  private List<PlayerLink> setupNPlayers(
      int n, BiConsumer<GameState, adg.keezen.CardsDeckInterface> seeder) {

    String sid = "preview-" + System.nanoTime();
    GameRegistry.createNewGame(sid, "Preview", n);
    GameSession session = GameRegistry.getGame(sid);
    GameState gs = session.getGameState();

    for (int i = 0; i < n; i++) {
      gs.addPlayer(new Player("p" + i, PLAYER_NAMES[i]));
    }
    gs.start(false);
    seeder.accept(gs, session.getCardsDeck());

    String currentId = gs.getPlayerIdTurn();
    List<PlayerLink> links = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      String pid = "p" + i;
      boolean isCurrent = pid.equals(currentId);
      // Relative URL: from /preview/{scenario} one "../" goes to the app root.
      String url = "../?sessionid=" + sid + "&playerid=" + pid;
      String label = PLAYER_NAMES[i] + (isCurrent ? " — your turn" : "");
      links.add(new PlayerLink(label, url, isCurrent));
    }
    return links;
  }

  private static void movePawn(GameState gs, String playerId, int pawnNr,
      String sectionId, int tileNr) {
    PositionKey nest = new PositionKey(playerId, -1 - pawnNr);
    gs.movePawn(new Pawn(playerId, new PawnId(playerId, pawnNr),
        new PositionKey(sectionId, tileNr), nest));
  }

  // ── HTML helpers ──────────────────────────────────────────────────────────

  private static ResponseEntity<String> html(String body) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
        .body(body);
  }

  private static final String PAGE_TEMPLATE = """
      <!DOCTYPE html>
      <html lang="en">
      <head>
        <meta charset="UTF-8">
        <title>{{title}}</title>
        <style>
          *, *::before, *::after { box-sizing: border-box; }
          body    { margin: 0; padding: 2rem 1.5rem; background: #12122a; color: #e0e0e0;
                    font-family: system-ui, sans-serif; }
          h1      { margin: 0 0 .4rem; color: #fff; font-size: 1.5rem; }
          .hint   { margin: 0 0 1.5rem; color: #888; font-size: .9rem; }
          table   { border-collapse: collapse; width: 100%; max-width: 820px; }
          th      { padding: .45rem 1rem; text-align: left; color: #aaa; font-size: .75rem;
                    text-transform: uppercase; letter-spacing: .06em;
                    border-bottom: 2px solid rgba(255,255,255,.12); }
          td      { padding: .6rem 1rem; border-bottom: 1px solid rgba(255,255,255,.06);
                    vertical-align: middle; }
          tr:hover td { background: rgba(255,255,255,.04); }
          .url    { font-family: monospace; font-size: .85rem; color: gold; white-space: nowrap; }
          .btn    { display: inline-block; padding: .4rem 1.1rem; border-radius: 5px;
                    font-size: .88rem; font-weight: 700; text-decoration: none;
                    border: none; white-space: nowrap; cursor: pointer; }
          .btn:hover { opacity: .82; }
          .primary { background: gold; color: #111; }
          .ghost   { background: rgba(255,255,255,.07); color: #ccc;
                     border: 1px solid rgba(255,255,255,.18); }
          .desc   { color: #bbb; margin: 0 0 1.75rem; }
          .actions { display: flex; gap: .75rem; flex-wrap: wrap; margin-bottom: 2rem; }
          .back   { color: #888; font-size: .85rem; text-decoration: none; }
          .back:hover { color: #ccc; }
        </style>
      </head>
      <body>{{body}}</body>
      </html>
      """;

  private static String page(String title, String body) {
    return PAGE_TEMPLATE
        .replace("{{title}}", esc(title))
        .replace("{{body}}", body);
  }

  private static String esc(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
  }
}