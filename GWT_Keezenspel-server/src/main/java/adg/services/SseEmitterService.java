package adg.services;

import adg.keezen.GameSession;
import adg.keezen.GameState;
import adg.processing.MoveAvailabilityChecker;
import com.adg.openapi.model.Card;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseEmitterService {

  private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);

  /**
   * Bound on how many state snapshots may queue for one slow client before the oldest are
   * dropped. Each snapshot is a full game state, so a slow client only needs the most recent
   * one — dropping stale snapshots (DiscardOldestPolicy) keeps memory bounded while still
   * delivering the latest state once the connection drains.
   */
  private static final int SEND_QUEUE_CAPACITY = 16;

  /** Each emitter gets its own single-thread sender so a slow client only delays its own stream. */
  private record PlayerEmitter(String playerId, SseEmitter emitter, ExecutorService sender) {}

  private final Map<String, CopyOnWriteArrayList<PlayerEmitter>> emittersBySession =
      new ConcurrentHashMap<>();

  /** Overridable so tests can inject an emitter whose send() blocks. */
  protected SseEmitter newEmitter() {
    return new SseEmitter(Long.MAX_VALUE);
  }

  public SseEmitter subscribe(String sessionId, String playerId) {
    SseEmitter emitter = newEmitter();
    CopyOnWriteArrayList<PlayerEmitter> list =
        emittersBySession.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>());
    PlayerEmitter pe = new PlayerEmitter(playerId, emitter, newSenderExecutor(playerId));
    list.add(pe);
    Runnable cleanup = () -> {
      list.remove(pe);
      pe.sender().shutdownNow();
    };
    emitter.onCompletion(cleanup);
    emitter.onTimeout(cleanup);
    emitter.onError(e -> cleanup.run());
    return emitter;
  }

  private static ExecutorService newSenderExecutor(String playerId) {
    return new ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue<>(SEND_QUEUE_CAPACITY),
        r -> {
          Thread t = new Thread(r, "sse-sender-" + playerId);
          t.setDaemon(true);
          return t;
        },
        new ThreadPoolExecutor.DiscardOldestPolicy());
  }

  public void push(String sessionId, GameSession session) {
    CopyOnWriteArrayList<PlayerEmitter> list = emittersBySession.get(sessionId);
    if (list == null || list.isEmpty()) return;
    for (PlayerEmitter pe : list) {
      GameStatePush snapshot;
      try {
        // Build the snapshot on the caller's (request) thread, where the game state is stable
        // and not being mutated by a concurrent move. Only the blocking network write is handed
        // off asynchronously below.
        snapshot = buildPush(session, pe.playerId(), false);
      } catch (RuntimeException e) {
        // A snapshot-building failure (e.g. an NPE deep in MoveAvailabilityChecker) must never
        // propagate: push() is called inside the move request, so an exception here would turn an
        // already-applied move into an HTTP 500 and kick the player back to the lobby. Log and
        // skip this player; the next push will retry.
        log.error("Failed to build game-state push for player {} in session {}",
            pe.playerId(), sessionId, e);
        continue;
      }
      sendAsync(list, pe, snapshot);
    }
  }

  /**
   * Hands the blocking SSE write to the emitter's own single-thread sender. This keeps a slow or
   * stalled client from blocking the move request (which would time out and look like a failed
   * move, and could exhaust the request-thread pool). Sends stay serialized and in order per
   * emitter; a client that falls too far behind has its stale snapshots dropped, and a broken
   * connection causes the emitter to be removed.
   */
  private void sendAsync(
      CopyOnWriteArrayList<PlayerEmitter> list, PlayerEmitter pe, GameStatePush snapshot) {
    try {
      pe.sender().execute(() -> {
        try {
          pe.emitter().send(
              SseEmitter.event().name("gamestate").data(snapshot, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
          drop(list, pe);
        }
      });
    } catch (RejectedExecutionException e) {
      // Sender already shut down (emitter is being cleaned up) — nothing to send.
    }
  }

  private void drop(CopyOnWriteArrayList<PlayerEmitter> list, PlayerEmitter pe) {
    list.remove(pe);
    pe.sender().shutdownNow();
    try {
      pe.emitter().complete();
    } catch (RuntimeException ignored) {
      // emitter may already be completed/errored
    }
  }

  public GameStatePush buildPush(GameSession session, String playerId, boolean omitLastMove) {
    GameState gs = session.getGameState();
    GameStatePush push = new GameStatePush();
    push.setCurrentPlayerId(gs.getPlayerIdTurn());
    push.setPawns(gs.getPawns());
    push.setPlayers(gs.getPlayers());
    push.setWinners(gs.getWinners());
    push.setVersion(gs.getVersion());
    push.setLastMoveResponse(omitLastMove ? null : session.getLastMoveResponse());

    // Public card data
    List<String> playedCardIds = session.getCardsDeck().getPlayedCards().stream()
        .map(c -> c.getSuit() + "_" + c.getValue())
        .collect(Collectors.toList());
    push.setPlayedCards(playedCardIds);
    push.setNrOfCardsPerPlayer(session.getCardsDeck().getNrOfCardsPerPlayer());

    // Private card data for this player only
    List<Card> hand = session.getCardsDeck().getCardsForPlayer(playerId);
    List<Card> safeHand = hand != null ? hand : List.of();
    push.setPlayerCards(safeHand);

    boolean canForfeit = !gs.isMustPlayIfPossible()
        || !MoveAvailabilityChecker.hasAvailableMove(gs, playerId, safeHand);
    push.setCanForfeit(canForfeit);

    if (playerId.equals(gs.getPlayerIdTurn()) && !canForfeit) {
      gs.recordMustPlayBlocked();
    }

    return push;
  }
}