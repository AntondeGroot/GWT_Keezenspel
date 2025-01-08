package ADG;

import ADG.*;

import java.util.LinkedList;
import java.util.ArrayList;

import static ADG.CardsDeck.playerHasCard;
import static ADG.MessageType.*;
import static ADG.MoveResult.*;
import static ADG.MoveType.*;
import static ADG.logic.StartTileLogic.canPassStartTile;
import static ADG.logic.WinnerLogic.checkForWinners;

public class GameState {

    private static ArrayList<Pawn> pawns = new ArrayList<>();
    private static int playerIdTurn;
    private static ArrayList<Player> players = new ArrayList<>();
    private static ArrayList<Integer> activePlayers = new ArrayList<>();
    private static ArrayList<Integer> winners = new ArrayList<>();
    private static int MAX_PLAYERS = 8;

    public GameState() {}

    public static void start(){
        if (pawns.isEmpty()) {
            pawns = new ArrayList<Pawn>();
            for (int playerId = 0; playerId < players.size(); playerId++) {
                activePlayers.add(playerId);
                for (int pawnNr = 0; pawnNr < 4; pawnNr++) {
                    pawns.add(new Pawn(
                            new PawnId(playerId, pawnNr),
                            new TileId(playerId,-1 - pawnNr)));
                }
            }

            CardsDeck.setNrPlayers(players.size());
            CardsDeck.shuffle();
            CardsDeck.dealCards();
        }
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }

    public static void addPlayer(Player player) {
        if(!players.contains(player) && players.size() < MAX_PLAYERS){
            players.add(player);
        }
    }

    public static ArrayList<Integer> getActivePlayers() {
        return activePlayers;
    }

    public static void resetActivePlayers(){
        // todo: old
        for (int playerId = 0; playerId < players.size(); playerId++) {
            if(!winners.contains(playerId)){
                activePlayers.add(playerId);
            }
        }
        // todo: new
        for(Player player : players){
            if(!player.hasFinished()){
                player.setActive();
            }
        }
    }

    public static void forfeitPlayer(int playerId) {
        // todo: modernize
        if(activePlayers.contains(playerId)){
            activePlayers.remove((Integer) playerId);
            players.get(playerId).setInActive();
        }
        if(activePlayers.isEmpty()){
            resetActivePlayers();
            CardsDeck.shuffle();
            CardsDeck.dealCards();
        }
        nextActivePlayer();
    }

    private static void removeWinnerFromActivePlayerList(){
        for (Integer winner_i: winners){
            if(activePlayers.contains(winner_i)){
                activePlayers.remove(winner_i);
            }
        }
    }

    public static void nextActivePlayer() {
        playerIdTurn = nextPlayerId(playerIdTurn);
        if(!activePlayers.isEmpty() && !activePlayers.contains(playerIdTurn)){
            nextActivePlayer();
        }
        // todo: check if all players have finished
        // update player with PlayerId to be playing
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setIsPlaying(i==playerIdTurn);
        }
    }

    public static int getPlayerIdTurn() {
        return playerIdTurn;
    }

    public static ArrayList<Pawn> getPawns() {
        return pawns;
    }

    private static boolean isPawnOnLastSection(int playerId, int sectionId){
        return sectionId == previousPlayerId(playerId);
    }

    private static int previousPlayerId(int playerId){
        return (playerId + players.size() -1) % players.size();
    }

    private static boolean isPawnOnFinish(PawnId pawnId, TileId tileId){
        return (pawnId.getPlayerId() == tileId.getPlayerId() && tileId.getTileNr() > 15);
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
                && !canMoveToTile(pawnId,new TileId(pawnId.getPlayerId(), 18))
                && !canMoveToTile(pawnId,new TileId(pawnId.getPlayerId(), 16))){
            return true;
        }

        return false;
    }

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

            if (pawn.getPlayerId() == selectedPawnId.getPlayerId()) {
                return false;
            }
            if (pawn.getPlayerId() == nextTileId.getPlayerId() && nextTileId.getTileNr() == 0){
                return false;
            }
        }
        return true;
    }

    private static boolean canMoveToTileBecauseSamePlayer(PawnId selectedPawnId, TileId nextTileId){
        Pawn pawn = getPawn(nextTileId);
        if(pawn != null) {
            if (pawn.getPlayerId() == selectedPawnId.getPlayerId() && !pawn.getPawnId().equals(selectedPawnId)) {
                return false;
            }
        }
        return true;
    }

    public static TileId getPawnTileId(PawnId pawnId){
        for(Pawn pawn : pawns){
            if(pawn.getPawnId().equals(pawnId)){
                return pawn.getCurrentTileId();
            }
        }
        return null;
    }

    public static void processOnMove(MoveMessage moveMessage, MoveResponse response){
        PawnId pawnId1 = moveMessage.getPawnId1();
        Card card = moveMessage.getCard();
        if(pawnId1 == null || card == null){
            response.setResult(INVALID_SELECTION);
            return;
        }

        int playerId = pawnId1.getPlayerId();
        TileId currentTileId = getPawnTileId(pawnId1);
        int nrSteps = moveMessage.getStepsPawn1();
        int next = 0;
        int playerIdOfTile = currentTileId.getPlayerId();
        System.out.println("moveMessage = "+moveMessage);
        int direction = 1;
        int tileNrToCheck = currentTileId.getTileNr();
        LinkedList<TileId> moves = new LinkedList<>();
        response.setMoveType(MOVE);
        System.out.println("GameState: OnMove: received msg: " + moveMessage);
        TileId startTileId = new TileId(-99,-99);

        // You cannot move from nest tiles
        if(currentTileId.getTileNr() < 0){
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        // Player must have the card he wants to play
        if(!playerHasCard(playerId, card)) {
            response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        // Player cannot move an opponents pawn without playing a Jack
        if(card.getCardValue() != 10){
            if(moveMessage.getPawnId1() != null && moveMessage.getPawnId1().getPlayerId() != playerId){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            if(moveMessage.getPawnId2() != null && moveMessage.getPawnId2().getPlayerId() != playerId){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
        }

        moves.add(currentTileId);
        next = currentTileId.getTileNr() + moveMessage.getStepsPawn1();

         // regular route
        if (next > 15 && !isPawnOnLastSection(playerId, playerIdOfTile) && !isPawnOnFinish(pawnId1, currentTileId) ) {
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
                next = (15 - next%15);
                moves.add(new TileId(playerIdOfTile, 15));
                if(next < 13){moves.add(new TileId(playerIdOfTile, 13));}
                if(next < 7){moves.add(new TileId(playerIdOfTile, 7));}
            }

            TileId nextTileId = new TileId(playerIdOfTile, next);
            moves.add(nextTileId);
            if(canMoveToTile(pawnId1, nextTileId)){
                response.setMovePawn1(moves);
                processMove(pawnId1, new TileId(playerIdOfTile,next), moveMessage, response);
            }else{
                response.setResult(CANNOT_MAKE_MOVE);
            }
            return;
        }

        // normal route within section
        if(next > 0 && next <= 15 && !isPawnOnFinish(pawnId1, currentTileId)){
            System.out.println("GameState: OnMove: normal route between 0,15");
            // check if you can kill an opponent
            TileId nextTileId = new TileId(playerIdOfTile, next);

            // in case you end up on your own pawn
            if(!canMoveToTile(pawnId1,nextTileId)){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            if(nrSteps >0) {
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

            processMove(pawnId1, new TileId(playerIdOfTile,next), moveMessage, response);

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
                processMove(pawnId1, nextTileId, moveMessage, response);
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
                processMove(pawnId1, new TileId(playerIdOfTile,0), moveMessage, response);
                return;
            }

            // if your own pawn is on start, but it is not a blockade: your move is invalid
            if(!tileIsABlockade(new TileId(playerIdOfTile,0)) && !canMoveToTileBecauseSamePlayer(pawnId1, new TileId(playerIdOfTile,0))){
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }
            // move forwards to tile 2 if you can
            if(canMoveToTile(pawnId1, new TileId(playerIdOfTile, 2))){
                moves.add(new TileId(playerIdOfTile, 2));
                response.setMovePawn1(moves);
                processMove(pawnId1, new TileId(playerIdOfTile,2), moveMessage, response);
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
            processMove(pawnId1, targetTileId, moveMessage, response);
            return;
        }

        if((next > 15 && isPawnOnLastSection(playerId, playerIdOfTile))){
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

            if(!canMoveToTileBecauseSamePlayer(pawnId1, targetTileId)){
                clearResponse(response);
                response.setResult(CANNOT_MAKE_MOVE);
                return;
            }

            moves.add(targetTileId);
            response.setMovePawn1(moves);
            processMove(pawnId1, targetTileId, moveMessage, response);
            return;
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
        // invalid selection
        PawnId pawnId1 = moveMessage.getPawnId1();
        PawnId pawnId2 = moveMessage.getPawnId2();
        Card card = moveMessage.getCard();
        if(pawnId1 == null || card == null || pawnId2 == null){
            moveResponse.setResult(INVALID_SELECTION);
            return;
        }

        int selectedPawnPlayerId1 = moveMessage.getPawnId1().getPlayerId();
        int selectedPawnPlayerId2 = moveMessage.getPawnId2().getPlayerId();
        int playerId = moveMessage.getPlayerId();
        moveResponse.setMoveType(SWITCH);

        // You can't switch with yourself
        if(selectedPawnPlayerId1 == selectedPawnPlayerId2){
            moveResponse.setResult(CANNOT_MAKE_MOVE);
            return;
        }
        // You can't switch two opponents
        if((selectedPawnPlayerId1 != playerId) && (selectedPawnPlayerId2 != playerId)){
            moveResponse.setResult(CANNOT_MAKE_MOVE);
            return;
        }
        if(!playerHasCard(moveMessage.getPlayerId(), card)) {
            moveResponse.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        // assume the player always controls pawn1
        MoveMessage newMoveMessage = new MoveMessage();
        if(playerId != selectedPawnPlayerId1){
            newMoveMessage.setPlayerId(moveMessage.getPlayerId());
            newMoveMessage.setPawnId1(moveMessage.getPawnId1());
            newMoveMessage.setPawnId2(moveMessage.getPawnId2());
            newMoveMessage.setMoveType(moveMessage.getMoveType());
        }else{
            newMoveMessage = moveMessage;
        }

        Pawn pawn1 = getPawn(pawnId1);
        Pawn pawn2 = getPawn(pawnId2);

        // player 1 cannot move from EndTile or from NestTile
        // player 2 cannot move from endtile or from nesttile
        int tileNr1 = pawn1.getCurrentTileId().getTileNr();
        int tileNr2 = pawn2.getCurrentTileId().getTileNr();
        int tilePlayerId2 = pawn2.getCurrentTileId().getPlayerId();
        if(tileNr1 < 0 || tileNr2 < 0 || tileNr1 > 15 || tileNr2 > 15){
            moveResponse.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        // player1 can move from starttile
        // player2 cannot be taken from starttile
        if(tilePlayerId2 == pawn2.getPlayerId() && tileNr2 == 0){
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
            CardsDeck.playerPlaysCard(moveMessage.getPlayerId(), card);
            nextActivePlayer();
        }
    }

    public static void processOnBoard(MoveMessage moveMessage, MoveResponse response) {
        // invalid selection
        PawnId pawnId1 = moveMessage.getPawnId1();
        Card card = moveMessage.getCard();
        if(pawnId1 == null || card == null){
            response.setResult(INVALID_SELECTION);
            return;
        }

        // player should have the card he's playing
        if(!playerHasCard(moveMessage.getPlayerId(), card)) {
            response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        // cannot go onboard without an Ace or King
        int cardValue = card.getCardValue();
        if(!(cardValue == 0 || cardValue == 12)){
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }

        System.out.println(""+System.lineSeparator());
        int playerId = pawnId1.getPlayerId();
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
        // VALIDATION
        if(!canMoveToTileBecauseSamePlayer(pawnId, targetTileId)){
            clearResponse(response);
            response.setResult(CANNOT_MAKE_MOVE);
            return;
        }
        if(!playerHasCard(moveMessage.getPlayerId(), moveMessage.getCard())){
            clearResponse(response);
            response.setResult(PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        // check for kills
        Pawn pawn = getPawn(targetTileId);
        if(pawn != null){
            if(pawn.getPlayerId() != pawnId.getPlayerId()){
                response.setPawnId1(pawnId);
                response.setPawnId2(pawn.getPawnId());
                LinkedList<TileId> move2 = new LinkedList<>();
                move2.add(targetTileId);
                move2.add(pawn.getNestTileId());
                response.setMovePawn2(move2);
                if(moveMessage.getMessageType() == MAKE_MOVE) {
                    movePawn(new Pawn(pawn.getPawnId(), pawn.getNestTileId()));
                }
            }
        }

        response.setPawnId1(pawnId);
        if(moveMessage.getMessageType() == MAKE_MOVE){
            movePawn(new Pawn(pawnId,targetTileId));
            CardsDeck.playerPlaysCard(moveMessage.getPlayerId(), moveMessage.getCard());
            checkForWinners(winners);
            removeWinnerFromActivePlayerList();
            nextActivePlayer();
        }

        printAllPawnsNotOnNests();
        response.setMessageType(moveMessage.getMessageType());
        response.setResult(CAN_MAKE_MOVE);
        System.out.println("GameState: pawn moves to "+targetTileId +", with response "+response);
    }

    public static void movePawn(Pawn selectedPawn){
        // set a pawn's location without triggering any validation
        // public for testing purposes
        for (Pawn pawn : pawns) {
            if (pawn.equals(selectedPawn)){ // equals only looks at pawnId
                pawn.setCurrentTileId(selectedPawn.getCurrentTileId());
                break;
            }
        }
    }

    public static void tearDown(){
        pawns = new ArrayList<>();
        playerIdTurn = 0;
        players.clear();
        activePlayers.clear();
        winners.clear();
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

    public static void printAllPawnsNotOnNests(){
        for (Pawn pawn : pawns) {
            if (pawn.getCurrentTileId().getTileNr() >= 0){
                System.out.println("pawn not on nest: "+pawn);
            }
        }
    }

    public static boolean tileIsABlockade(TileId selectedStartTile){
        Pawn pawnOnStart = getPawn(selectedStartTile);
        if(pawnOnStart == null){
            return false;
        }
        if(pawnOnStart.getPawnId().getPlayerId() == selectedStartTile.getPlayerId()){
            return true;
        }
        return false;
    }

    public static int nextPlayerId(int playerId){
        return (playerId + 1)%players.size();
    }

    public static ArrayList<Integer> getWinners() {
        return winners;
    }
}
