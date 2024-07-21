package gwtks;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GameState {

    private static List<Pawn> pawns = new ArrayList<>();
    private static int playerIdTurn;
    private static int nrPlayers;
    private static List<TileId> tiles = new ArrayList<>();

    public GameState(int nrPlayers) {
        if (pawns.isEmpty()) {
            PawnId pawnId = new PawnId();

            GameState.nrPlayers = nrPlayers;
            pawns = new ArrayList<Pawn>();
            for (int playerId = 0; playerId < nrPlayers; playerId++) {
                for (int pawnNr = 0; pawnNr < 4; pawnNr++) {
                    pawns.add(new Pawn(
                            new PawnId(playerId, pawnNr),
                            new TileId(playerId,-1 - pawnNr)));
                }
            }
            CardsDeck.setNrPlayers(nrPlayers);
            CardsDeck.shuffle();
            CardsDeck.dealCards();
        }
    }

    public Integer nextTurn() {
        playerIdTurn = (playerIdTurn + 1) % nrPlayers;
        return playerIdTurn;
    }

    public int getPlayerIdTurn() {
        return playerIdTurn;
    }

    public List<Pawn> getPawns() {
        return pawns;
    }

    private static boolean isPawnOnLastSection(int playerId, int sectionId){
        return sectionId == previousPlayerId(playerId);
    }

    private static int previousPlayerId(int playerId){
        return (playerId + nrPlayers -1) % nrPlayers;
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

    private static boolean canMoveToTileBecauseSamePawn(PawnId selectedPawnId, TileId nextTileId){
        Pawn pawn = getPawn(nextTileId);
        if(pawn != null) {
            if (pawn.getPlayerId() == selectedPawnId.getPlayerId()) {
                return false;
            }
        }
        return true;
    }

    public static void processOnMove(MoveMessage moveMessage, MoveResponse response){
        PawnId pawnId1 = moveMessage.getPawnId1();
        int playerId = pawnId1.getPlayerId();
        TileId currentTileId = moveMessage.getTileId();
        int nrSteps = moveMessage.getStepsPawn1();
        int next = 0;
        int playerIdOfTile = moveMessage.getTileId().getPlayerId();
        System.out.println("moveMessage = "+moveMessage);
        int direction = 1;
        int tileNrToCheck = currentTileId.getTileNr();
        LinkedList<TileId> moves = new LinkedList<>();
        response.setMoveType(MoveType.MOVE);
        System.out.println("GameState: OnMove: received msg: " + moveMessage);

        // You cannot move from nest tiles
        if(currentTileId.getTileNr() < 0){
            response.setResult(MoveResult.CANNOT_MAKE_MOVE);
            return;
        }

        if(!CardsDeck.playerHasCard(moveMessage.getPlayerId(), moveMessage.getCard())) {
            System.out.println("playerId"+moveMessage.getPlayerId());
            response.setResult(MoveResult.PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        moves.add(currentTileId);
        next = moveMessage.getTileId().getTileNr() + moveMessage.getStepsPawn1();

         // regular route
        if (next > 15 && !isPawnOnLastSection(playerId, playerIdOfTile) && !isPawnOnFinish(pawnId1, currentTileId) ) {
            System.out.println("GameState: OnMove: normal route between 0,15 but could move to next section");
            // check
            if(!canMoveToTileBecauseSamePawn(pawnId1, new TileId(playerId+1, 0))&&next==16){
                response.setResult(MoveResult.CANNOT_MAKE_MOVE);
                return;
            }

            if(currentTileId.getTileNr() < 1){moves.add(new TileId(currentTileId.getPlayerId(), 1));}
            if(currentTileId.getTileNr() < 7){moves.add(new TileId(currentTileId.getPlayerId(), 7));}
            if(currentTileId.getTileNr() < 13){moves.add(new TileId(currentTileId.getPlayerId(), 13));}
            if(currentTileId.getTileNr() < 15){moves.add(new TileId(currentTileId.getPlayerId(), 15));}

            if (canMoveToTile(pawnId1, new TileId(playerIdOfTile+1,0))) {
                System.out.println("GameState: OnMove: can move past StartTile "+new TileId(playerIdOfTile+1,0));
                System.out.println("GameState: OnMove: normal route can move to the next section");
                next = next % 16;
                playerIdOfTile++;
                playerIdOfTile = playerIdOfTile % nrPlayers;
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
                response.setResult(MoveResult.CANNOT_MAKE_MOVE);
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
                response.setResult(MoveResult.CANNOT_MAKE_MOVE);
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
            if (canMoveToTile(pawnId1, new TileId(playerIdOfTile,0))) {
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
                response.setResult(MoveResult.CANNOT_MAKE_MOVE);
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
            else{
                response.setResult(MoveResult.CANNOT_MAKE_MOVE);
                return;
            }
        }

        // pawn is already on finish
        if (isPawnOnFinish(pawnId1, currentTileId)){
            System.out.println("GameState: OnMove: pawn is already on the finish");
            // moving is not possible when the pawn is directly between two other pawns
            if (isPawnTightlyClosedIn(pawnId1, currentTileId)){
                response.setResult(MoveResult.CANNOT_MAKE_MOVE);
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

            int tileHighestTileNr = checkHighestTileNrYouCanMoveTo(pawnId1,currentTileId,nrSteps);
            if(tileHighestTileNr > targetTileId.getTileNr()){
                moves.add(new TileId((playerIdOfTile+1)%8, tileHighestTileNr));
                if(targetTileId.getTileNr() < 15){moves.add(new TileId(targetTileId.getPlayerId(), 15));}
                if(targetTileId.getTileNr() < 13){moves.add(new TileId(targetTileId.getPlayerId(), 13));}
                if(targetTileId.getTileNr() < 7){moves.add(new TileId(targetTileId.getPlayerId(), 7));}
            }
            moves.add(targetTileId);
            response.setMovePawn1(moves);
            processMove(pawnId1, targetTileId, moveMessage, response);
            return;
        }
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

            if(!canMoveToTile(pawnId, new TileId(pawnId.getPlayerId(), tileNrToCheck))) {
                direction = - direction;
                tileNrToCheck = tileNrToCheck + 2*direction;
            }
        }

        if(tileNrToCheck <= 15){// when back on the last section, change the playerId of the section
            return new TileId(previousPlayerId(pawnId.getPlayerId()), tileNrToCheck);
        }

        return new TileId(pawnId.getPlayerId(), tileNrToCheck);

    }

    public static void processOnSwitch(MoveMessage moveMessage, MoveResponse moveResponse){
        if(moveMessage.getPawnId1() == null || moveMessage.getPawnId2() == null){
            moveResponse.setResult(MoveResult.CANNOT_MAKE_MOVE);
            return;
        }

        int selectedPawnPlayerId1 = moveMessage.getPawnId1().getPlayerId();
        int selectedPawnPlayerId2 = moveMessage.getPawnId2().getPlayerId();
        int playerId = moveMessage.getPlayerId();
        moveResponse.setMoveType(MoveType.SWITCH);

        // You can't switch with yourself
        if(selectedPawnPlayerId1 == selectedPawnPlayerId2){
            moveResponse.setResult(MoveResult.CANNOT_MAKE_MOVE);
            return;
        }
        // You can't switch two opponents
        if((selectedPawnPlayerId1 != playerId) && (selectedPawnPlayerId2 != playerId)){
            moveResponse.setResult(MoveResult.CANNOT_MAKE_MOVE);
            return;
        }
        if(!CardsDeck.playerHasCard(moveMessage.getPlayerId(), moveMessage.getCard())) {
            moveResponse.setResult(MoveResult.PLAYER_DOES_NOT_HAVE_CARD);
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

        PawnId pawnId1 = newMoveMessage.getPawnId1();
        PawnId pawnId2 = newMoveMessage.getPawnId2();
        Pawn pawn1 = getPawn(pawnId1);
        Pawn pawn2 = getPawn(pawnId2);

        // player 1 cannot move from EndTile or from NestTile
        // player 2 cannot move from endtile or from nesttile
        int tileNr1 = pawn1.getCurrentTileId().getTileNr();
        int tileNr2 = pawn2.getCurrentTileId().getTileNr();
        int tilePlayerId2 = pawn2.getCurrentTileId().getPlayerId();
        if(tileNr1 < 0 || tileNr2 < 0 || tileNr1 > 15 || tileNr2 > 15){
            moveResponse.setResult(MoveResult.CANNOT_MAKE_MOVE);
            return;
        }

        // player1 can move from starttile
        // player2 cannot be taken from starttile
        if(tilePlayerId2 == pawn2.getPlayerId() && tileNr2 == 0){
            moveResponse.setResult(MoveResult.CANNOT_MAKE_MOVE);
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
        movePawn(new Pawn(pawnId1,tileId2));
        movePawn(new Pawn(pawnId2,tileId1));
    }

    public static void processOnBoard(MoveMessage moveMessage, MoveResponse response) {
        // player should have the card he's playing
        if(!CardsDeck.playerHasCard(moveMessage.getPlayerId(), moveMessage.getCard())) {
            response.setResult(MoveResult.PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }

        // cannot go onboard without an Ace or King
        int cardValue = moveMessage.getCard().getCard();
        if(!(cardValue == 0 || cardValue == 12)){
            response.setResult(MoveResult.CANNOT_MAKE_MOVE);
            return;
        }

        System.out.println(""+System.lineSeparator());
        PawnId pawnId1 = moveMessage.getPawnId1();
        int playerId = pawnId1.getPlayerId();
        TileId currentTileId = getPawn(pawnId1).getCurrentTileId();
        TileId targetTileId = new TileId(playerId,0);
        response.setMoveType(MoveType.ONBOARD);

        // when occupied by own pawn
        if(!canMoveToTile(pawnId1, targetTileId)){
            response.setResult(MoveResult.CANNOT_MAKE_MOVE);
            return;
        }

        // when pawn not in the nest
        if(currentTileId.getTileNr() >= 0 ){
            response.setResult(MoveResult.CANNOT_MAKE_MOVE);
            return;
        }

        LinkedList<TileId> move = new LinkedList<>();
        move.add(currentTileId);
        move.add(targetTileId);

        response.setPawnId1(moveMessage.getPawnId1());
        response.setMovePawn1(move);
        processMove(pawnId1, targetTileId, moveMessage, response);
    }

    public static void processMove(PawnId pawnId, TileId targetTileId, MoveMessage moveMessage, MoveResponse response){
        // check if player has the card
        if(!CardsDeck.playerHasCard(moveMessage.getPlayerId(), moveMessage.getCard())){
            response.setResult(MoveResult.PLAYER_DOES_NOT_HAVE_CARD);
            return;
        }else{
            // only use the card when not testing if the move is possible
            if(moveMessage.getMessageType() == MessageType.MAKE_MOVE){
                CardsDeck.playerPlaysCard(moveMessage.getPlayerId(), moveMessage.getCard());
            }
        }

        // check for kills
        System.out.println(""+System.lineSeparator());
        Pawn pawn = getPawn(targetTileId);
        if(pawn != null){
            if(pawn.getPlayerId() != pawnId.getPlayerId()){
                response.setPawnId1(pawnId);
                response.setPawnId2(pawn.getPawnId());
                LinkedList<TileId> move2 = new LinkedList<>();
                move2.add(targetTileId);
                move2.add(pawn.getNestTileId());
                response.setMovePawn2(move2);
                if(moveMessage.getMessageType() == MessageType.MAKE_MOVE) {
                    movePawn(new Pawn(pawn.getPawnId(), pawn.getNestTileId()));
                }
            }
        }

        response.setPawnId1(pawnId);
        if(moveMessage.getMessageType() == MessageType.MAKE_MOVE){
            movePawn(new Pawn(pawnId,targetTileId));
        }

        printAllPawnsNotOnNests();
        response.setMessageType(moveMessage.getMessageType());
        response.setResult(MoveResult.CAN_MAKE_MOVE);
        System.out.println("GameState: pawn moves to "+targetTileId +", with resposne "+response);
    }

    public static void movePawn(Pawn selectedPawn){
        // set a pawn's location without triggering any validation
        // public for testing purposes
        for (Pawn pawn : pawns) {
            if (pawn.equals(selectedPawn)){ // equals only looks at pawnId
                pawn.setCurrentTileId(selectedPawn.getCurrentTileId());
            }
        }
    }

    public static void tearDown(){
        pawns = new ArrayList<>();
        playerIdTurn = 0;
        nrPlayers = 0;
        tiles = new ArrayList<>();
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
}
