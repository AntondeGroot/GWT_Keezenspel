package ADG.Games.Keezen;

import java.util.*;
import java.util.stream.Collectors;

import static ADG.Games.Keezen.CardsDeck.playerDoesNotHaveCard;
import static ADG.Games.Keezen.MessageType.*;
import static ADG.Games.Keezen.MoveResult.*;
import static ADG.Games.Keezen.MoveType.*;
import static ADG.Games.Keezen.Util.CardValueCheck.*;
import static ADG.Games.Keezen.logic.MissingTiles.extrapolateMissingTiles;
import static ADG.Games.Keezen.logic.StartTileLogic.canPassStartTile;
import static ADG.Games.Keezen.logic.WinnerLogic.checkForWinners;

public class GameState {

    private static ArrayList<Pawn> pawns = new ArrayList<>();
    private static String playerIdTurn;
    private static String playerIdStartingRound;
    private static final ArrayList<Player> players = new ArrayList<>();
    private static final HashMap<String, Integer> playerColors = new HashMap<>(); // to map a player UUID to an int for player Colors
    private static final ArrayList<String> activePlayers = new ArrayList<>();
    private static final ArrayList<String> winners = new ArrayList<>();
    private static final int MAX_PLAYERS = 8;

    public GameState() {}

    public static void stop(){
        pawns.clear();
        players.clear();
        playerColors.clear();
        activePlayers.clear();
        winners.clear();
    }

    public static void start(){
        playerIdTurn = players.getFirst().getUUID();
        playerIdStartingRound = playerIdTurn;
        if (pawns.isEmpty()) {
            pawns = new ArrayList<>();
            int playerInt = 0;
            for(Player player : players) {
                activePlayers.add(player.getUUID());
                playerColors.put(player.getUUID(), playerInt);
                for (int pawnNr = 0; pawnNr < 4; pawnNr++) {
                    pawns.add(new Pawn(
                            new PawnId(player.getUUID(), pawnNr),
                            new TileId(player.getUUID(),-1 - pawnNr),
                            playerInt));
                }
                playerInt++;
            }

            CardsDeck.addPlayers(players);
            CardsDeck.shuffleIfFirstRound();
            CardsDeck.dealCards();
        }
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }

    public static void addPlayer(Player player) {
        if(!players.contains(player) && players.size() < MAX_PLAYERS){
            player.setIndex(players.size());
            players.add(player);
        }
    }

    public static HashMap<String, Integer> getPlayerColors() {
        return playerColors;
    }

    public static ArrayList<String> getActivePlayers() {
        return players.stream()
                .filter(Player::isActive)
                .map(Player::getUUID)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static void resetActivePlayers(){
        activePlayers.clear();
        for(Player player : players){
            if(!player.hasFinished()){
                player.setActive();
                activePlayers.add(player.getUUID());
            }
        }
    }

    public static void forfeitPlayer(String playerId) {
        Optional<Player> matchingPlayer = players.stream()
                .filter(player -> player.getUUID().equals(playerId))
                .findFirst();

        matchingPlayer.ifPresent(Player::setInactive);

        if(players.stream().noneMatch(Player::isActive)){
            resetActivePlayers();
            CardsDeck.shuffleIfFirstRound();// error
            CardsDeck.dealCards();
            nextRoundPlayer();
        }else {
            activePlayers.remove(playerId);
            nextActivePlayer();
        }
    }

    private static void removeWinnerFromActivePlayerList(){
        for (String winnerId: winners){
            activePlayers.remove(winnerId);
        }
    }

    private static void nextRoundPlayer(){
        playerIdTurn = nextPlayerId(playerIdStartingRound);
        playerIdStartingRound = playerIdTurn;
        if(!activePlayers.isEmpty() && !activePlayers.contains(playerIdTurn)){
            nextRoundPlayer();
        }
        // todo: check if all players have finished
        // update player with PlayerId to be playing
        for(Player player : players){
            player.setIsPlaying(player.getUUID().equals(playerIdTurn));
        }
    }

    private static void nextActivePlayer() {
        playerIdTurn = nextPlayerId(playerIdTurn);
        if(!activePlayers.isEmpty() && !activePlayers.contains(playerIdTurn)){
            nextActivePlayer();
        }
        // todo: check if all players have finished
        // update player with PlayerId to be playing
        for(Player player : players){
            player.setIsPlaying(player.getUUID().equals(playerIdTurn));
        }
    }

    public static String getPlayerIdTurn() {
        return playerIdTurn;
    }

    /**
     * for testing purposes
     * @param playerIdTurn
     */
    public static void setPlayerIdTurn(String playerIdTurn){
        GameState.playerIdTurn = playerIdTurn;
    }

    public static ArrayList<Pawn> getPawns() {
        return pawns;
    }

    private static boolean isPawnOnLastSection(String playerId, String sectionId){
        return sectionId.equals(previousPlayerId(playerId));
    }

    public static String previousPlayerId(String playerId){
        int playerInt = playerColors.get(playerId);
        int previousPlayerInt = (playerInt + players.size() - 1) % players.size();
        return playerColors.entrySet().stream()
                .filter(entry -> entry.getValue().equals(previousPlayerInt))
                .map(HashMap.Entry::getKey).findFirst().orElse("0");
    }

    private static boolean isPawnOnFinish(PawnId pawnId, TileId tileId){
        return Objects.equals(pawnId.getPlayerId(), tileId.getPlayerId()) && tileId.getTileNr() > 15;
    }

    private static boolean isPawnClosedInFromBehind(PawnId pawnId, TileId tileId){
        int tileNr = tileId.getTileNr();
        while (tileNr > 15){
            if(!canMoveToTile(pawnId, new TileId(pawnId.getPlayerId(), tileNr-1))){
                return true;
            }
            tileNr--;
        }
        return false;
    }

    public static int getNrPlayers() {
        return players.size();
    }

    private static boolean isPawnLooselyClosedIn(PawnId pawnId, TileId tileId){
        int tileNr = tileId.getTileNr();
        String playerId = pawnId.getPlayerId();

        if(tileNr <= 16){
            return false;
        }

        for (int i = tileId.getTileNr(); i > 16; i--) {
            if(!canMoveToTile(pawnId, new TileId(playerId, i-1))){
                return true;
            }
        }

        return false;
    }

    private static boolean isPawnTightlyClosedIn(PawnId pawnId, TileId tileId){

        if(tileId.getTileNr() == 19
                && !canMoveToTile(pawnId,new TileId(pawnId.getPlayerId(), 18))){
            return true;
        }

        if(tileId.getTileNr() == 18
                && !canMoveToTile(pawnId,new TileId(pawnId.getPlayerId(), 19))
                && !canMoveToTile(pawnId,new TileId(pawnId.getPlayerId(), 17))){
            return true;
        }

        if(tileId.getTileNr() == 17
                && !canMoveToTile(pawnId, new TileId(pawnId.getPlayerId(), 18))
                && !canMoveToTile(pawnId,new TileId(pawnId.getPlayerId(), 16))){
            return true;
        }

        return false;
    }

    /**
     * @param selectedPawnId
     * @param nextTileId
     * @Return True if it ends on own position
     * @Return False it it ends on own other pawn of same player
     * @Return False if it ends on blockaded starting tile
     *
     */
    private static boolean canMoveToTile(PawnId selectedPawnId, TileId nextTileId){
        if(nextTileId.getTileNr() > 19){
            return false;
        }
        Pawn pawn = getPawn(nextTileId);
        System.out.println("found pawn on start tile: "+pawn);
        if(pawn != null) {
            if(pawn.getPawnId().equals(selectedPawnId)){
                return true;
            }

            if (Objects.equals(pawn.getPlayerId(), selectedPawnId.getPlayerId())) {
                return false;
            }
            if (Objects.equals(pawn.getPlayerId(), nextTileId.getPlayerId()) && nextTileId.getTileNr() == 0){
                return false;
            }
        }
        return true;
    }

    private static boolean cannotMoveToTileBecauseSamePlayer(PawnId selectedPawnId, TileId nextTileId){
        Pawn pawn = getPawn(nextTileId);
        if(pawn != null) {
            if (Objects.equals(pawn.getPlayerId(), selectedPawnId.getPlayerId()) && !pawn.getPawnId().equals(selectedPawnId)) {
                return true;
            }
        }
        return false;
    }

    public static TileId getPawnTileId(PawnId pawnId){
        for(Pawn pawn : pawns){
            if(pawn.getPawnId().equals(pawnId)){
                return pawn.getCurrentTileId();
            }
        }
        return null;
    }

    public static void processOnSplit(MoveMessage moveMessage, MoveResponse response){
        PawnId pawnId1 = moveMessage.getPawnId1();
        PawnId pawnId2 = moveMessage.getPawnId2();
        Card card = moveMessage.getCard();
        MoveType moveType = moveMessage.getMoveType();
        int nrStepsPawn1 = moveMessage.getStepsPawn1();
        int nrStepsPawn2 = moveMessage.getStepsPawn2();
        String playerId = pawnId1.getPlayerId();
        String playerId2 = pawnId2.getPlayerId();

        if(!playerId.equals(playerId2)){
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        if(pawnId1 == null || card == null || pawnId2 == null){
            response.setResult(INVALID_SELECTION);
            return;
        }

        if(!isSeven(card)){
            response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        if((nrStepsPawn1 + nrStepsPawn2 != 7) &&  moveType==MOVE){
            response.setResult(INVALID_SELECTION);
            return;
        }
        MoveMessage moveMessagePawn1 = new MoveMessage();
        MoveMessage moveMessagePawn2 = new MoveMessage();
        // pawn1
        moveMessagePawn1.setPlayerId(playerId);
        moveMessagePawn1.setCard(card);
        moveMessagePawn1.setStepsPawn1(nrStepsPawn1);
        moveMessagePawn1.setPawnId1(moveMessage.getPawnId1());
        moveMessagePawn1.setMessageType(CHECK_MOVE);
        moveMessagePawn1.setMoveType(SPLIT);
        // pawn2
        moveMessagePawn2.setPlayerId(playerId);
        moveMessagePawn2.setCard(card);
        moveMessagePawn2.setStepsPawn1(nrStepsPawn2);
        moveMessagePawn2.setPawnId1(moveMessage.getPawnId2());
        moveMessagePawn2.setMessageType(CHECK_MOVE);
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
        Pawn backupPawn1 = new Pawn(pawnId1, GameState.getPawn(moveMessagePawn1.getPawnId1()).getCurrentTileId());

        processOnMove(moveMessagePawn1, moveResponsePawn1);
        if(moveResponsePawn1.getResult().equals(CANNOT_MAKE_MOVE)){
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        // temporarily move Pawn1
        GameState.movePawn(new Pawn(pawnId1, moveResponsePawn1.getMovePawn1().getLast()));

        // check Pawn2, this time it will take in account the new position of Pawn1
        processOnMove(moveMessagePawn2, moveResponsePawn2);
        //restore and move Pawn1 back to where it originally was
        GameState.movePawn(new Pawn(pawnId1, backupPawn1.getCurrentTileId()));
        if(moveResponsePawn2.getResult().equals(CANNOT_MAKE_MOVE)){
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        if(moveMessage.getMessageType() == MAKE_MOVE){
            if(moveMessage.getStepsPawn1() + moveMessage.getStepsPawn2() != 7){
                response.setResult(INVALID_SELECTION);
                return;
            }
            // DO IT AGAIN NOW FOR REAL
            moveMessagePawn1.setMessageType(MAKE_MOVE);
            CardsDeck.setPlayerCard(playerId, card); // duplicate the 7 card so that the player can play both pawns with 1 card
            moveMessagePawn2.setMessageType(MAKE_MOVE);
            processOnMove(moveMessagePawn1, moveResponsePawn1, false);
            processOnMove(moveMessagePawn2, moveResponsePawn2, true);
            response.setMessageType(MAKE_MOVE);
        }else{
            response.setMessageType(CHECK_MOVE);
        }
        response.setPawnId1(moveMessage.getPawnId1());
        response.setPawnId2(moveMessage.getPawnId2());
        response.setMovePawn1(extrapolateMissingTiles(moveResponsePawn1.getMovePawn1()));
        response.setMovePawn2(extrapolateMissingTiles(moveResponsePawn2.getMovePawn1()));
        if(moveResponsePawn1.getMoveKilledPawn1() != null){
            response.setPawnIdKilled1(moveResponsePawn1.getPawnIdKilled1());// only the first one is filled in with a kill when you check only 1 pawn
            response.setMoveKilledPawn1(moveResponsePawn1.getMoveKilledPawn1());
        }
        if(moveResponsePawn2.getMoveKilledPawn1() != null){
            response.setPawnIdKilled2(moveResponsePawn2.getPawnIdKilled1());// only the first one is filled in with a kill when you check only 1 pawn
            response.setMoveKilledPawn2(moveResponsePawn2.getMoveKilledPawn1());
        }
        response.setResult(CAN_MAKE_MOVE);
        response.setMoveType(SPLIT);
    }


    public static void processOnMove(MoveMessage moveMessage, MoveResponse response){
        processOnMove(moveMessage, response, true);
    }

    public static void processOnMove(MoveMessage moveMessage, MoveResponse response, boolean goToNextPlayer){
        // only don't go to next player when playing a SPLIT card, since you have to make processOnMove twice
        PawnId pawnId1 = moveMessage.getPawnId1();
        Card card = moveMessage.getCard();
        if(pawnId1 == null || card == null){
            response.setResult(INVALID_SELECTION);
            return;
        }

        String playerId = pawnId1.getPlayerId();
        TileId currentTileId = getPawnTileId(pawnId1);
        int nrSteps = moveMessage.getStepsPawn1();
        int next = 0;
        String playerIdOfTile = currentTileId.getPlayerId();
        System.out.println("moveMessage = "+moveMessage);
        int direction = 1;
        int tileNrToCheck = currentTileId.getTileNr();
        LinkedList<TileId> moves = new LinkedList<>();
        response.setMoveType(MOVE);
        System.out.println("GameState: OnMove: received msg: " + moveMessage);
        TileId startTileId;

        // You cannot move from nest tiles
        if(currentTileId.getTileNr() < 0){
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        // Player must have the card he wants to play
        if(playerDoesNotHaveCard(playerId, card)) {
            response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        // Player cannot move an opponents pawn without playing a Jack
        if(!isJack(card)){
            if(moveMessage.getPawnId1() != null && !Objects.equals(moveMessage.getPawnId1().getPlayerId(), playerId)){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            if(moveMessage.getPawnId2() != null && !Objects.equals(moveMessage.getPawnId2().getPlayerId(), playerId)){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
        }

        moves.add(currentTileId);
        next = currentTileId.getTileNr() + moveMessage.getStepsPawn1();

         // regular route
        if (next > 15 &&
                !isPawnOnLastSection(playerId, playerIdOfTile) &&
                !isPawnOnFinish(pawnId1, currentTileId) ) {
            System.out.println("GameState: OnMove: normal route between 0,15 but could move to next section");
            // check

            if(currentTileId.getTileNr() < 1){moves.add(new TileId(currentTileId.getPlayerId(), 1));}
            if(currentTileId.getTileNr() < 7){moves.add(new TileId(currentTileId.getPlayerId(), 7));}
            if(currentTileId.getTileNr() < 13){moves.add(new TileId(currentTileId.getPlayerId(), 13));}
            if(currentTileId.getTileNr() < 15){moves.add(new TileId(currentTileId.getPlayerId(), 15));}

            startTileId = new TileId(nextPlayerId(playerIdOfTile), 0);
            if (canPassStartTile(pawnId1, startTileId)){
                System.out.println("GameState: OnMove: can move past StartTile "+new TileId(playerIdOfTile+1,0));
                System.out.println("GameState: OnMove: normal route can move to the next section");
                next = next % 16;
                playerIdOfTile = nextPlayerId(playerIdOfTile);
                if(next > 1){moves.add(new TileId(playerIdOfTile, 1));}
                if(next > 7){moves.add(new TileId(playerIdOfTile, 7));}
            }else { // or turn back
                System.out.println("GameState: OnMove: normal route is blocked by a start tile, move backwards");
                next = 15 - next%15;
                moves.add(new TileId(playerIdOfTile, 15));
                if(next < 13){moves.add(new TileId(playerIdOfTile, 13));}
                if(next < 7){moves.add(new TileId(playerIdOfTile, 7));}
            }

            TileId nextTileId = new TileId(playerIdOfTile, next);
            moves.add(nextTileId);
            if(canMoveToTile(pawnId1, nextTileId)){
                response.setMovePawn1(moves);
                processMove(pawnId1, new TileId(playerIdOfTile,next), moveMessage, response, goToNextPlayer);
            }else{
                response.setResult(CANNOT_MAKE_MOVE);
            }
            return;
        }

        // normal route within section
        if(next > 0 &&
                next <= 15 &&
                !isPawnOnFinish(pawnId1, currentTileId)){
            System.out.println("GameState: OnMove: normal route between 0,15");
            // check if you can kill an opponent
            TileId nextTileId = new TileId(playerIdOfTile, next);

            // in case you end up on your own pawn
            if(!canMoveToTile(pawnId1,nextTileId)){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            if(nrSteps > 0) {
                if (next > 1 && currentTileId.getTileNr() < 1) {
                    moves.add(new TileId(playerIdOfTile, 1));
                }
                if (next > 7 && currentTileId.getTileNr() < 7) {
                    moves.add(new TileId(playerIdOfTile, 7));
                }
                if (next > 13 && currentTileId.getTileNr() < 13) {
                    moves.add(new TileId(playerIdOfTile, 13));
                }
            }else{
                if(next < 13 && currentTileId.getTileNr() > 13){moves.add(new TileId(playerIdOfTile, 13));}
                if(next < 7 && currentTileId.getTileNr() > 7){moves.add(new TileId(playerIdOfTile, 7));}
                if(next < 1 && currentTileId.getTileNr() > 1){moves.add(new TileId(playerIdOfTile, 1));}
            }

            moves.add(nextTileId);
            response.setMovePawn1(moves);

            processMove(pawnId1, new TileId(playerIdOfTile,next), moveMessage, response, goToNextPlayer);

            return;
        }

        // you go negative
        if(next < 0){
            System.out.println("GameState: OnMove: pawn goes backwards");
            if(currentTileId.getTileNr() > 1){moves.add(new TileId(playerIdOfTile, 1));}

            // check if you can pass || otherwise turn back i.e. forward
            startTileId = new TileId(playerIdOfTile, 0);
            if (canPassStartTile(pawnId1, startTileId)){
                next = 16 + next;
                playerIdOfTile = previousPlayerId(playerIdOfTile);
                if(next < 13){moves.add(new TileId(playerIdOfTile, 13));}
            }else { // or turn back (forwards since next is negative)
                System.out.println("GameState: OnMove: pawn wants to go backwards but is blocked by a start tile, goes forwards");
                next = -next+2; // +1 : you can't move on tile 0 and would then move on tile 1 twice.
            }

            TileId nextTileId = new TileId(playerIdOfTile, next);
            moves.add(nextTileId);
            if(canMoveToTile(pawnId1, nextTileId)){
                response.setMovePawn1(moves);
                processMove(pawnId1, nextTileId, moveMessage, response, goToNextPlayer);
            }else{
                response.setResult(CANNOT_MAKE_MOVE);
            }
            return;
        }

        // when moving backwards and ending exactly on the starttile
        if(next == 0){
            System.out.println("GameState: OnMove: pawn ends exactly on start tile");
            if(currentTileId.getTileNr() > 1){moves.add(new TileId(playerIdOfTile, 1));}
            if (canMoveToTile(pawnId1, new TileId(playerIdOfTile,0))) {
                moves.add(new TileId(playerIdOfTile, 0));
                response.setMovePawn1(moves);
                processMove(pawnId1, new TileId(playerIdOfTile,0), moveMessage, response, goToNextPlayer);
                return;
            }

            // if your own pawn is on start, but it is not a blockade: your move is invalid
            if(!tileIsABlockade(new TileId(playerIdOfTile,0)) && cannotMoveToTileBecauseSamePlayer(pawnId1, new TileId(playerIdOfTile,0))){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            // move forwards to tile 2 if you can
            if(canMoveToTile(pawnId1, new TileId(playerIdOfTile, 2))){
                moves.add(new TileId(playerIdOfTile, 2));
                response.setMovePawn1(moves);
                processMove(pawnId1, new TileId(playerIdOfTile,2), moveMessage, response, goToNextPlayer);
                return;
            }else{
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
        }

        // pawn is already on finish
        if (isPawnOnFinish(pawnId1, currentTileId)){
            System.out.println("GameState: OnMove: pawn is already on the finish");
            // moving is not possible when the pawn is directly between two other pawns
            if (isPawnTightlyClosedIn(pawnId1, currentTileId)){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            // moving between pawns on the finish tile
            if(isPawnLooselyClosedIn(pawnId1, currentTileId)){
                ArrayList<TileId> pingpongmoves = pingpongMove(pawnId1, currentTileId, nrSteps);
                moves.clear();
                moves.addAll(pingpongmoves);// todo is this necessary?
                response.setMovePawn1(moves);
                processMove(pawnId1, pingpongmoves.getLast(), moveMessage, response, goToNextPlayer);
                return;
            }

            TileId targetTileId = moveAndCheckEveryTile(pawnId1, currentTileId, nrSteps);
            int tileHighestTileNr = 0;
            if(nrSteps > 0){
                tileHighestTileNr = checkHighestTileNrYouCanMoveTo(pawnId1, currentTileId, nrSteps);
                if (tileHighestTileNr > targetTileId.getTileNr()) {
                    System.out.println("GameState: OnMove: pawn moves out of the finish");
                    moves.add(new TileId(playerIdOfTile, tileHighestTileNr));
                }
            }
            if (targetTileId.getTileNr() < 15) {moves.add(new TileId(targetTileId.getPlayerId(), 15));}
            if (targetTileId.getTileNr() < 13) {moves.add(new TileId(targetTileId.getPlayerId(), 13));}
            if (targetTileId.getTileNr() < 7) {moves.add(new TileId(targetTileId.getPlayerId(), 7));}

            moves.add(targetTileId);
            response.setMovePawn1(moves);
            processMove(pawnId1, targetTileId, moveMessage, response, goToNextPlayer);
            return;
        }

        if(next > 15 &&
                isPawnOnLastSection(playerId, playerIdOfTile)){
            System.out.println("GameState: OnMove: pawn is on last section and goes into finish");
            if(currentTileId.getTileNr() < 7){moves.add(new TileId(currentTileId.getPlayerId(), 7));}
            if(currentTileId.getTileNr() < 13){moves.add(new TileId(currentTileId.getPlayerId(), 13));}
            if(currentTileId.getTileNr() < 15){moves.add(new TileId(currentTileId.getPlayerId(), 15));}

            TileId targetTileId = moveAndCheckEveryTile(pawnId1, currentTileId, nrSteps);

            int tileHighestTileNr = checkHighestTileNrYouCanMoveTo(pawnId1, currentTileId, nrSteps);
            if(tileHighestTileNr > targetTileId.getTileNr()){
                if(tileHighestTileNr > 15){
                    // move to finish
                    moves.add(new TileId(nextPlayerId(playerIdOfTile), tileHighestTileNr));
                    // possibly move back out of finish
                    // otherwise if 16 was taken, then it would move (0,15) (1,15) (0,15) and then correctly back
                    if(targetTileId.getTileNr() < 15){moves.add(new TileId(targetTileId.getPlayerId(), 15));}
                }
                if(targetTileId.getTileNr() < 13){moves.add(new TileId(targetTileId.getPlayerId(), 13));}
                if(targetTileId.getTileNr() < 7){moves.add(new TileId(targetTileId.getPlayerId(), 7));}
            }

            if(cannotMoveToTileBecauseSamePlayer(pawnId1, targetTileId)){
                clearResponse(response);
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }

            moves.add(targetTileId);
            response.setMovePawn1(moves);
            processMove(pawnId1, targetTileId, moveMessage, response, goToNextPlayer);
        }
    }

    private static void clearResponse(MoveResponse response) {
        response.setPawnId1(null);
        response.setPawnId2(null);
        response.setMoveType(null);
        response.setMovePawn1(null);
        response.setMovePawn2(null);
    }

    public static int checkHighestTileNrYouCanMoveTo(PawnId pawnId, TileId tileId, int nrSteps) {
        int direction = 1;
        int tileNrToCheck = tileId.getTileNr();

        if (nrSteps < 0) {
            direction = -1;
            nrSteps = -nrSteps;
        }

        for (int i = 0; i < nrSteps; i++) {
            tileNrToCheck = tileNrToCheck + direction;

            if (!canMoveToTile(pawnId, new TileId(pawnId.getPlayerId(), tileNrToCheck))) {
                return tileNrToCheck - 1;
            }
        }
        return tileNrToCheck;
    }

    public static ArrayList<TileId> pingpongMove(PawnId pawnId, TileId tileId , int nrSteps){
        // it is already guaranteed that a pawn is loosely closed in on the finish tiles
        ArrayList<TileId> moves = new ArrayList<>();
        int direction = 1;
        int tileNrToCheck = tileId.getTileNr();
        moves.add(tileId);
        if (nrSteps < 0) {
            direction = -1;
            nrSteps = Math.abs(nrSteps);
        }

        for (int i = 0; i < nrSteps; i++) {
            tileNrToCheck = tileNrToCheck + direction;
            if(!canMoveToTile(pawnId, new TileId(pawnId.getPlayerId(), tileNrToCheck))) {
                moves.add(new TileId(pawnId.getPlayerId(), tileNrToCheck-direction));
                direction = - direction;
                tileNrToCheck = tileNrToCheck + 2*direction;
            }
        }
        moves.add(new TileId(pawnId.getPlayerId(), tileNrToCheck));

        // an extra check to see if the first two moves are identical. this can happen when you do -4 steps and are
        // closed in from behind or try to move forward but are blocked that way.
        if(moves.size() >= 2){
            if(moves.get(0).equals(moves.get(1))){
                moves.removeFirst();
            }
        }
        return moves;
    }

    public static TileId moveAndCheckEveryTile(PawnId pawnId, TileId tileId, int nrSteps){
        int direction = 1;
        int tileNrToCheck = tileId.getTileNr();

        if (nrSteps<0){
            direction = -1;
            nrSteps = - nrSteps;
        }

        for (int i = 0; i < nrSteps; i++) {
            tileNrToCheck = tileNrToCheck + direction;
            if(tileNrToCheck > 15){// only check tiles when they are on the finish
                if(!canMoveToTile(pawnId, new TileId(pawnId.getPlayerId(), tileNrToCheck))) {
                    direction = - direction;
                    tileNrToCheck = tileNrToCheck + 2*direction;
                }
            }
        }

        if(tileNrToCheck <= 15){// when back on the last section, change the playerId of the section
            return new TileId(previousPlayerId(pawnId.getPlayerId()), tileNrToCheck);
        }

        return new TileId(pawnId.getPlayerId(), tileNrToCheck);

    }

    public static void processOnForfeit(MoveMessage message){
        CardsDeck.forfeitCardsForPlayer(message.getPlayerId());
        GameState.forfeitPlayer(message.getPlayerId());
    }

    public static void processOnSwitch(MoveMessage moveMessage, MoveResponse moveResponse){
        PawnId pawnId1 = moveMessage.getPawnId1();
        PawnId pawnId2 = moveMessage.getPawnId2();
        Card card = moveMessage.getCard();

        // invalid selection
        if(pawnId1 == null || card == null || pawnId2 == null){
            moveResponse.setResult(INVALID_SELECTION);
            return;
        }

        String selectedPawnPlayerId1 = moveMessage.getPawnId1().getPlayerId();
        String selectedPawnPlayerId2 = moveMessage.getPawnId2().getPlayerId();
        String playerId = moveMessage.getPlayerId();
        moveResponse.setMoveType(SWITCH);

        // You can't switch with yourself
        if(Objects.equals(selectedPawnPlayerId1, selectedPawnPlayerId2)){
            moveResponse.setResult(CANNOT_MAKE_MOVE);
            return;
        }
        // You can't switch two opponents
        if(!playerId.equals(selectedPawnPlayerId1) && (!playerId.equals(selectedPawnPlayerId2))){
            moveResponse.setResult(CANNOT_MAKE_MOVE);
            return;
        }
        if(playerDoesNotHaveCard(playerId, card)) {
            moveResponse.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        Pawn pawn1 = getPawn(pawnId1);
        Pawn pawn2 = getPawn(pawnId2);

        // player 1 cannot move from EndTile or from NestTile
        // player 2 cannot move from endtile or from nesttile
        int tileNr1 = pawn1.getCurrentTileId().getTileNr();
        int tileNr2 = pawn2.getCurrentTileId().getTileNr();
        String tilePlayerId2 = pawn2.getCurrentTileId().getPlayerId();
        if(tileNr1 < 0 || tileNr2 < 0 || tileNr1 > 15 || tileNr2 > 15){
            moveResponse.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        // player1 can move from starttile
        // player2 cannot be taken from starttile
        if(tilePlayerId2.equals(pawn2.getPlayerId()) && tileNr2 == 0){
            moveResponse.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        LinkedList<TileId> move1 = new LinkedList<>();
        LinkedList<TileId> move2 = new LinkedList<>();

        move1.add(pawn1.getCurrentTileId());
        move1.add(pawn2.getCurrentTileId());

        move2.add(pawn2.getCurrentTileId());
        move2.add(pawn1.getCurrentTileId());

        moveResponse.setMovePawn1(move1);
        moveResponse.setMovePawn2(move2);
        moveResponse.setPawnId1(pawn1.getPawnId());
        moveResponse.setPawnId2(pawn2.getPawnId());

        TileId tileId1 = new TileId(pawn1.getCurrentTileId());
        TileId tileId2 = new TileId(pawn2.getCurrentTileId());
        // switch in gamestate
        // only use the card when not testing
        if(moveMessage.getMessageType() == MAKE_MOVE){
            movePawn(new Pawn(pawnId1,tileId2));
            movePawn(new Pawn(pawnId2,tileId1));
            Boolean playerHasNoCardsLeft = CardsDeck.playerPlaysCard(playerId, card);
            if(playerHasNoCardsLeft){
                forfeitPlayer(playerId);
            }else{
                nextActivePlayer();
            }
        }
    }

    public static void processOnBoard(MoveMessage moveMessage, MoveResponse response) {
        PawnId pawnId1 = moveMessage.getPawnId1();
        Card card = moveMessage.getCard();
        String playerId = moveMessage.getPlayerId();

        // invalid selection
        if(pawnId1 == null || card == null){
            response.setResult(INVALID_SELECTION);
            return;
        }

        // player should have the card he's playing
        if(playerDoesNotHaveCard(playerId, card)) {
            response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        // cannot go onboard without an Ace or King
        int cardValue = card.getCardValue();
        if(!(isAce(card) || isKing(card))){
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        TileId currentTileId = getPawn(pawnId1).getCurrentTileId();
        TileId targetTileId = new TileId(playerId,0);
        response.setMoveType(ONBOARD);

        // when occupied by own pawn
        if(!canMoveToTile(pawnId1, targetTileId)){
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        // when pawn not in the nest
        if(currentTileId.getTileNr() >= 0 ){
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        LinkedList<TileId> move = new LinkedList<>();
        move.add(currentTileId);
        move.add(targetTileId);

        response.setPawnId1(pawnId1);
        response.setMovePawn1(move);
        processMove(pawnId1, targetTileId, moveMessage, response);
    }

    public static void processMove(PawnId pawnId, TileId targetTileId, MoveMessage moveMessage, MoveResponse response){
        processMove(pawnId, targetTileId, moveMessage, response, true);
    }

    public static void processMove(PawnId pawnId, TileId targetTileId, MoveMessage moveMessage, MoveResponse response, boolean goToNextPlayer){
        String playerId = moveMessage.getPlayerId();
        Card card = moveMessage.getCard();

        if(cannotMoveToTileBecauseSamePlayer(pawnId, targetTileId)){
            clearResponse(response);
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }
        if(playerDoesNotHaveCard(playerId, card)){
            clearResponse(response);
            response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        // check for kills
        Pawn pawn = getPawn(targetTileId);
        if(pawn != null){
            if(!Objects.equals(pawn.getPlayerId(), pawnId.getPlayerId())){
                response.setPawnId1(pawnId);
                response.setPawnId2(null);
                LinkedList<TileId> move2 = new LinkedList<>();
                move2.add(targetTileId);
                move2.add(pawn.getNestTileId());
                response.setPawnIdKilled1(pawn.getPawnId());
                response.setMoveKilledPawn1(move2);
                if(moveMessage.getMessageType() == MAKE_MOVE) {
                    movePawn(new Pawn(pawn.getPawnId(), pawn.getNestTileId()));
                }
            }
        }

        response.setPawnId1(pawnId);
        if(moveMessage.getMessageType() == MAKE_MOVE){
            movePawn(new Pawn(pawnId,targetTileId));
            Boolean playerHasNoCardsLeft = CardsDeck.playerPlaysCard(playerId, card);
            if(goToNextPlayer){// this is only false when you SPLIT a move, the second time calling it will make you go to the next player
                if(playerHasNoCardsLeft){
                    forfeitPlayer(playerId);
                }else{
                    nextActivePlayer();
                }
                checkForWinners(winners);
                removeWinnerFromActivePlayerList();
            }
        }

        response.setMessageType(moveMessage.getMessageType());
        response.setResult(CAN_MAKE_MOVE);
        System.out.println("GameState: pawn moves to "+targetTileId +", with response "+response);
    }

    public static void movePawn(Pawn selectedPawn){
        // set a pawn's location without triggering any validation
        // public for testing purposes
        for (Pawn pawn : pawns) {
            if (pawn.getPawnId().equals(selectedPawn.getPawnId())){ // equals only looks at pawnId
                pawn.setCurrentTileId(selectedPawn.getCurrentTileId());
                break;
            }
        }
    }

    public static void tearDown(){
        pawns = new ArrayList<>();
        playerIdTurn = "0";
        players.clear();
        activePlayers.clear();
        winners.clear();
        playerIdStartingRound = "0";
    }

    public static Pawn getPawn(Pawn selectedPawn){
        for (Pawn pawn : pawns) {
            if (pawn.equals(selectedPawn)){
                return pawn;
            }
        }
        return null;
    }
    public static Pawn getPawn(PawnId selectedPawnId){
        for (Pawn pawn : pawns) {
            if (pawn.getPawnId().equals(selectedPawnId)){
                return pawn;
            }
        }
        return null;
    }
    public static Pawn getPawn(TileId selectedTileId){
        for (Pawn pawn : pawns) {
            if (pawn.getCurrentTileId().equals(selectedTileId)){
                return pawn;
            }
        }
        return null;
    }

    public static boolean tileIsABlockade(TileId selectedStartTile){
        Pawn pawnOnStart = getPawn(selectedStartTile);
        if(pawnOnStart == null){
            return false;
        }
        if(Objects.equals(pawnOnStart.getPawnId().getPlayerId(), selectedStartTile.getPlayerId())){
            return true;
        }
        return false;
    }

    public static String nextPlayerId(String playerId){
        int playerInt = playerColors.get(playerId);
        return nextPlayerId(playerInt);
    }

    private static String nextPlayerId(int playerInt){
        int nextPlayerInt = (playerInt + 1) % players.size();
        return playerColors.entrySet().stream()
                .filter(entry -> entry.getValue().equals(nextPlayerInt))
                .map(HashMap.Entry::getKey).findFirst().orElse("0");
    }

    public static ArrayList<String> getWinners() {
        return winners;
    }
}
