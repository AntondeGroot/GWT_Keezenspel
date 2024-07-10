package gwtks;

import java.util.ArrayList;
import java.util.List;

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
        // get the data

        PawnId pawnId1 = moveMessage.getPawnId1();
        int playerId = pawnId1.getPlayerId();
        TileId tileId = moveMessage.getTileId();
        int nrSteps = moveMessage.getStepsPawn1();

        int next = 0;
        int playerIdOfTile = moveMessage.getTileId().getPlayerId();
        System.out.println("moveMessage = "+moveMessage);
        int direction = 1;
        int tileNrToCheck = tileId.getTileNr();

        if (nrSteps<0){
            direction = -1;
            nrSteps = - nrSteps;
        }

        // You cannot move from nest tiles
        if(tileId.getTileNr() < 0){
            return;
        }

        if (moveMessage.getTileId() != null) {
            next = moveMessage.getTileId().getTileNr() + moveMessage.getStepsPawn1();
        }

         // regular route
        if (next > 15 && !isPawnOnLastSection(playerId, playerIdOfTile) && !isPawnOnFinish(pawnId1, tileId) ) {
            // check
            if(!canMoveToTileBecauseSamePawn(pawnId1, new TileId(playerId+1, 0))&&next==16){
                return;
            }

            if (canMoveToTile(pawnId1, new TileId(playerIdOfTile+1,0))) {
                next = next % 16;
                playerIdOfTile++;
                playerIdOfTile = playerIdOfTile % nrPlayers;
            }else { // or turn back
                next = (15 - next%15);
            }

            TileId nextTileId = new TileId(playerIdOfTile, next);
            if(canMoveToTile(pawnId1, nextTileId)){
                processMove(pawnId1, new TileId(playerIdOfTile,next), response);
            }
            return;
        }

        // normal route within section
        if(next > 0 && next <= 15 && !isPawnOnFinish(pawnId1, tileId)){
            System.out.println("normal route");
            // check if you can kill an opponent
            TileId nextTileId = new TileId(playerIdOfTile, next);

            // in case you end up on your own pawn
            if(!canMoveToTile(pawnId1,nextTileId)){
                return;
            }

            if(canMoveToTile(pawnId1, nextTileId)){
                processMove(pawnId1, new TileId(playerIdOfTile,next), response);
            }

            return;
        }

        // you go negative
        if(next < 0){
            // check if you can pass || otherwise turn back i.e. forward
            if (canMoveToTile(pawnId1, new TileId(playerIdOfTile,0))) {
                next = 16 + next;
                playerIdOfTile = previousPlayerId(playerIdOfTile);
            }else { // or turn back (forwards since next is negative)
                next = -next+2; // +1 : you can't move on tile 0 and would then move on tile 1 twice.
            }

            TileId nextTileId = new TileId(playerIdOfTile, next);
            if(canMoveToTile(pawnId1, nextTileId)){
                processMove(pawnId1, new TileId(playerIdOfTile,next), response);
            }
            return;
        }

        if(next == 0){
            if (canMoveToTile(pawnId1, new TileId(playerIdOfTile,0))) {
                processMove(pawnId1, new TileId(playerIdOfTile,0), response);
            }
        }

        // pawn is already on finish tile OR close to it
        if (isPawnOnFinish(pawnId1, tileId)){
            // moving is not possible when the pawn is directly between two other pawns
            if (isPawnTightlyClosedIn(pawnId1, tileId)){
                return;
            }

            TileId targetTileId = moveAndCheckEveryTile(pawnId1, tileId, nrSteps);
            processMove(pawnId1, targetTileId, response);
            return;
        }

        if((next > 15 && isPawnOnLastSection(playerId, playerIdOfTile))){
            TileId targetTileId = moveAndCheckEveryTile(pawnId1, tileId, nrSteps);
            processMove(pawnId1, targetTileId, response);
            return;
        }
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
        int selectedPawnPlayerId1 = moveMessage.getPawnId1().getPlayerId();
        int selectedPawnPlayerId2 = moveMessage.getPawnId2().getPlayerId();
        int playerId = moveMessage.getPlayerId();

        // You can't switch with yourself
        if(selectedPawnPlayerId1 == selectedPawnPlayerId2){
            return;
        }
        if((selectedPawnPlayerId1 != playerId) && (selectedPawnPlayerId2 != playerId)){
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
            return;
        }

        // player1 can move from starttile
        // player2 cannot be taken from starttile
        if(tilePlayerId2 == pawn2.getPlayerId() && tileNr2 == 0){
            return;
        }

        List<TileId> move1 = new ArrayList<>();
        List<TileId> move2 = new ArrayList<>();

        move1.add(pawn2.getCurrentTileId());
        move2.add(pawn1.getCurrentTileId());
        moveResponse.setMovePawn1(move1);
        moveResponse.setMovePawn2(move2);
        moveResponse.setPawnId1(pawn1.getPawnId());
        moveResponse.setPawnId2(pawn2.getPawnId());

        System.out.println("switching pawns: " + moveResponse);

        TileId tileId1 = new TileId(pawn1.getCurrentTileId());
        TileId tileId2 = new TileId(pawn2.getCurrentTileId());
        // switch in gamestate
        movePawn(new Pawn(pawnId1,tileId2));
        movePawn(new Pawn(pawnId2,tileId1));
    }

    public static void processOnBoard(MoveMessage moveMessage, MoveResponse response) {
        // get the data
        PawnId pawnId1 = moveMessage.getPawnId1();
        int playerId = pawnId1.getPlayerId();
        TileId currentTileId = getPawn(pawnId1).getCurrentTileId();
        TileId targetTileId = new TileId(playerId,0);

        if(!canMoveToTile(pawnId1, targetTileId)){
            return;
        }

        if(currentTileId.getTileNr() >= 0 ){
            // when not in nest
            return;
        }

        // if start is occupied by other player, kill that pawn

        List<TileId> move = new ArrayList<>();
        move.add(targetTileId);
        response.setMovePawn1(move);
        response.setPawnId1(moveMessage.getPawnId1());
        movePawn(new Pawn(pawnId1,targetTileId));
    }

    public static void processMove(PawnId pawnId, TileId targetTileId, MoveResponse response){
        // check for kills
        Pawn pawn = getPawn(targetTileId);
        if(pawn != null){
            if(pawn.getPlayerId() != pawnId.getPlayerId()){
                response.setPawnId1(pawnId);
                response.setPawnId2(pawn.getPawnId());
                List<TileId> move2 = new ArrayList<>();
                move2.add(pawn.getNestTileId());
                response.setMovePawn2(move2);

                movePawn(new Pawn(pawn.getPawnId(),pawn.getNestTileId()));
            }
        }

        List<TileId> move = new ArrayList<>();
        move.add(targetTileId);
        response.setPawnId1(pawnId);
        response.setMovePawn1(move);
//        storeToResponse(response, targetTileId.getPlayerId(), targetTileId.getTileNr() );
        movePawn(new Pawn(pawnId,targetTileId));
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
}
