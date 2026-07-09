package adg.keezen;

import static adg.util.BoardLogic.isPawnOnFinish;
import static adg.util.CardValueCheck.isAce;
import static adg.util.CardValueCheck.isKing;
import static adg.util.PlayerStatus.hasFinished;
import static adg.util.PlayerStatus.setActive;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.CAN_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.PLAYER_DOES_NOT_HAVE_CARD;

import adg.Log;
import adg.processing.ProcessOnBoard;
import adg.processing.ProcessOnMove;
import adg.processing.ProcessOnSplit;
import adg.processing.ProcessOnSwitch;
import adg.util.PlayerStatus;
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
  private static final int MAX_PLAYERS = 8;
  private final CardsDeckInterface cardsDeck;
  private volatile int animationSpeed;
  private volatile boolean exactMoveRequired = false;
  private volatile boolean mustPlayIfPossible = false;
  private volatile boolean teamPlay = false;
  private final TradeManager tradeManager;
  private final TileReachability tileReachability;
  private final PlayerRoster roster;
  private final PawnLocations pawnLocations;
  private volatile long mustPlayBlockedSinceMs = 0;
  private static final long MUST_PLAY_TIMEOUT_MS = 3 * 60 * 1000L;
  private Boolean hasStarted = false;
  private final AtomicLong version =
      new AtomicLong(0); // to make it compatible with javascript as it doesn't do int64 well!

  // Collaborators only capture the this::getPawn reference; it is not invoked during construction.
  @SuppressWarnings("this-escape")
  public GameState(CardsDeckInterface cardsDeck) {
    this.cardsDeck = cardsDeck;
    this.roster = new PlayerRoster(players, playerColors);
    this.pawnLocations = new PawnLocations(() -> pawns);
    this.tradeManager =
        new TradeManager(
            cardsDeck, version, () -> hasStarted && teamPlay, this::teammateOf, this::isKingOrAce);
    this.tileReachability = new TileReachability(this::getPawn);
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  public void start() {
    start(true);
  }

  public void start(boolean shuffle) {
    hasStarted = true;
    if (shuffle) shufflePlayers();
    assignPlayerInts();
    assignTeams();
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
    tradeManager.clearPending();
  }

  public void reset() {
    resetWinners();
    resetPawnPositions();
    resetActivePlayers();
    resetCards();
    resetTurn();
    tradeManager.clearPending();
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

  /**
   * In team play, teams are PAIRS: each player teams up with the player directly opposite
   * (seat + n/2). So n players make n/2 teams — 4→2, 6→3, 8→4. teamId = seat % (n/2), which
   * pairs seat i with seat i+n/2 under the same id. Assigned only for an even count of at least
   * four (2 players would be a single pair with no opponents; odd counts can't pair) — otherwise
   * teamId stays null, the state omits it, and the roster shows no teams. Runs after
   * assignPlayerInts.
   */
  private void assignTeams() {
    int n = players.size();
    if (!teamPlay) return;
    if (n < 4 || n % 2 != 0) {
      // Teams need an even count of at least four; with fewer or an odd count there are no pairs.
      // Turn team play OFF so every downstream check (win detection, move gating, trades, the
      // client push) reverts to individual play — otherwise a lone player's win is never recorded
      // because the team win-check keeps waiting for a partner that doesn't exist.
      teamPlay = false;
      return;
    }
    int teamCount = n / 2;
    // The loop index is the seat: this runs right after assignPlayerInts over the same list,
    // so index == playerInt (and avoids unboxing the nullable getPlayerInt()).
    int seat = 0;
    for (Player player : players) {
      player.setTeamId(seat % teamCount);
      seat++;
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
    cancelTradeForDeparture(playerId);
    leavers.add(playerId);
    removePawnsForPlayer(playerId);
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
    cancelTradeForDeparture(playerId);
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

  /** A departed player is out of the game — take their pawns off the board entirely. */
  private void removePawnsForPlayer(String playerId) {
    pawns.removeIf(pawn -> playerId.equals(pawn.getPlayerId()));
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
    clearMustPlayBlocked();
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
    Integer cardId = moveMessage.getCardId();
    if (cardId == null) {
      clearResponse(response);
      response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return;
    }
    Card card = getCard(cardId, playerId);
    if (card == null) {
      clearResponse(response);
      response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return;
    }

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
    if (teamPlay) {
      checkForTeamWinners(winners);
      return;
    }
    for (Player player : players) {
      if (!winners.contains(player.getId()) && hasAllPawnsOnFinish(player.getId())) {
        recordWinner(player, winners);
      }
    }
  }

  /**
   * In team play a team places only when <em>both</em> members have all their pawns home — so a
   * player who finishes their own pawns first gets no place yet, stays active, and keeps taking
   * turns (to play their teammate's pawns) until the pair is done. Both members share the place.
   */
  private void checkForTeamWinners(ArrayList<String> winners) {
    for (Player player : players) {
      Integer team = player.getTeamId();
      if (team == null) {
        continue;
      }
      List<Player> members = teamMembers(team);
      if (members.stream().anyMatch(m -> winners.contains(m.getId()))) {
        continue; // team already placed
      }
      // A departed teammate is out of the game: their pawns are gone and don't need to come home.
      // The team places once every member STILL PRESENT has all pawns on the finish — so a lone
      // survivor wins on their own four. A fully-abandoned team can't win, and a leaver earns no
      // place (only present members are recorded).
      List<Player> present = members.stream()
          .filter(m -> !leavers.contains(m.getId()))
          .toList();
      if (!present.isEmpty() && present.stream().allMatch(m -> hasAllPawnsOnFinish(m.getId()))) {
        int place = distinctTeamsPlaced(winners) + 1;
        for (Player member : present) {
          recordWinner(member, place, winners);
        }
      }
    }
  }

  /** How many distinct teams are already in the winners list — the next team places behind them. */
  private int distinctTeamsPlaced(ArrayList<String> winners) {
    return (int) winners.stream()
        .map(this::findPlayerById)
        .filter(p -> p != null && p.getTeamId() != null)
        .map(Player::getTeamId)
        .distinct()
        .count();
  }

  private List<Player> teamMembers(int teamId) {
    return roster.teamMembers(teamId);
  }

  private boolean hasAllPawnsOnFinish(String playerId) {
    long finishedCount = pawns.stream()
        .filter(p -> playerId.equals(p.getPlayerId()))
        .filter(p -> isPawnOnFinish(p))
        .count();
    return finishedCount == 4;
  }

  private void recordWinner(Player player, ArrayList<String> winners) {
    recordWinner(player, winners.size() + 1, winners);
  }

  private void recordWinner(Player player, int place, ArrayList<String> winners) {
    player.setPlace(place);
    winners.add(player.getId());
    cardsDeck.forfeitCardsForPlayer(player.getId());
    player.setIsPlaying(false);
    player.setIsActive(false);
    version.incrementAndGet();
  }

  /**
   * Team-play hand-off rule: you may move your own pawns freely, but a teammate's pawns only
   * once all of your own are home — and never a non-teammate's pawn. No restriction when teams
   * are off. Gated at the move entry point so every move type inherits it.
   */
  public boolean isTeamMoveAllowed(String moverId, Pawn pawn) {
    if (!teamPlay || pawn == null) {
      return true;
    }
    String owner = pawn.getPlayerId();
    if (owner.equals(moverId)) {
      return true; // your own pawn
    }
    if (!sameTeam(moverId, owner)) {
      return false; // never an opponent's pawn
    }
    return hasAllPawnsOnFinish(moverId); // a teammate's pawn, only after your own are home
  }

  private boolean sameTeam(String playerA, String playerB) {
    return roster.sameTeam(playerA, playerB);
  }

  /**
   * Whether the mover may control (move/switch) this pawn: always their own, and — in team play,
   * once all their own pawns are home — their teammate's. Used by the move processors' ownership
   * checks so a teammate's pawns can be played, while opponents' stay off-limits.
   */
  public boolean mayControlPawn(String moverId, Pawn pawn) {
    if (pawn == null) {
      return false;
    }
    if (pawn.getPlayerId().equals(moverId)) {
      return true;
    }
    return teamPlay && sameTeam(moverId, pawn.getPlayerId()) && hasAllPawnsOnFinish(moverId);
  }

  // ── Team card trade (step 5) ──────────────────────────────────────────────

  public void setTeamCardTrade(boolean teamCardTrade) {
    tradeManager.setEnabled(teamCardTrade);
  }

  public boolean isTeamCardTrade() {
    return tradeManager.isEnabled();
  }

  public TradeRequest getPendingTrade() {
    return tradeManager.getPending();
  }

  public boolean requestTrade(String requesterId, Card offeredCard) {
    return tradeManager.request(requesterId, offeredCard);
  }

  public boolean acceptTrade(String teammateId, Card kingOrAce) {
    return tradeManager.accept(teammateId, kingOrAce);
  }

  public boolean rejectTrade(String teammateId) {
    return tradeManager.reject(teammateId);
  }

  public boolean cancelTrade(String requesterId) {
    return tradeManager.cancel(requesterId);
  }

  private void cancelTradeForDeparture(String playerId) {
    tradeManager.cancelForDeparture(playerId);
  }

  private String teammateOf(String playerId) {
    return roster.teammateOf(playerId);
  }

  private boolean isKingOrAce(Card card) {
    return isKing(card) || isAce(card);
  }

  // ── Board logic ───────────────────────────────────────────────────────────

  public boolean isPawnOnLastSection(String playerId, String sectionId) {
    return sectionId.equals(previousPlayerId(playerId));
  }

  public boolean isPawnLooselyClosedIn(Pawn pawn, PositionKey tileId) {
    return tileReachability.isPawnLooselyClosedIn(pawn, tileId);
  }

  public boolean isPawnTightlyClosedIn(Pawn pawn, PositionKey tileId) {
    return tileReachability.isPawnTightlyClosedIn(pawn, tileId);
  }

  public boolean canMoveToTile(Pawn selectedPawn, PositionKey nextTileId) {
    return tileReachability.canMoveToTile(selectedPawn, nextTileId);
  }

  public boolean cannotMoveToTileBecauseSamePlayer(Pawn selectedPawn, PositionKey nextTileId) {
    return tileReachability.cannotMoveToTileBecauseSamePlayer(selectedPawn, nextTileId);
  }

  public boolean canPassStartTile(Pawn selectedPawn, PositionKey tileId) {
    return tileReachability.canPassStartTile(selectedPawn, tileId);
  }

  public boolean tileIsABlockade(PositionKey selectedStartTile) {
    return tileReachability.tileIsABlockade(selectedStartTile);
  }

  public int checkHighestTileNrYouCanMoveTo(Pawn pawn, PositionKey tileId, int nrSteps) {
    return tileReachability.checkHighestTileNrYouCanMoveTo(pawn, tileId, nrSteps);
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
    return roster.nextPlayerId(playerId);
  }

  public String previousPlayerId(String playerId) {
    return roster.previousPlayerId(playerId);
  }

  // ── Pawn helpers ──────────────────────────────────────────────────────────

  /** Public for testing: set a pawn's location without triggering any validation. */
  public void movePawn(Pawn selectedPawn) {
    pawnLocations.moveTo(selectedPawn);
  }

  public Pawn getPawn(Pawn selectedPawn) {
    return pawnLocations.withId(selectedPawn.getPawnId());
  }

  public Pawn getPawn(PawnId selectedPawnId) {
    return pawnLocations.withId(selectedPawnId);
  }

  public Pawn getPawn(PositionKey selectedTileId) {
    return pawnLocations.atTile(selectedTileId);
  }

  public ArrayList<Pawn> getPawns() {
    return pawns;
  }

  // ── Player helpers ────────────────────────────────────────────────────────

  @Nullable
  private Player findPlayerById(String playerId) {
    return roster.findById(playerId);
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

  public void setMustPlayIfPossible(boolean mustPlayIfPossible) {
    this.mustPlayIfPossible = mustPlayIfPossible;
  }

  public boolean isMustPlayIfPossible() {
    return mustPlayIfPossible;
  }

  public void setTeamPlay(boolean teamPlay) {
    this.teamPlay = teamPlay;
  }

  public boolean isTeamPlay() {
    return teamPlay;
  }

  public void recordMustPlayBlocked() {
    if (mustPlayBlockedSinceMs == 0) {
      mustPlayBlockedSinceMs = System.currentTimeMillis();
    }
  }

  public void clearMustPlayBlocked() {
    mustPlayBlockedSinceMs = 0;
  }

  public boolean mustPlayTimeoutElapsed() {
    return mustPlayBlockedSinceMs > 0
        && System.currentTimeMillis() - mustPlayBlockedSinceMs > MUST_PLAY_TIMEOUT_MS;
  }

  /** For testing: backdates the blocked-since timestamp so the timeout appears to have elapsed. */
  public void setMustPlayBlockedSince(long ms) {
    mustPlayBlockedSinceMs = ms;
  }
}