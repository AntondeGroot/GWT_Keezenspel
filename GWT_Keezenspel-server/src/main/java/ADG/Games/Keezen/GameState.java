package ADG.Games.Keezen;

import ADG.Games.Keezen.Move.MessageType;
import ADG.util.PlayerStatus;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.MoveType;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.Player;
import ADG.Log;
import com.adg.openapi.model.PositionKey;
import com.adg.openapi.model.TempMessageType;
import java.util.*;
import java.util.stream.Collectors;

import static ADG.util.BoardLogic.isPawnOnFinish;
import static ADG.util.BoardLogic.isPawnOnNest;
import static ADG.util.CardValueCheck.isAce;
import static ADG.util.CardValueCheck.isJack;
import static ADG.util.CardValueCheck.isKing;
import static ADG.util.CardValueCheck.isSeven;
import static ADG.util.PlayerStatus.hasFinished;
import static ADG.util.PlayerStatus.setActive;
import static ADG.util.PlayerStatus.setInactive;
import static com.adg.openapi.model.MoveResult.CANNOT_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.CAN_MAKE_MOVE;
import static com.adg.openapi.model.MoveResult.INVALID_SELECTION;
import static com.adg.openapi.model.MoveResult.PLAYER_DOES_NOT_HAVE_CARD;
import static com.adg.openapi.model.MoveType.MOVE;
import static com.adg.openapi.model.MoveType.ON_BOARD;
import static com.adg.openapi.model.MoveType.SPLIT;
import static com.adg.openapi.model.MoveType.SWITCH;
import static com.adg.openapi.model.RequestType.CHECK_MOVE;
import static com.adg.openapi.model.RequestType.MAKE_MOVE;

public class GameState {

  private ArrayList<Pawn> pawns = new ArrayList<>();
  private String playerIdTurn;
  private String playerIdStartingRound;
  private final ArrayList<Player> players = new ArrayList<>();
  private final HashMap<String, Integer> playerColors = new HashMap<>(); // to map a player UUID to an int for player Colors
  private final ArrayList<String> activePlayers = new ArrayList<>();
  private final ArrayList<String> winners = new ArrayList<>();
  private final int MAX_PLAYERS = 8;
  private final CardsDeckInterface cardsDeck;
  private int animationSpeed;
  private Boolean hasStarted = false;

  public Boolean hasStarted() {
    return hasStarted;
  }

  public void reset() {
    winners.clear();
    for (Pawn p : pawns) {
      p.setCurrentTileId(p.getNestTileId());
    }
    resetActivePlayers();
    cardsDeck.reset();
  }

  public GameState(CardsDeckInterface cardsDeck) {
    this.cardsDeck = cardsDeck;
  }

  public void stop() {
    hasStarted = false;

    pawns.clear();
    players.clear();
    playerColors.clear();
    activePlayers.clear();
    winners.clear();
  }

  public void start() {
    hasStarted = true;

    playerIdTurn = players.getFirst().getId();
    playerIdStartingRound = playerIdTurn;
    if (pawns.isEmpty()) {
      pawns = new ArrayList<>();
      int playerInt = 0;
      for (Player player : players) {
        activePlayers.add(player.getId());
        playerColors.put(player.getId(), playerInt);
        for (int pawnNr = 0; pawnNr < 4; pawnNr++) {
          PositionKey currentPosition = new PositionKey(player.getId(), -1 - pawnNr);
          PositionKey nestPosition = currentPosition;
          pawns.add(new Pawn(
              player.getId(),
              new PawnId(player.getId(), pawnNr),
              currentPosition,
              nestPosition
          ));
        }
        playerInt++;
      }

      cardsDeck.addPlayers(players);
      cardsDeck.shuffleIfFirstRound();
      cardsDeck.dealCards();
    }
  }

  public ArrayList<Player> getPlayers() {
    return players;
  }

  public void addPlayer(Player player) {
    if (!players.contains(player) && players.size() < MAX_PLAYERS) {
      player.setIsActive(true);
      player.setPlace(-1);
      player.isPlaying(false);
      players.add(player);
    }
  }

  public HashMap<String, Integer> getPlayerColors() {
    return playerColors;
  }

  public ArrayList<String> getActivePlayers() {
    return players.stream()
        .filter(Player::getIsActive)
        .map(Player::getId)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public void resetActivePlayers() {
    activePlayers.clear();
    for (Player player : players) {
      if (!hasFinished(player)) {
        setActive(player);
        activePlayers.add(player.getId());
      }
    }
  }

  public void forfeitPlayer(String playerId) {
    Optional<Player> matchingPlayer = players.stream()
        .filter(player -> player.getId().equals(playerId))
        .findFirst();

    matchingPlayer.ifPresent(PlayerStatus::setInactive);

    if (players.stream().noneMatch(Player::getIsActive)) {
      resetActivePlayers();
      cardsDeck.shuffleIfFirstRound();// error
      cardsDeck.dealCards();
      nextRoundPlayer();
    } else {
      activePlayers.remove(playerId);
      nextActivePlayer();
    }
  }

  private void removeWinnerFromActivePlayerList() {
    for (String winnerId : winners) {
      activePlayers.remove(winnerId);
    }
  }

  private void nextRoundPlayer() {
    playerIdTurn = nextPlayerId(playerIdStartingRound);
    playerIdStartingRound = playerIdTurn;
    if (!activePlayers.isEmpty() && !activePlayers.contains(playerIdTurn)) {
      nextRoundPlayer();
    }
    // todo: check if all players have finished
    // update player with PlayerId to be playing
    for (Player player : players) {
      player.setIsPlaying(player.getId().equals(playerIdTurn));
    }
  }

  private void nextActivePlayer() {
    playerIdTurn = nextPlayerId(playerIdTurn);
    if (!activePlayers.isEmpty() && !activePlayers.contains(playerIdTurn)) {
      nextActivePlayer();
    }
    // todo: check if all players have finished
    // update player with PlayerId to be playing
    for (Player player : players) {
      player.setIsPlaying(player.getId().equals(playerIdTurn));
    }
  }

  public String getPlayerIdTurn() {
    return playerIdTurn;
  }

  /**
   * for testing purposes
   *
   * @param playerId
   */
  public void setPlayerIdTurn(String playerId) {
    playerIdTurn = playerId;
  }

  public ArrayList<Pawn> getPawns() {
    return pawns;
  }

  private boolean isPawnOnLastSection(String playerId, String sectionId) {
    return sectionId.equals(previousPlayerId(playerId));
  }

  public String previousPlayerId(String playerId) {
    int playerInt = playerColors.get(playerId);
    int previousPlayerInt = (playerInt + players.size() - 1) % players.size();
    return playerColors.entrySet().stream()
        .filter(entry -> entry.getValue().equals(previousPlayerInt))
        .map(HashMap.Entry::getKey).findFirst().orElse("0");
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

  public int getNrPlayers() {
    return players.size();
  }

  private boolean isPawnLooselyClosedIn(Pawn pawn, PositionKey tileId) {
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

  private boolean isPawnTightlyClosedIn(Pawn pawn, PositionKey tileId) {

    String playerId = pawn.getPlayerId();
    if (tileId.getTileNr() == 19
        && !canMoveToTile(pawn, new PositionKey(playerId, 18))) {
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

  /**
   * @param selectedPawn
   * @param nextTileId
   * @Return True if it ends on own position
   * @Return False it it ends on own other pawn of same player
   * @Return False if it ends on blockaded starting tile
   *
   */
  private boolean canMoveToTile(Pawn selectedPawn, PositionKey nextTileId) {
    if (nextTileId.getTileNr() > 19) {
      return false;
    }
    Pawn pawn = getPawn(nextTileId);
    if (pawn != null) {
      Log.info("found pawn on start tile: " + pawn);
      if (pawn.equals(selectedPawn)) {
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

  private boolean cannotMoveToTileBecauseSamePlayer(Pawn selectedPawnId, PositionKey nextTileId) {
    Pawn pawn = getPawn(nextTileId);
    if (pawn != null) {
      if (Objects.equals(pawn.getPlayerId(), selectedPawnId.getPlayerId()) && !pawn.getPawnId()
          .equals(selectedPawnId)) {
        return true;
      }
    }
    return false;
  }

  public PositionKey getPawnTileId(PawnId pawnId) {
    for (Pawn pawn : pawns) {
      if (pawn.getPawnId().equals(pawnId)) {
        return pawn.getCurrentTileId();
      }
    }
    return null;
  }

  public void processOnSplit(MoveRequest moveMessage, MoveResponse response) {
    Pawn pawn1 = moveMessage.getPawn1();
    Pawn pawn2 = moveMessage.getPawn2();
    Card card = moveMessage.getCard();
    MoveType moveType = moveMessage.getMoveType();
    int nrStepsPawn1 = moveMessage.getStepsPawn1();
    int nrStepsPawn2 = moveMessage.getStepsPawn2();
    String playerId = pawn1.getPlayerId();
    String playerId2 = pawn2.getPlayerId();

    if (!playerId.equals(playerId2)) {
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    // todo: this seems sensible but will fail tests do not uncomment
//        if(!playerId.equals(playerIdTurn)){
//            response.setResult(CANNOT_MAKE_MOVE);
//            return;
//        }
    // todo : do not uncomment the above

    if (pawn1 == null || card == null || pawn2 == null) {
      response.setResult(INVALID_SELECTION);
      return;
    }

    if (!isSeven(card)) {
      response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return;
    }

    // This should not actually be possible due to validation on client side
    // However, still double check it.
    if ((nrStepsPawn1 + nrStepsPawn2 != 7) && moveType == MOVE) {
      response.setResult(INVALID_SELECTION);
      return;
    }
    MoveRequest moveMessagePawn1 = new MoveRequest();
    MoveRequest moveMessagePawn2 = new MoveRequest();
    // pawn1
    moveMessagePawn1.setPlayerId(playerId);
    moveMessagePawn1.setCard(card);
    moveMessagePawn1.setStepsPawn1(nrStepsPawn1);
    moveMessagePawn1.setPawn1(moveMessage.getPawn1());
    moveMessagePawn1.setTempMessageType(TempMessageType.CHECK_MOVE);
    moveMessagePawn1.setMoveType(SPLIT);
    // pawn2
    moveMessagePawn2.setPlayerId(playerId);
    moveMessagePawn2.setCard(card);
    moveMessagePawn2.setStepsPawn1(nrStepsPawn2);
    moveMessagePawn2.setPawn1(moveMessage.getPawn2());
    moveMessagePawn2.setTempMessageType(TempMessageType.CHECK_MOVE);
    moveMessagePawn2.setMoveType(SPLIT);

    MoveResponse moveResponsePawn1 = new MoveResponse();
    MoveResponse moveResponsePawn2 = new MoveResponse();

    // the following is a bit convoluted
    // 1. backup Pawn1
    // 2. move Pawn1 as if it were already done for real
    // 3. check move Pawn2
    // 4. move Pawn1 back to its original place
    // 5. then if the movetype is MAKE_MOVE then do it for real.
    // make sure to use new Pawn(), otherwise it will refer to the same memory and the backup would be updated!
    Pawn backupPawn1 = new Pawn(
        pawn1.getPlayerId(),
        pawn1.getPawnId(),
        moveMessagePawn1.getPawn1().getCurrentTileId(),
        pawn1.getNestTileId());

    processOnMove(moveMessagePawn1, moveResponsePawn1);
    if (moveResponsePawn1.getResult().equals(CANNOT_MAKE_MOVE)) {
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    // temporarily move Pawn1
    movePawn(new Pawn(pawn1.getPlayerId(),
        pawn1.getPawnId(),
        moveResponsePawn1.getMovePawn1().getLast(),
        pawn1.getNestTileId()));

    // check Pawn2, this time it will take in account the new position of Pawn1
    processOnMove(moveMessagePawn2, moveResponsePawn2);
    //restore and move Pawn1 back to where it originally was
    movePawn(new Pawn(pawn1.getPlayerId(),
        pawn1.getPawnId(),
        backupPawn1.getCurrentTileId(),
        pawn1.getNestTileId()));
    if (moveResponsePawn2.getResult().equals(CANNOT_MAKE_MOVE)) {
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    if (Objects.equals(moveMessage.getTempMessageType(), TempMessageType.MAKE_MOVE)) {
      if (moveMessage.getStepsPawn1() + moveMessage.getStepsPawn2() != 7) {
        response.setResult(INVALID_SELECTION);
        return;
      }
      // DO IT AGAIN NOW FOR REAL
      moveMessagePawn1.setTempMessageType(TempMessageType.MAKE_MOVE);
      cardsDeck.setPlayerCard(playerId,
          card); // duplicate the 7 card so that the player can play both pawns with 1 card
      moveMessagePawn2.setTempMessageType(TempMessageType.MAKE_MOVE);
      processOnMove(moveMessagePawn1, moveResponsePawn1, false);
      processOnMove(moveMessagePawn2, moveResponsePawn2, true);
      //response.setMessageType(MAKE_MOVE);
    } else {
      //response.setMessageType(CHECK_MOVE);
    }
    response.setPawn1(moveMessage.getPawn1());
    response.setPawn2(moveMessage.getPawn2());
    response.setMovePawn1(moveResponsePawn1.getMovePawn1());
    response.setMovePawn2(moveResponsePawn2.getMovePawn1());
    if (moveResponsePawn1.getMovePawnKilledByPawn1() != null) {
//            response.setMovePawnKilledByPawn1(moveResponsePawn1. getPawnIdKilled1());// only the first one is filled in with a kill when you check only 1 pawn
//            response.setMoveKilledPawn1(moveResponsePawn1.getMoveKilledPawn1());
    }
    if (moveResponsePawn2.getMovePawnKilledByPawn2() != null) {
//            response.setMovePawnKilledByPawn2(moveMessagePawn2); setPawnIdKilled2(moveResponsePawn2.getPawnIdKilled1());// only the first one is filled in with a kill when you check only 1 pawn
      response.setMovePawnKilledByPawn2(moveResponsePawn2.getMovePawnKilledByPawn2());
    }
    response.setResult(CAN_MAKE_MOVE);
    response.setMoveType(SPLIT);
  }


  public void processOnMove(MoveRequest moveMessage, MoveResponse response) {
    processOnMove(moveMessage, response, true);
  }

  public void processOnMove(MoveRequest moveMessage, MoveResponse response,
      boolean goToNextPlayer) {
    // only don't go to next player when playing a SPLIT card, since you have to make processOnMove twice
    Pawn pawn1 = moveMessage.getPawn1();
    Card card = moveMessage.getCard();
    if (pawn1 == null || card == null) {
      response.setResult(INVALID_SELECTION);
      return;
    }

    String playerId = moveMessage.getPlayerId();
    // todo: this seems sensible but will fail tests
//        if(!playerId.equals(playerIdTurn)){
//            response.setResult(CANNOT_MAKE_MOVE);
//            return;
//        }
    PositionKey currentTileId = pawn1.getCurrentTileId();//getPawnTileId(pawn1);
    int nrSteps = moveMessage.getStepsPawn1();
    int next;
    String playerIdOfTile = currentTileId.getPlayerId();
    Log.info("moveMessage = " + moveMessage);
    LinkedList<PositionKey> moves = new LinkedList<>();
    response.setMoveType(MOVE);
    Log.info("GameState: OnMove: received msg: " + moveMessage);
    PositionKey startTileId;

    // You cannot move from nest tiles
    if (currentTileId.getTileNr() < 0) {
      response.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    // Player must have the card he wants to play
    if (!cardsDeck.playerHasCard(playerId, card)) {
      response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return;
    }

    // Player cannot move an opponents pawn without playing a Jack
    if (!isJack(card)) {
      if (moveMessage.getPawn1() != null && !Objects.equals(moveMessage.getPawn1().getPlayerId(),
          playerId)) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
      if (moveMessage.getPawn2() != null && !Objects.equals(moveMessage.getPawn2().getPlayerId(),
          playerId)) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
    }

    moves.add(currentTileId);
    next = currentTileId.getTileNr() + moveMessage.getStepsPawn1();

    // regular route
    if (next > 15 &&
        !isPawnOnLastSection(playerId, playerIdOfTile) &&
        !isPawnOnFinish(pawn1)) {
      Log.info("GameState: OnMove: normal route between 0,15 but could move to next section");
      // check

      if (currentTileId.getTileNr() < 1) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 1));
      }
      if (currentTileId.getTileNr() < 7) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 7));
      }
      if (currentTileId.getTileNr() < 13) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 13));
      }
      if (currentTileId.getTileNr() < 15) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 15));
      }

      startTileId = new PositionKey(nextPlayerId(playerIdOfTile), 0);
      if (canPassStartTile(pawn1, startTileId)) {
        Log.info(
            "GameState: OnMove: can move past StartTile " + new PositionKey(playerIdOfTile + 1, 0));
        Log.info("GameState: OnMove: normal route can move to the next section");
        next = next % 16;
        playerIdOfTile = nextPlayerId(playerIdOfTile);
        if (next > 1) {
          moves.add(new PositionKey(playerIdOfTile, 1));
        }
        if (next > 7) {
          moves.add(new PositionKey(playerIdOfTile, 7));
        }
      } else { // or turn back
        Log.info("GameState: OnMove: normal route is blocked by a start tile, move backwards");
        next = 15 - next % 15;
        moves.add(new PositionKey(playerIdOfTile, 15));
        if (next < 13) {
          moves.add(new PositionKey(playerIdOfTile, 13));
        }
        if (next < 7) {
          moves.add(new PositionKey(playerIdOfTile, 7));
        }
      }

      PositionKey nextTileId = new PositionKey(playerIdOfTile, next);
      moves.add(nextTileId);
      if (canMoveToTile(pawn1, nextTileId)) {
        response.setMovePawn1(moves);
        processMove(pawn1, new PositionKey(playerIdOfTile, next), moveMessage, response,
            goToNextPlayer);
      } else {
        response.setResult(CANNOT_MAKE_MOVE);
      }
      return;
    }

    // normal route within section
    if (next > 0 &&
        next <= 15 &&
        !isPawnOnFinish(pawn1)) {
      Log.info("GameState: OnMove: normal route between 0,15");
      // check if you can kill an opponent
      PositionKey nextTileId = new PositionKey(playerIdOfTile, next);

      // in case you end up on your own pawn
      if (!canMoveToTile(pawn1, nextTileId)) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
      if (nrSteps > 0) {
        if (next > 1 && currentTileId.getTileNr() < 1) {
          moves.add(new PositionKey(playerIdOfTile, 1));
        }
        if (next > 7 && currentTileId.getTileNr() < 7) {
          moves.add(new PositionKey(playerIdOfTile, 7));
        }
        if (next > 13 && currentTileId.getTileNr() < 13) {
          moves.add(new PositionKey(playerIdOfTile, 13));
        }
      } else {
        if (next < 13 && currentTileId.getTileNr() > 13) {
          moves.add(new PositionKey(playerIdOfTile, 13));
        }
        if (next < 7 && currentTileId.getTileNr() > 7) {
          moves.add(new PositionKey(playerIdOfTile, 7));
        }
        if (next < 1 && currentTileId.getTileNr() > 1) {
          moves.add(new PositionKey(playerIdOfTile, 1));
        }
      }

      moves.add(nextTileId);
      response.setMovePawn1(moves);

      processMove(pawn1, new PositionKey(playerIdOfTile, next), moveMessage, response,
          goToNextPlayer);

      return;
    }

    // you go negative
    if (next < 0) {
      Log.info("GameState: OnMove: pawn goes backwards");
      if (currentTileId.getTileNr() > 1) {
        moves.add(new PositionKey(playerIdOfTile, 1));
      }

      // check if you can pass || otherwise turn back i.e. forward
      startTileId = new PositionKey(playerIdOfTile, 0);
      if (canPassStartTile(pawn1, startTileId)) {
        next = 16 + next;
        playerIdOfTile = previousPlayerId(playerIdOfTile);
        if (next < 13) {
          moves.add(new PositionKey(playerIdOfTile, 13));
        }
      } else { // or turn back (forwards since next is negative)
        Log.info(
            "GameState: OnMove: pawn wants to go backwards but is blocked by a start tile, goes forwards");
        next = -next + 2; // +1 : you can't move on tile 0 and would then move on tile 1 twice.
      }

      PositionKey nextTileId = new PositionKey(playerIdOfTile, next);
      moves.add(nextTileId);
      if (canMoveToTile(pawn1, nextTileId)) {
        response.setMovePawn1(moves);
        processMove(pawn1, nextTileId, moveMessage, response, goToNextPlayer);
      } else {
        response.setResult(CANNOT_MAKE_MOVE);
      }
      return;
    }

    // when moving backwards and ending exactly on the starttile
    if (next == 0) {
      Log.info("GameState: OnMove: pawn ends exactly on start tile");
      if (currentTileId.getTileNr() > 1) {
        moves.add(new PositionKey(playerIdOfTile, 1));
      }
      if (canMoveToTile(pawn1, new PositionKey(playerIdOfTile, 0))) {
        moves.add(new PositionKey(playerIdOfTile, 0));
        response.setMovePawn1(moves);
        processMove(pawn1, new PositionKey(playerIdOfTile, 0), moveMessage, response,
            goToNextPlayer);
        return;
      }

      // if your own pawn is on start, but it is not a blockade: your move is invalid
      if (!tileIsABlockade(new PositionKey(playerIdOfTile, 0)) && cannotMoveToTileBecauseSamePlayer(
          pawn1, new PositionKey(playerIdOfTile, 0))) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
      // move forwards to tile 2 if you can
      if (canMoveToTile(pawn1, new PositionKey(playerIdOfTile, 2))) {
        moves.add(new PositionKey(playerIdOfTile, 2));
        response.setMovePawn1(moves);
        processMove(pawn1, new PositionKey(playerIdOfTile, 2), moveMessage, response,
            goToNextPlayer);
        return;
      } else {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
    }

    // pawn is already on finish
    if (isPawnOnFinish(pawn1)) {
      Log.info("GameState: OnMove: pawn is already on the finish");
      // moving is not possible when the pawn is directly between two other pawns
      if (isPawnTightlyClosedIn(pawn1, currentTileId)) {
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }
      // moving between pawns on the finish tile
      if (isPawnLooselyClosedIn(pawn1, currentTileId)) {
        ArrayList<PositionKey> pingpongmoves = pingpongMove(pawn1, currentTileId, nrSteps);
        moves.clear();
        moves.addAll(pingpongmoves);// todo is this necessary?
        response.setMovePawn1(moves);
        processMove(pawn1, pingpongmoves.getLast(), moveMessage, response, goToNextPlayer);
        return;
      }

      PositionKey targetTileId = moveAndCheckEveryTile(pawn1, currentTileId, nrSteps);
      int tileHighestTileNr = 0;
      if (nrSteps > 0) {
        tileHighestTileNr = checkHighestTileNrYouCanMoveTo(pawn1, currentTileId, nrSteps);
        if (tileHighestTileNr > targetTileId.getTileNr()) {
          Log.info("GameState: OnMove: pawn moves out of the finish");
          moves.add(new PositionKey(playerIdOfTile, tileHighestTileNr));
        }
      }
      if (targetTileId.getTileNr() < 15) {
        moves.add(new PositionKey(targetTileId.getPlayerId(), 15));
      }
      if (targetTileId.getTileNr() < 13) {
        moves.add(new PositionKey(targetTileId.getPlayerId(), 13));
      }
      if (targetTileId.getTileNr() < 7) {
        moves.add(new PositionKey(targetTileId.getPlayerId(), 7));
      }

      moves.add(targetTileId);
      response.setMovePawn1(moves);
      processMove(pawn1, targetTileId, moveMessage, response, goToNextPlayer);
      return;
    }

    if (next > 15 &&
        isPawnOnLastSection(playerId, playerIdOfTile)) {
      Log.info("GameState: OnMove: pawn is on last section and goes into finish");
      if (currentTileId.getTileNr() < 7) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 7));
      }
      if (currentTileId.getTileNr() < 13) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 13));
      }
      if (currentTileId.getTileNr() < 15) {
        moves.add(new PositionKey(currentTileId.getPlayerId(), 15));
      }

      PositionKey targetTileId = moveAndCheckEveryTile(pawn1, currentTileId, nrSteps);

      int tileHighestTileNr = checkHighestTileNrYouCanMoveTo(pawn1, currentTileId, nrSteps);
      if (tileHighestTileNr > targetTileId.getTileNr()) {
        if (tileHighestTileNr > 15) {
          // move to finish
          moves.add(new PositionKey(nextPlayerId(playerIdOfTile), tileHighestTileNr));
          // possibly move back out of finish
          // otherwise if 16 was taken, then it would move (0,15) (1,15) (0,15) and then correctly back
          if (targetTileId.getTileNr() < 15) {
            moves.add(new PositionKey(targetTileId.getPlayerId(), 15));
          }
        }
        if (targetTileId.getTileNr() < 13) {
          moves.add(new PositionKey(targetTileId.getPlayerId(), 13));
        }
        if (targetTileId.getTileNr() < 7) {
          moves.add(new PositionKey(targetTileId.getPlayerId(), 7));
        }
      }

      if (cannotMoveToTileBecauseSamePlayer(pawn1, targetTileId)) {
        clearResponse(response);
        response.setResult(CANNOT_MAKE_MOVE);
        return;
      }

      moves.add(targetTileId);
      response.setMovePawn1(moves);
      processMove(pawn1, targetTileId, moveMessage, response, goToNextPlayer);
    }
  }

  private void clearResponse(MoveResponse response) {
    response.setPawn1(null);
    response.setPawn2(null);
    response.setMoveType(null);
    response.setMovePawn1(null);
    response.setMovePawn2(null);
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

    // an extra check to see if the first two moves are identical. this can happen when you do -4 steps and are
    // closed in from behind or try to move forward but are blocked that way.
    if (moves.size() >= 2) {
      if (moves.get(0).equals(moves.get(1))) {
        moves.removeFirst();
      }
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
      if (tileNrToCheck > 15) {// only check tiles when they are on the finish
        if (!canMoveToTile(pawn, new PositionKey(pawn.getPlayerId(), tileNrToCheck))) {
          direction = -direction;
          tileNrToCheck = tileNrToCheck + 2 * direction;
        }
      }
    }

    if (tileNrToCheck <= 15) {// when back on the last section, change the playerId of the section
      return new PositionKey(previousPlayerId(pawn.getPlayerId()), tileNrToCheck);
    }

    return new PositionKey(pawn.getPlayerId(), tileNrToCheck);

  }

  public void processOnForfeit(String playerId) {
    cardsDeck.forfeitCardsForPlayer(playerId);
    forfeitPlayer(playerId);
  }

  public void processOnSwitch(MoveRequest moveMessage, MoveResponse moveResponse) {

    Pawn pawn1t = moveMessage.getPawn1();
    Pawn pawn2t = moveMessage.getPawn2();
    Card card = moveMessage.getCard();

    // invalid selection
    if (pawn1t == null || card == null || pawn2t == null) {
      moveResponse.setResult(INVALID_SELECTION);
      return;
    }

    String selectedPawnPlayerId1 = moveMessage.getPawn1().getPlayerId();
    String selectedPawnPlayerId2 = moveMessage.getPawn2().getPlayerId();
    String playerId = moveMessage.getPlayerId();
    // todo: this seems sensible but will fail tests
//        if(!playerId.equals(playerIdTurn)){
//            moveResponse.setResult(CANNOT_MAKE_MOVE);
//            return;
//        }
    //todo: do not uncomment above
    moveResponse.setMoveType(SWITCH);

    // You can't switch with yourself
    if (Objects.equals(selectedPawnPlayerId1, selectedPawnPlayerId2)) {
      moveResponse.setResult(CANNOT_MAKE_MOVE);
      return;
    }
    // You can't switch two opponents
    if (!playerId.equals(selectedPawnPlayerId1) && (!playerId.equals(selectedPawnPlayerId2))) {
      moveResponse.setResult(CANNOT_MAKE_MOVE);
      return;
    }
    if (!cardsDeck.playerHasCard(playerId, card)) {
      moveResponse.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return;
    }

    // get pawn state from gamestate, do not trust the client
    Pawn pawn1 = getPawn(pawn1t);
    Pawn pawn2 = getPawn(pawn2t);

    String tilePlayerId2 = pawn2.getCurrentTileId().getPlayerId();
    // pawns cannot move from Finish or from Nest
    if (isPawnOnNest(pawn1) || isPawnOnNest(pawn2) ||
        isPawnOnFinish(pawn1) || isPawnOnFinish(pawn2)) {
      moveResponse.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    // player1 can move from start
    // player2 cannot be taken from start
    int tileNr2 = pawn2.getCurrentTileId().getTileNr();
    if (tilePlayerId2.equals(pawn2.getPlayerId()) && tileNr2 == 0) {
      moveResponse.setResult(CANNOT_MAKE_MOVE);
      return;
    }

    LinkedList<PositionKey> move1 = new LinkedList<>();
    LinkedList<PositionKey> move2 = new LinkedList<>();

    move1.add(pawn1.getCurrentTileId());
    move1.add(pawn2.getCurrentTileId());

    move2.add(pawn2.getCurrentTileId());
    move2.add(pawn1.getCurrentTileId());

    moveResponse.setMovePawn1(move1);
    moveResponse.setMovePawn2(move2);
    moveResponse.setPawn1(pawn1);
    moveResponse.setPawn2(pawn2);
    moveResponse.setResult(CAN_MAKE_MOVE);

    PositionKey tileId1 = new PositionKey(pawn1.getCurrentTileId().getPlayerId(),
        pawn1.getCurrentTileId().getTileNr());
    PositionKey tileId2 = new PositionKey(pawn2.getCurrentTileId().getPlayerId(),
        pawn2.getCurrentTileId().getTileNr());
    // switch in gamestate
    // only use the card when not testing
    if (moveMessage.getTempMessageType().equals(TempMessageType.MAKE_MOVE)) {
      movePawn(new Pawn(
          pawn1.getPlayerId(),
          pawn1.getPawnId(),
          tileId2,
          pawn1.getNestTileId()));
      movePawn(new Pawn(
          pawn2.getPlayerId(),
          pawn2.getPawnId(),
          tileId1,
          pawn2.getNestTileId()));
      Boolean playerHasNoCardsLeft = cardsDeck.playerPlaysCard(playerId, card);
      if (playerHasNoCardsLeft) {
        forfeitPlayer(playerId);
      } else {
        nextActivePlayer();
      }
    }
  }

  public void processOnBoard(MoveRequest moveMessage, MoveResponse response) {
    Pawn pawn1 = moveMessage.getPawn1();
    Card card = moveMessage.getCard();
    String playerId = moveMessage.getPlayerId();

    // invalid selection
    if (pawn1 == null || card == null) {
      response.setResult(INVALID_SELECTION);
      return;
    }

    // player should have the card he's playing
    if (!cardsDeck.playerHasCard(playerId, card)) {
      response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
      return;
    }

    // cannot go onboard without an Ace or King
    if (!(isAce(card) || isKing(card))) {
      response.setResult(CANNOT_MAKE_MOVE);
      //response.setErrorMessage("You can't move on board without an Ace or King");
      return;
    }

    PositionKey currentTileId = getPawn(pawn1).getCurrentTileId();

    PositionKey targetTileId = new PositionKey(playerId, 0);
    response.setMoveType(ON_BOARD);

    // when occupied by own pawn
    if (!canMoveToTile(pawn1, targetTileId)) {
      response.setResult(CANNOT_MAKE_MOVE);
      //response.setErrorMessage("You can't end up on your own pawn");
      return;
    }

    // when pawn not in the nest
    if (currentTileId.getTileNr() >= 0) {
      response.setResult(CANNOT_MAKE_MOVE);
      //response.setErrorMessage("You can't move from nest to board when you are already on board");
      return;
    }

    LinkedList<PositionKey> move = new LinkedList<>();
    move.add(currentTileId);
    move.add(targetTileId);

    response.setPawn1(pawn1);
    response.setMovePawn1(move);
    processMove(pawn1, targetTileId, moveMessage, response);
  }

  public void processMove(Pawn pawn, PositionKey targetTileId, MoveRequest moveMessage,
      MoveResponse response) {
    processMove(pawn, targetTileId, moveMessage, response, true);
  }

  public void processMove(Pawn pawn0, PositionKey targetTileId, MoveRequest moveMessage,
      MoveResponse response, boolean goToNextPlayer) {
    String playerId = moveMessage.getPlayerId();
    Card card = moveMessage.getCard();

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

    // check for kills
    Pawn pawn = getPawn(targetTileId);
    if (pawn != null) {
      if (!Objects.equals(pawn.getPlayerId(), pawn0.getPlayerId())) {
        response.setPawn1(pawn0);
        response.setPawn2(null);
        LinkedList<PositionKey> move2 = new LinkedList<>();
        move2.add(targetTileId);
        move2.add(pawn.getNestTileId());
        response.setPawnKilledByPawn1(pawn);
        response.setMovePawnKilledByPawn1(move2);
        if (moveMessage.getTempMessageType().equals(MAKE_MOVE)) {
          movePawn(new Pawn(
              pawn.getPlayerId(),
              pawn.getPawnId(),
              pawn.getNestTileId(),
              pawn.getNestTileId()));
        }
      }
    }

    response.setPawn1(pawn0);
    if (Objects.equals(moveMessage.getTempMessageType(), TempMessageType.MAKE_MOVE)) {
      movePawn(new Pawn(
          pawn0.getPlayerId(),
          pawn0.getPawnId(),
          targetTileId,
          pawn0.getNestTileId()));
      Boolean playerHasNoCardsLeft = cardsDeck.playerPlaysCard(playerId, card);
      if (goToNextPlayer) {// this is only false when you SPLIT a move, the second time calling it will make you go to the next player
        if (playerHasNoCardsLeft) {
          forfeitPlayer(playerId);
        } else {
          nextActivePlayer();
        }
        checkForWinners(winners);
        removeWinnerFromActivePlayerList();
      }
    }

    response.setResult(CAN_MAKE_MOVE);
    Log.info("GameState: pawn moves to " + targetTileId + ", with response " + response);
  }

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

  public void tearDown() {
    pawns = new ArrayList<>();
    playerIdTurn = "0";
    players.clear();
    activePlayers.clear();
    winners.clear();
    playerIdStartingRound = "0";
  }

  public Pawn getPawn(Pawn selectedPawn) {
    for (Pawn pawn : pawns) {
      if (pawn.getPawnId().equals(selectedPawn.getPawnId())) {
        // only the pawnId is important, you want to use the getPawn to acquire the correct state pawn
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

  public boolean tileIsABlockade(PositionKey selectedStartTile) {
    Pawn pawnOnStart = getPawn(selectedStartTile);
    if (pawnOnStart == null) {
      return false;
    }
    if (Objects.equals(pawnOnStart.getPawnId().getPlayerId(), selectedStartTile.getPlayerId())) {
      return true;
    }
    return false;
  }

  public String nextPlayerId(String playerId) {
    int playerInt = playerColors.get(playerId);
    return nextPlayerId(playerInt);
  }

  private String nextPlayerId(int playerInt) {
    int nextPlayerInt = (playerInt + 1) % players.size();
    return playerColors.entrySet().stream()
        .filter(entry -> entry.getValue().equals(nextPlayerInt))
        .map(HashMap.Entry::getKey).findFirst().orElse("0");
  }

  public ArrayList<String> getWinners() {
    return winners;
  }

  public boolean canPassStartTile(Pawn selectedPawn, PositionKey tileId) {
    Pawn pawnOnTile = getPawn(tileId);

    if (pawnOnTile == null) {
      return true;
    }

    if (selectedPawn.equals(pawnOnTile.getPawnId())) {
      return true;
    }

    if (Objects.equals(pawnOnTile.getPlayerId(), tileId.getPlayerId())) {
      return false;
    }

    return true;
  }

  public void checkForWinners(ArrayList<String> winners) {
    ArrayList<Pawn> pawns = getPawns();
    ArrayList<Player> players = getPlayers();

    for (Player player : players) {
      int nrPawnsFinished = 0;
      String playerId = player.getId();
      for (Pawn pawn : pawns) {
        if (playerId.equals(pawn.getPlayerId()) && isPawnOnFinish(pawn)) {
          nrPawnsFinished++;
        }
      }
      if (nrPawnsFinished == 4 && !winners.contains(playerId)) {
        player.setPlace(winners.size() + 1);
        winners.add(playerId);
      }
    }
  }

  public void setAnimationSpeed(int speed) {
    animationSpeed = speed;
  }

  public int getAnimationSpeed() {
    return animationSpeed;
  }
}
