package adg.services;

import adg.keezen.GameSession;
import adg.keezen.GameState;
import adg.processing.MoveAvailabilityChecker;
import com.adg.openapi.model.Card;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseEmitterService {

  private record PlayerEmitter(String playerId, SseEmitter emitter) {}

  private final Map<String, CopyOnWriteArrayList<PlayerEmitter>> emittersBySession =
      new ConcurrentHashMap<>();

  public SseEmitter subscribe(String sessionId, String playerId) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    CopyOnWriteArrayList<PlayerEmitter> list =
        emittersBySession.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>());
    PlayerEmitter pe = new PlayerEmitter(playerId, emitter);
    list.add(pe);
    Runnable cleanup = () -> list.remove(pe);
    emitter.onCompletion(cleanup);
    emitter.onTimeout(cleanup);
    emitter.onError(e -> cleanup.run());
    return emitter;
  }

  public void push(String sessionId, GameSession session) {
    CopyOnWriteArrayList<PlayerEmitter> list = emittersBySession.get(sessionId);
    if (list == null || list.isEmpty()) return;
    List<PlayerEmitter> dead = new ArrayList<>();
    for (PlayerEmitter pe : list) {
      try {
        GameStatePush push = buildPush(session, pe.playerId(), false);
        pe.emitter().send(
            SseEmitter.event().name("gamestate").data(push, MediaType.APPLICATION_JSON));
      } catch (IOException e) {
        dead.add(pe);
      }
    }
    list.removeAll(dead);
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