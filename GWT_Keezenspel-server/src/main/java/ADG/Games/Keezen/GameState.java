package ADG.Games.Keezen;

import static ADG.util.BoardLogic.isPawnOnFinish;
import static ADG.util.PlayerStatus.hasFinished;
import static ADG.util.PlayerStatus.setActive;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.CAN_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.PLAYER_DOES_NOT_HAVE_CARD;

import ADG.Log;
import ADG.Processing.ProcessOnBoard;
import ADG.Processing.ProcessOnMove;
import ADG.Processing.ProcessOnSplit;
import ADG.Processing.ProcessOnSwitch;
import ADG.util.PlayerStatus;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.Player;
import com.adg.openapi.model.PositionKey;
import com.adg.openapi.model.TempMessageType;
import jakarta.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class GameState {

  private ArrayList<Pawn> pawns = new ArrayList<>();
  private String playerIdTurn;
  private String playerIdStartingRound;
  private final ArrayList<Player> players = new ArrayList<>();
  private final HashMap<String, Integer> playerColors =
      new HashMap<>(); // to map a player UUID to an int for player Colors
  private final ArrayList<String> activePlayers = new ArrayList<>();
  private final ArrayList<String> winners = new ArrayList<>();
  private final HashSet<String> leavers = new HashSet<>();
  private final int MAX_PLAYERS = 8;
  private final CardsDeckInterface cardsDeck;
  private int animationSpeed;
  private boolean exactMoveRequired = false;
  private Boolean hasStarted = false;
  private final AtomicLong version =
      new AtomicLong(0); // to make it compatible with javascript as it doesn't do int64 well!

  public GameState(CardsDeckInterface cardsDeck) {
    this.cardsDeck = cardsDeck;
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  public void start() {
    start(true);
  }

  public void start(boolean shuffle) {
    hasStarted = true;
    if (shuffle) shufflePlayers();
    assignPlayerInts();
    activateAllPlayers();
    initializePawns();
    initializeCards();
    initializeTurn();
  }

  public void stop() {
    hasStarted = false;
    pawns.clear();
    players.clear();
    playerColors.clear();
    activePlayers.clear();
    winners.clear();
  }

  public void reset() {
    resetWinners();
    resetPawnPositions();
    resetActivePlayers();
    resetCards();
    resetTurn();
    version.incrementAndGet();
  }

  public void tearDown() {
    pawns = new ArrayList<>();
    playerIdTurn = "0";
    players.clear();
    activePlayers.clear();
    winners.clear();
    playerIdStartingRound = "0";
  }

  // ── Start helpers ─────────────────────────────────────────────────────────

  private void assignPlayerInts() {
    // playerInts determine the order in which the players play and
    // which colors they have, this should be assinged after shuffling.
    int i = 0;
    for (Player player : players) {
      player.setPlayerInt(i++);
    }
  }

  private void shufflePlayers() {
    Collections.shuffle(players);
  }

  private void initializeTurn() {
    playerIdTurn = players.getFirst().getId();
    playerIdStartingRound = playerIdTurn;
    players.getFirst().setIsPlaying(true);
  }

  private void activateAllPlayers() {
    for (Player p : players) {
      p.setIsActive(true);
    }
  }

  private void initializePawns() {
    pawns = new ArrayList<>();
    int playerInt = 0;
    for (Player player : players) {
      activePlayers.add(player.getId());
      playerColors.put(player.getId(), playerInt);
      for (int pawnNr = 0; pawnNr < 4; pawnNr++) {
        PositionKey nestPosition = new PositionKey(player.getId(), -1 - pawnNr);
        pawns.add(new Pawn(player.getId(), new PawnId(player.getId(), pawnNr), nestPosition, nestPosition));
      }
      playerInt++;
    }
  }

  private void initializeCards() {
    cardsDeck.addPlayers(players);
    cardsDeck.shuffleIfFirstRound();
    cardsDeck.dealCards();
  }

  // ── Reset helpers ─────────────────────────────────────────────────────────

  private void resetWinners() {
    winners.clear();
    for (Player player : players) {
      player.setPlace(-1);
    }
  }

  private void resetPawnPositions() {
    for (Pawn p : pawns) {
      p.setCurrentTileId(p.getNestTileId());
    }
  }

  private void resetCards() {
    cardsDeck.reset();
    cardsDeck.addPlayers(players);
    cardsDeck.shuffleIfFirstRound();
    cardsDeck.dealCards();
  }

  private void resetTurn() {
    if (!players.isEmpty()) {
      playerIdTurn = players.getFirst().getId();
      playerIdStartingRound = playerIdTurn;
      setPlayingPlayer(playerIdTurn);
    }
  }

  // ── Player management ─────────────────────────────────────────────────────

  public void addPlayer(Player player) {
    if (!players.contains(player) && players.size() < MAX_PLAYERS) {
      player.setIsActive(true);
      player.setPlace(-1);
      player.isPlaying(false);
      players.add(player);
      version.incrementAndGet();
    }
  }

  public void processLeaveGame(String playerId) {
    cardsDeck.forfeitCardsForPlayer(playerId);
    leavers.add(playerId);
    resetPawnsForPlayer(playerId);
    deactivateAndStopPlayingPlayer(playerId);
    if (allActivePlayersExhausted()) {
      startNewRound();
    } else {
      activePlayers.remove(playerId);
      if (playerId.equals(playerIdTurn)) {
        nextActivePlayer();
      }
    }
    version.incrementAndGet();
  }

  public void forfeitPlayer(String playerId) {
    deactivatePlayerById(playerId);
    if (allActivePlayersExhausted()) {
      startNewRound();
    } else {
      activePlayers.remove(playerId);
      nextActivePlayer();
    }
    version.incrementAndGet();
  }

  public void processOnForfeit(String playerId) {
    cardsDeck.forfeitCardsForPlayer(playerId);
    forfeitPlayer(playerId);
    version.incrementAndGet();
  }

  // ── Player management helpers ─────────────────────────────────────────────

  private void deactivatePlayerById(String playerId) {
    Player player = findPlayerById(playerId);
    if (player != null) {
      PlayerStatus.setInactive(player);
    }
  }

  private void resetPawnsForPlayer(String playerId) {
    for (Pawn pawn : pawns) {
      if (playerId.equals(pawn.getPlayerId())) {
        pawn.setCurrentTileId(pawn.getNestTileId());
      }
    }
  }

  private void deactivateAndStopPlayingPlayer(String playerId) {
    Player player = findPlayerById(playerId);
    if (player != null) {
      PlayerStatus.setInactive(player);
      player.setIsPlaying(false);
    }
  }

  private boolean allActivePlayersExhausted() {
    return players.stream().noneMatch(Player::getIsActive);
  }

  private void startNewRound() {
    resetActivePlayers();
    cardsDeck.shuffleIfFirstRound();
    cardsDeck.dealCards();
    nextRoundPlayer();
  }

  public void resetActivePlayers() {
    activePlayers.clear();
    for (Player player : players) {
      if (!hasFinished(player) && !leavers.contains(player.getId())) {
        setActive(player);
        activePlayers.add(player.getId());
      }
    }
  }

  public boolean allPlayersHaveLeft() {
    return !players.isEmpty() && players.stream().allMatch(p -> leavers.contains(p.getId()));
  }

  // ── Turn management ───────────────────────────────────────────────────────

  /**
   * Plays the card and advances the game turn. Call this after physically moving pawns.
   * When goToNextPlayer is false (split first pawn) the card is consumed but the turn is not yet
   * advanced; the second processOnMove call with goToNextPlayer=true will advance it.
   */
  public void finishTurn(String playerId, Card card, boolean goToNextPlayer) {
    boolean noCardsLeft = cardsDeck.playerPlaysCard(playerId, card);
    if (goToNextPlayer) {
      advanceTurnAfterPlay(playerId, noCardsLeft);
      version.incrementAndGet();
    }
  }

  public void removeWinnerFromActivePlayerList() {
    for (String winnerId : winners) {
      activePlayers.remove(winnerId);
    }
  }

  // ── Turn management helpers ───────────────────────────────────────────────

  private void advanceTurnAfterPlay(String playerId, boolean noCardsLeft) {
    if (noCardsLeft) {
      forfeitPlayer(playerId);
    } else {
      nextActivePlayer();
    }
    checkForWinners(winners);
    removeWinnerFromActivePlayerList();
    if (activePlayers.isEmpty() && winners.size() < players.size()) {
      startNewRound();
    }
  }

  private void nextRoundPlayer() {
    playerIdTurn = nextPlayerId(playerIdStartingRound);
    playerIdStartingRound = playerIdTurn;
    if (!activePlayers.isEmpty() && !activePlayers.contains(playerIdTurn)) {
      nextRoundPlayer();
    }
    // todo: check if all players have finished
    setPlayingPlayer(playerIdTurn);
  }

  private void nextActivePlayer() {
    playerIdTurn = nextPlayerId(playerIdTurn);
    if (!activePlayers.isEmpty() && !activePlayers.contains(playerIdTurn)) {
      nextActivePlayer();
    } else if (activePlayers.contains(playerIdTurn)) {
      setPlayingPlayer(playerIdTurn);
    }
    // If activePlayers is empty, no one is set as playing (game is between rounds or over)
  }

  private void setPlayingPlayer(String playerId) {
    for (Player player : players) {
      player.setIsPlaying(player.getId().equals(playerId));
    }
  }

  // ── Move processing ───────────────────────────────────────────────────────

  public void processOnMove(MoveRequest moveMessage, MoveResponse response) {
    ProcessOnMove.process(this, moveMessage, response);
  }

  public void processOnMove(MoveRequest moveMessage, MoveResponse response, boolean goToNextPlayer) {
    ProcessOnMove.process(this, moveMessage, response, goToNextPlayer);
  }

  public void processOnSplit(MoveRequest moveMessage, MoveResponse response) {
    ProcessOnSplit.process(this, moveMessage, response);
  }

  public void processOnSwitch(MoveRequest moveMessage, MoveResponse moveResponse) {
    ProcessOnSwitch.process(this, moveMessage, moveResponse);
  }

  public void processOnBoard(MoveRequest moveMessage, MoveResponse response) {
    ProcessOnBoard.process(this, moveMessage, response);
  }

  public void processMove(
      Pawn pawn, PositionKey targetTileId, MoveRequest moveMessage, MoveResponse response) {
    processMove(pawn, targetTileId, moveMessage, response, true);
  }

  public void processMove(
      Pawn pawn0,
      PositionKey targetTileId,
      MoveRequest moveMessage,
      MoveResponse response,
      boolean goToNextPlayer) {

    String playerId = moveMessage.getPlayerId();
    Card card = getCard(moveMessage.getCardId(), moveMessage.getPlayerId());

    if (cannotMoveToTileBecauseSamePlayer(pawn0, targetTileId)) {
      clearResponse(response);
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }
    if (!cardsDeck.playerHasCard(playerId, card)) {
      clearResponse(response);
      response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return;
    }

    handleKillIfPresent(pawn0, targetTileId, moveMessage, response);

    response.setPawn1(pawn0);
    if (isMakeMove(moveMessage)) {
      movePawn(new Pawn(pawn0.getPlayerId(), pawn0.getPawnId(), targetTileId, pawn0.getNestTileId()));
      // goToNextPlayer is false only for the first pawn of a SPLIT move; the second call advances
      finishTurn(playerId, card, goToNextPlayer);
    }

    response.setResult(CAN_MAKE_MOVE);
    Log.info("GameState: pawn moves to " + targetTileId + ", with response " + response);
  }

  // ── Move helpers ──────────────────────────────────────────────────────────

  private void handleKillIfPresent(
      Pawn pawn0, PositionKey targetTileId, MoveRequest moveMessage, MoveResponse response) {
    Pawn pawnOnTarget = getPawn(targetTileId);
    if (pawnOnTarget == null) return;
    if (Objects.equals(pawnOnTarget.getPlayerId(), pawn0.getPlayerId())) return;

    response.setPawn1(pawn0);
    response.setPawn2(null);
    LinkedList<PositionKey> killMove = new LinkedList<>();
    killMove.add(targetTileId);
    killMove.add(pawnOnTarget.getNestTileId());
    response.setPawnKilledByPawn1(pawnOnTarget);
    response.setMovePawnKilledByPawn1(killMove);
    if (isMakeMove(moveMessage)) {
      movePawn(new Pawn(
          pawnOnTarget.getPlayerId(),
          pawnOnTarget.getPawnId(),
          pawnOnTarget.getNestTileId(),
          pawnOnTarget.getNestTileId()));
    }
  }

  // ── Winner tracking ───────────────────────────────────────────────────────

  public void checkForWinners(ArrayList<String> winners) {
    for (Player player : players) {
      if (!winners.contains(player.getId()) && hasAllPawnsOnFinish(player.getId())) {
        recordWinner(player, winners);
      }
    }
  }

  private boolean hasAllPawnsOnFinish(String playerId) {
    long finishedCount = pawns.stream()
        .filter(p -> playerId.equals(p.getPlayerId()))
        .filter(p -> isPawnOnFinish(p))
        .count();
    return finishedCount == 4;
  }

  private void recordWinner(Player player, ArrayList<String> winners) {
    player.setPlace(winners.size() + 1);
    winners.add(player.getId());
    cardsDeck.forfeitCardsForPlayer(player.getId());
    player.setIsPlaying(false);
    player.setIsActive(false);
    version.incrementAndGet();
  }

  // ── Board logic ───────────────────────────────────────────────────────────

  public boolean isPawnOnLastSection(String playerId, String sectionId) {
    return sectionId.equals(previousPlayerId(playerId));
  }

  public boolean isPawnLooselyClosedIn(Pawn pawn, PositionKey tileId) {
    int tileNr = tileId.getTileNr();
    String playerId = pawn.getPlayerId();

    if (tileNr <= 16) {
      return false;
    }

    for (int i = tileId.getTileNr(); i > 16; i--) {
      if (!canMoveToTile(pawn, new PositionKey(playerId, i - 1))) {
        return true;
      }
    }

    return false;
  }

  public boolean isPawnTightlyClosedIn(Pawn pawn, PositionKey tileId) {
    String playerId = pawn.getPlayerId();

    if (tileId.getTileNr() == 19 && !canMoveToTile(pawn, new PositionKey(playerId, 18))) {
      return true;
    }
    if (tileId.getTileNr() == 18
        && !canMoveToTile(pawn, new PositionKey(playerId, 19))
        && !canMoveToTile(pawn, new PositionKey(playerId, 17))) {
      return true;
    }
    if (tileId.getTileNr() == 17
        && !canMoveToTile(pawn, new PositionKey(playerId, 18))
        && !canMoveToTile(pawn, new PositionKey(playerId, 16))) {
      return true;
    }

    return false;
  }

  private boolean isPawnClosedInFromBehind(Pawn pawn, PositionKey tileId) {
    int tileNr = tileId.getTileNr();
    while (tileNr > 15) {
      if (!canMoveToTile(pawn, new PositionKey(pawn.getPlayerId(), tileNr - 1))) {
        return true;
      }
      tileNr--;
    }
    return false;
  }

  /**
   * @param selectedPawn
   * @param nextTileId @Return True if it ends on own position @Return False it it ends on own other
   *     pawn of same player @Return False if it ends on blockaded starting tile
   */
  public boolean canMoveToTile(Pawn selectedPawn, PositionKey nextTileId) {
    if (nextTileId.getTileNr() > 19) {
      return false;
    }
    Pawn pawn = getPawn(nextTileId);
    if (pawn != null) {
      Log.info("found pawn on start tile: " + pawn);
      if (pawn.getPawnId().equals(selectedPawn.getPawnId())) {
        return true;
      }
      if (Objects.equals(pawn.getPlayerId(), selectedPawn.getPlayerId())) {
        return false;
      }
      if (Objects.equals(pawn.getPlayerId(), nextTileId.getPlayerId())
          && nextTileId.getTileNr() == 0) {
        return false;
      }
    }
    return true;
  }

  public boolean cannotMoveToTileBecauseSamePlayer(Pawn selectedPawn, PositionKey nextTileId) {
    Pawn pawn = getPawn(nextTileId);
    if (pawn != null) {
      if (Objects.equals(pawn.getPlayerId(), selectedPawn.getPlayerId())
          && !pawn.getPawnId().equals(selectedPawn.getPawnId())) {
        return true;
      }
    }
    return false;
  }

  public boolean canPassStartTile(Pawn selectedPawn, PositionKey tileId) {
    Pawn pawnOnTile = getPawn(tileId);
    if (pawnOnTile == null) {
      return true;
    }
    if (selectedPawn.getPawnId().equals(pawnOnTile.getPawnId())) {
      return true;
    }
    if (Objects.equals(pawnOnTile.getPlayerId(), tileId.getPlayerId())) {
      return false;
    }
    return true;
  }

  public boolean tileIsABlockade(PositionKey selectedStartTile) {
    Pawn pawnOnStart = getPawn(selectedStartTile);
    if (pawnOnStart == null) {
      return false;
    }
    return Objects.equals(pawnOnStart.getPawnId().getPlayerId(), selectedStartTile.getPlayerId());
  }

  public int checkHighestTileNrYouCanMoveTo(Pawn pawn, PositionKey tileId, int nrSteps) {
    int direction = 1;
    int tileNrToCheck = tileId.getTileNr();

    if (nrSteps < 0) {
      direction = -1;
      nrSteps = -nrSteps;
    }

    for (int i = 0; i < nrSteps; i++) {
      tileNrToCheck = tileNrToCheck + direction;
      if (!canMoveToTile(pawn, new PositionKey(pawn.getPlayerId(), tileNrToCheck))) {
        return tileNrToCheck - 1;
      }
    }
    return tileNrToCheck;
  }

  public ArrayList<PositionKey> pingpongMove(Pawn pawn, PositionKey tileId, int nrSteps) {
    // it is already guaranteed that a pawn is loosely closed in on the finish tiles
    ArrayList<PositionKey> moves = new ArrayList<>();
    int direction = 1;
    int tileNrToCheck = tileId.getTileNr();
    moves.add(tileId);
    if (nrSteps < 0) {
      direction = -1;
      nrSteps = Math.abs(nrSteps);
    }

    for (int i = 0; i < nrSteps; i++) {
      tileNrToCheck = tileNrToCheck + direction;
      if (!canMoveToTile(pawn, new PositionKey(pawn.getPlayerId(), tileNrToCheck))) {
        moves.add(new PositionKey(pawn.getPlayerId(), tileNrToCheck - direction));
        direction = -direction;
        tileNrToCheck = tileNrToCheck + 2 * direction;
      }
    }
    moves.add(new PositionKey(pawn.getPlayerId(), tileNrToCheck));

    // an extra check to see if the first two moves are identical. this can happen when you do -4
    // steps and are closed in from behind or try to move forward but are blocked that way.
    if (moves.size() >= 2 && moves.get(0).equals(moves.get(1))) {
      moves.removeFirst();
    }
    return moves;
  }

  public PositionKey moveAndCheckEveryTile(Pawn pawn, PositionKey tileId, int nrSteps) {
    int direction = 1;
    int tileNrToCheck = tileId.getTileNr();

    if (nrSteps < 0) {
      direction = -1;
      nrSteps = -nrSteps;
    }

    for (int i = 0; i < nrSteps; i++) {
      tileNrToCheck = tileNrToCheck + direction;
      if (tileNrToCheck > 15) { // only check tiles when they are on the finish
        if (!canMoveToTile(pawn, new PositionKey(pawn.getPlayerId(), tileNrToCheck))) {
          direction = -direction;
          tileNrToCheck = tileNrToCheck + 2 * direction;
        }
      }
    }

    if (tileNrToCheck <= 15) { // when back on the last section, change the playerId of the section
      return new PositionKey(previousPlayerId(pawn.getPlayerId()), tileNrToCheck);
    }

    return new PositionKey(pawn.getPlayerId(), tileNrToCheck);
  }

  // ── Player navigation ─────────────────────────────────────────────────────

  public String nextPlayerId(String playerId) {
    int playerInt = playerColors.get(playerId);
    return nextPlayerId(playerInt);
  }

  private String nextPlayerId(int playerInt) {
    int nextPlayerInt = (playerInt + 1) % players.size();
    return playerColors.entrySet().stream()
        .filter(entry -> entry.getValue().equals(nextPlayerInt))
        .map(HashMap.Entry::getKey)
        .findFirst()
        .orElse("0");
  }

  public String previousPlayerId(String playerId) {
    int playerInt = playerColors.get(playerId);
    int previousPlayerInt = (playerInt + players.size() - 1) % players.size();
    return playerColors.entrySet().stream()
        .filter(entry -> entry.getValue().equals(previousPlayerInt))
        .map(HashMap.Entry::getKey)
        .findFirst()
        .orElse("0");
  }

  // ── Pawn helpers ──────────────────────────────────────────────────────────

  public void movePawn(Pawn selectedPawn) {
    // set a pawn's location without triggering any validation
    // public for testing purposes
    for (Pawn pawn : pawns) {
      if (pawn.getPawnId().equals(selectedPawn.getPawnId())) { // equals only looks at pawnId
        pawn.setCurrentTileId(selectedPawn.getCurrentTileId());
        break;
      }
    }
  }

  public Pawn getPawn(Pawn selectedPawn) {
    for (Pawn pawn : pawns) {
      if (pawn.getPawnId().equals(selectedPawn.getPawnId())) {
        return pawn;
      }
    }
    return null;
  }

  public Pawn getPawn(PawnId selectedPawnId) {
    for (Pawn pawn : pawns) {
      if (pawn.getPawnId().equals(selectedPawnId)) {
        return pawn;
      }
    }
    return null;
  }

  public Pawn getPawn(PositionKey selectedTileId) {
    for (Pawn pawn : pawns) {
      if (pawn.getCurrentTileId().equals(selectedTileId)) {
        return pawn;
      }
    }
    return null;
  }

  public PositionKey getPawnTileId(PawnId pawnId) {
    for (Pawn pawn : pawns) {
      if (pawn.getPawnId().equals(pawnId)) {
        return pawn.getCurrentTileId();
      }
    }
    return null;
  }

  public ArrayList<Pawn> getPawns() {
    return pawns;
  }

  // ── Player helpers ────────────────────────────────────────────────────────

  @Nullable
  private Player findPlayerById(String playerId) {
    for (Player player : players) {
      if (player.getId().equals(playerId)) {
        return player;
      }
    }
    return null;
  }

  public ArrayList<Player> getPlayers() {
    return players;
  }

  public ArrayList<String> getActivePlayers() {
    return players.stream()
        .filter(Player::getIsActive)
        .map(Player::getId)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public HashMap<String, Integer> getPlayerColors() {
    return playerColors;
  }

  public ArrayList<String> getWinners() {
    return winners;
  }

  // ── Misc ──────────────────────────────────────────────────────────────────

  public Card getCard(int cardUUID, String playerId) {
    for (Card card : cardsDeck.getCardsForPlayer(playerId)) {
      if (card.getUuid().equals(cardUUID)) {
        return card;
      }
    }
    System.out.println("Card not found for " + playerId + " " + cardUUID);
    return null;
  }

  public boolean playerHasCard(String playerId, Card card) {
    return cardsDeck.playerHasCard(playerId, card);
  }

  public void duplicatePlayerCard(String playerId, Card card) {
    cardsDeck.setPlayerCard(playerId, card);
  }

  public boolean isMakeMove(MoveRequest request) {
    return TempMessageType.MAKE_MOVE.equals(request.getTempMessageType());
  }

  public void clearResponse(MoveResponse response) {
    response.setPawn1(null);
    response.setPawn2(null);
    response.setMoveType(null);
    response.setMovePawn1(null);
    response.setMovePawn2(null);
  }

  public Boolean hasStarted() {
    return hasStarted;
  }

  public long getVersion() {
    return version.get();
  }

  public int getNrPlayers() {
    return players.size();
  }

  public String getPlayerIdTurn() {
    return playerIdTurn;
  }

  /**
   * for testing purposes
   */
  public void setPlayerIdTurn(String playerId) {
    playerIdTurn = playerId;
  }

  public void setAnimationSpeed(int speed) {
    animationSpeed = speed;
  }

  public int getAnimationSpeed() {
    return animationSpeed;
  }

  public void setExactMoveRequired(boolean exactMoveRequired) {
    this.exactMoveRequired = exactMoveRequired;
  }

  public boolean isExactMoveRequired() {
    return exactMoveRequired;
  }
}