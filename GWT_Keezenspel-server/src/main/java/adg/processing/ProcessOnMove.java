package adg.processing;

import static adg.util.BoardLogic.isPawnOnFinish;
import static adg.util.CardValueCheck.isJack;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.INVALID_SELECTION;
import static com.adg.openapi.model.MoveResult.PLAYER_DOES_NOT_HAVE_CARD;
import static com.adg.openapi.model.MoveType.MOVE;

import adg.keezen.GameState;
import adg.Log;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PositionKey;
import java.util.LinkedList;
import java.util.Objects;

public class ProcessOnMove {

  // ── Entry points ──────────────────────────────────────────────────────────

  public static void process(GameState gs, MoveRequest moveMessage, MoveResponse response) {
    process(gs, moveMessage, response, true);
  }

  public static void process(
      GameState gs, MoveRequest moveMessage, MoveResponse response, boolean goToNextPlayer) {
    new ProcessOnMove(gs, moveMessage, response, goToNextPlayer).execute();
  }

  // ── Instance state ────────────────────────────────────────────────────────

  private final GameState gs;
  private final MoveRequest moveMessage;
  private final MoveResponse response;
  private final boolean goToNextPlayer;

  private Pawn pawn1;
  private Card card;
  private String playerId;
  private PositionKey currentTileId;
  private String playerIdOfTile;
  private int nrSteps;
  private int next;
  private final LinkedList<PositionKey> moves = new LinkedList<>();

  private ProcessOnMove(
      GameState gs, MoveRequest moveMessage, MoveResponse response, boolean goToNextPlayer) {
    this.gs = gs;
    this.moveMessage = moveMessage;
    this.response = response;
    this.goToNextPlayer = goToNextPlayer;
  }

  // ── Execution ─────────────────────────────────────────────────────────────

  private void execute() {
    pawn1 = gs.getPawn(moveMessage.getPawn1Id());
    card = gs.getCard(moveMessage.getCardId(), moveMessage.getPlayerId());
    playerId = moveMessage.getPlayerId();

    if (!selectionIsValid()) return;
    if (!playerHasCard()) return;
    if (!pawnOwnershipIsValid()) return;

    initializeRouting();
    moves.add(currentTileId);
    response.setMoveType(MOVE);
    Log.info("GameState: OnMove: received msg: " + moveMessage);

    if (isForwardCrossSection())  { routeForwardCrossSection(); return; }
    if (isNormalRouteInSection()) { routeNormalInSection();     return; }
    if (next < 0)                 { routeBackward();            return; }
    if (next == 0)                { routeBackwardToStartTile(); return; }
    if (isPawnOnFinish(pawn1))    { routeAlreadyOnFinish();     return; }
    if (isEnteringFinish())       { routeEnteringFinish(); }
  }

  // ── Validation ────────────────────────────────────────────────────────────

  private boolean selectionIsValid() {
    if (pawn1 == null || card == null) {
      response.setResult(INVALID_SELECTION);
      return false;
    }
    if (pawn1.getCurrentTileId().getTileNr() < 0) {
      response.setResult(CANNOT_MAKE_MOVE);
      return false;
    }
    return true;
  }

  private boolean playerHasCard() {
    if (!gs.playerHasCard(playerId, card)) {
      response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return false;
    }
    return true;
  }

  private boolean pawnOwnershipIsValid() {
    if (isJack(card)) return true;
    if (moveMessage.getPawn1Id() != null
        && !Objects.equals(moveMessage.getPawn1Id().getPlayerId(), playerId)) {
      response.setResult(CANNOT_MAKE_MOVE);
      return false;
    }
    if (moveMessage.getPawn2Id() != null
        && !Objects.equals(moveMessage.getPawn2Id().getPlayerId(), playerId)) {
      response.setResult(CANNOT_MAKE_MOVE);
      return false;
    }
    return true;
  }

  // ── Routing setup ─────────────────────────────────────────────────────────

  private void initializeRouting() {
    currentTileId = pawn1.getCurrentTileId();
    playerIdOfTile = currentTileId.getPlayerId();
    nrSteps = moveMessage.getStepsPawn1();
    next = currentTileId.getTileNr() + nrSteps;
  }

  private boolean isForwardCrossSection() {
    return next > 15
        && !gs.isPawnOnLastSection(playerId, playerIdOfTile)
        && !isPawnOnFinish(pawn1);
  }

  private boolean isNormalRouteInSection() {
    return next > 0 && next <= 15 && !isPawnOnFinish(pawn1);
  }

  private boolean isEnteringFinish() {
    return next > 15 && gs.isPawnOnLastSection(playerId, playerIdOfTile);
  }

  // ── Route: forward cross-section ─────────────────────────────────────────

  private void routeForwardCrossSection() {
    Log.info("GameState: OnMove: normal route between 0,15 but could move to next section");
    addCurrentSectionLandmarksToEnd();
    PositionKey nextSectionStart = new PositionKey(gs.nextPlayerId(playerIdOfTile), 0);
    if (gs.canPassStartTile(pawn1, nextSectionStart)) {
      enterNextSection();
    } else {
      if (!reverseBackInCurrentSection()) return;
    }
    finalizeMoveToPosition(new PositionKey(playerIdOfTile, next));
  }

  private void addCurrentSectionLandmarksToEnd() {
    if (currentTileId.getTileNr() < 1)  moves.add(new PositionKey(currentTileId.getPlayerId(), 1));
    if (currentTileId.getTileNr() < 7)  moves.add(new PositionKey(currentTileId.getPlayerId(), 7));
    if (currentTileId.getTileNr() < 13) moves.add(new PositionKey(currentTileId.getPlayerId(), 13));
    if (currentTileId.getTileNr() < 15) moves.add(new PositionKey(currentTileId.getPlayerId(), 15));
  }

  private void enterNextSection() {
    Log.info("GameState: OnMove: normal route can move to the next section");
    next = next % 16;
    playerIdOfTile = gs.nextPlayerId(playerIdOfTile);
    if (next > 1) moves.add(new PositionKey(playerIdOfTile, 1));
    if (next > 7) moves.add(new PositionKey(playerIdOfTile, 7));
  }

  private boolean reverseBackInCurrentSection() {
    Log.info("GameState: OnMove: normal route is blocked by a start tile, move backwards");
    if (gs.isExactMoveRequired()) {
      response.setResult(CANNOT_MAKE_MOVE);
      return false;
    }
    next = 15 - next % 15;
    moves.add(new PositionKey(playerIdOfTile, 15));
    if (next < 13) moves.add(new PositionKey(playerIdOfTile, 13));
    if (next < 7)  moves.add(new PositionKey(playerIdOfTile, 7));
    return true;
  }

  // ── Route: normal within section ─────────────────────────────────────────

  private void routeNormalInSection() {
    Log.info("GameState: OnMove: normal route between 0,15");
    addWaypointsWithinSection();
    finalizeMoveToPosition(new PositionKey(playerIdOfTile, next));
  }

  private void addWaypointsWithinSection() {
    if (nrSteps > 0) {
      if (next > 1  && currentTileId.getTileNr() < 1)  moves.add(new PositionKey(playerIdOfTile, 1));
      if (next > 7  && currentTileId.getTileNr() < 7)  moves.add(new PositionKey(playerIdOfTile, 7));
      if (next > 13 && currentTileId.getTileNr() < 13) moves.add(new PositionKey(playerIdOfTile, 13));
    } else {
      if (next < 13 && currentTileId.getTileNr() > 13) moves.add(new PositionKey(playerIdOfTile, 13));
      if (next < 7  && currentTileId.getTileNr() > 7)  moves.add(new PositionKey(playerIdOfTile, 7));
      if (next < 1  && currentTileId.getTileNr() > 1)  moves.add(new PositionKey(playerIdOfTile, 1));
    }
  }

  // ── Route: backward past section ─────────────────────────────────────────

  private void routeBackward() {
    Log.info("GameState: OnMove: pawn goes backwards");
    if (currentTileId.getTileNr() > 1) moves.add(new PositionKey(playerIdOfTile, 1));
    PositionKey ownStartTile = new PositionKey(playerIdOfTile, 0);
    if (gs.canPassStartTile(pawn1, ownStartTile)) {
      crossIntoPreviousSection();
    } else {
      if (!reverseForwardFromStartTile()) return;
    }
    finalizeMoveToPosition(new PositionKey(playerIdOfTile, next));
  }

  private void crossIntoPreviousSection() {
    next = 16 + next;
    playerIdOfTile = gs.previousPlayerId(playerIdOfTile);
    if (next < 13) moves.add(new PositionKey(playerIdOfTile, 13));
  }

  private boolean reverseForwardFromStartTile() {
    Log.info("GameState: OnMove: pawn wants to go backwards but is blocked by a start tile, goes forwards");
    if (gs.isExactMoveRequired()) {
      response.setResult(CANNOT_MAKE_MOVE);
      return false;
    }
    next = -next + 2;
    return true;
  }

  // ── Route: backward landing exactly on start tile ────────────────────────

  private void routeBackwardToStartTile() {
    Log.info("GameState: OnMove: pawn ends exactly on start tile");
    if (currentTileId.getTileNr() > 1) moves.add(new PositionKey(playerIdOfTile, 1));
    PositionKey startTile = new PositionKey(playerIdOfTile, 0);
    if (gs.canMoveToTile(pawn1, startTile)) {
      landOnTile(startTile);
    } else if (startTileIsBlockedByOwnPawn(startTile)) {
      response.setResult(CANNOT_MAKE_MOVE);
    } else {
      landBeyondBlockadedStartTile();
    }
  }

  private boolean startTileIsBlockedByOwnPawn(PositionKey startTile) {
    return !gs.tileIsABlockade(startTile)
        && gs.cannotMoveToTileBecauseSamePlayer(pawn1, startTile);
  }

  private void landBeyondBlockadedStartTile() {
    PositionKey tile2 = new PositionKey(playerIdOfTile, 2);
    if (gs.canMoveToTile(pawn1, tile2)) {
      landOnTile(tile2);
    } else {
      response.setResult(CANNOT_MAKE_MOVE);
    }
  }

  // ── Route: pawn already on finish ────────────────────────────────────────

  private void routeAlreadyOnFinish() {
    Log.info("GameState: OnMove: pawn is already on the finish");
    if (gs.isPawnTightlyClosedIn(pawn1, currentTileId)) {
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }
    if (gs.isPawnLooselyClosedIn(pawn1, currentTileId)) {
      if (gs.isExactMoveRequired()) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
      executePingPongMove();
      return;
    }
    executeFinishMoveWithOvershootCheck();
  }

  private void executePingPongMove() {
    LinkedList<PositionKey> pingpongmoves =
        new LinkedList<>(gs.pingpongMove(pawn1, currentTileId, nrSteps));
    moves.clear();
    moves.addAll(pingpongmoves);
    response.setMovePawn1(moves);
    gs.processMove(pawn1, pingpongmoves.getLast(), moveMessage, response, goToNextPlayer);
  }

  private void executeFinishMoveWithOvershootCheck() {
    PositionKey targetTileId = gs.moveAndCheckEveryTile(pawn1, currentTileId, nrSteps);
    if (nrSteps > 0) {
      int highestReachable = gs.checkHighestTileNrYouCanMoveTo(pawn1, currentTileId, nrSteps);
      if (highestReachable > targetTileId.getTileNr()) {
        if (!addFinishBounceWaypoint(highestReachable)) return;
      }
    }
    addFinishReverseWaypoints(targetTileId);
    moves.add(targetTileId);
    response.setMovePawn1(moves);
    gs.processMove(pawn1, targetTileId, moveMessage, response, goToNextPlayer);
  }

  private boolean addFinishBounceWaypoint(int highestReachable) {
    if (gs.isExactMoveRequired()) {
      response.setResult(CANNOT_MAKE_MOVE);
      return false;
    }
    Log.info("GameState: OnMove: pawn moves out of the finish");
    moves.add(new PositionKey(playerIdOfTile, highestReachable));
    return true;
  }

  private void addFinishReverseWaypoints(PositionKey targetTile) {
    if (targetTile.getTileNr() < 15) moves.add(new PositionKey(targetTile.getPlayerId(), 15));
    if (targetTile.getTileNr() < 13) moves.add(new PositionKey(targetTile.getPlayerId(), 13));
    if (targetTile.getTileNr() < 7)  moves.add(new PositionKey(targetTile.getPlayerId(), 7));
  }

  // ── Route: entering finish from last section ──────────────────────────────

  private void routeEnteringFinish() {
    Log.info("GameState: OnMove: pawn is on last section and goes into finish");
    addForwardLandmarksIntoFinish();
    PositionKey targetTileId = gs.moveAndCheckEveryTile(pawn1, currentTileId, nrSteps);
    int highestReachable = gs.checkHighestTileNrYouCanMoveTo(pawn1, currentTileId, nrSteps);
    if (highestReachable > targetTileId.getTileNr()) {
      if (!addEnteringFinishOvershootWaypoints(targetTileId, highestReachable)) return;
    }
    if (gs.cannotMoveToTileBecauseSamePlayer(pawn1, targetTileId)) {
      gs.clearResponse(response);
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }
    moves.add(targetTileId);
    response.setMovePawn1(moves);
    gs.processMove(pawn1, targetTileId, moveMessage, response, goToNextPlayer);
  }

  private void addForwardLandmarksIntoFinish() {
    if (currentTileId.getTileNr() < 7)  moves.add(new PositionKey(playerIdOfTile, 7));
    if (currentTileId.getTileNr() < 13) moves.add(new PositionKey(playerIdOfTile, 13));
    if (currentTileId.getTileNr() < 15) moves.add(new PositionKey(playerIdOfTile, 15));
  }

  private boolean addEnteringFinishOvershootWaypoints(
      PositionKey targetTileId, int highestReachable) {
    if (gs.isExactMoveRequired()) {
      response.setResult(CANNOT_MAKE_MOVE);
      return false;
    }
    if (highestReachable > 15) {
      moves.add(new PositionKey(gs.nextPlayerId(playerIdOfTile), highestReachable));
      if (targetTileId.getTileNr() < 15) {
        moves.add(new PositionKey(targetTileId.getPlayerId(), 15));
      }
    }
    if (targetTileId.getTileNr() < 13) moves.add(new PositionKey(targetTileId.getPlayerId(), 13));
    if (targetTileId.getTileNr() < 7)  moves.add(new PositionKey(targetTileId.getPlayerId(), 7));
    return true;
  }

  // ── Common helpers ────────────────────────────────────────────────────────

  private void finalizeMoveToPosition(PositionKey nextTileId) {
    moves.add(nextTileId);
    if (gs.canMoveToTile(pawn1, nextTileId)) {
      response.setMovePawn1(moves);
      gs.processMove(pawn1, nextTileId, moveMessage, response, goToNextPlayer);
    } else {
      response.setResult(CANNOT_MAKE_MOVE);
    }
  }

  private void landOnTile(PositionKey targetTile) {
    moves.add(targetTile);
    response.setMovePawn1(moves);
    gs.processMove(pawn1, targetTile, moveMessage, response, goToNextPlayer);
  }
}