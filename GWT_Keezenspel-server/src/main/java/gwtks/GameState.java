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

    private static void storeToResponse(MoveResponse response, int playerIdOfTile, int next){
        List<TileId> move = new ArrayList<>();
        move.add(new TileId(playerIdOfTile, next));
        response.setMovePawn1(move);
    }

    private static boolean canMoveToTile(PawnId selectedPawnId, TileId nextTileId){
        if(nextTileId.getTileNr() > 19){
            return false;
        }

        for (Pawn pawn : pawns) {
            if (pawn.getPawnId().equals(selectedPawnId)) {continue;}

            if (pawn.getCurrentTileId().equals(nextTileId)){
                // you can't beat a player off their own tiles or pass them
                return !(pawn.getPawnId().getPlayerId() == nextTileId.getPlayerId());
            }
        }
        return true;
    }

    public static void processOnMove(MoveMessage moveMessage, MoveResponse response){
        // get the data
        PawnId pawnId1 = moveMessage.getPawnId1();
        int playerId = pawnId1.getPlayerId();
        PawnId pawnId2 = moveMessage.getPawnId2();
        MoveType moveType = moveMessage.getMoveType();
        int stepsPawn1 = moveMessage.getStepsPawn1();
        int stepsPawn2 = moveMessage.getStepsPawn2();
        TileId tileId = moveMessage.getTileId();
        int nrSteps = moveMessage.getStepsPawn1();

        int next = 0;
        int playerIdOfTile = moveMessage.getTileId().getPlayerId();
        System.out.println("moveMessage = "+moveMessage);

        // You cannot move from nest tiles
        if(tileId.getTileNr() < 0){
            return;
        }

        if (moveMessage.getTileId() != null) {
            next = moveMessage.getTileId().getTileNr() + moveMessage.getStepsPawn1();
        }

         // regular route
        if (next > 15 && !isPawnOnLastSection(playerId, playerIdOfTile) && !isPawnOnFinish(pawnId1, tileId) ) {
            // check if you can pass || otherwise turn back
            if (canMoveToTile(pawnId1, new TileId(playerIdOfTile+1,0))) {
                next = next % 16; // account for tile 0
                playerIdOfTile++;
                playerIdOfTile = playerIdOfTile % nrPlayers;
            }else { // or turn back
                next = (15 - next%15); // you don't pass 0
            }

            TileId nextTileId = new TileId(playerIdOfTile, next);
            if(canMoveToTile(pawnId1, nextTileId)){
                response.setPawnId1(pawnId1);
                storeToResponse(response, playerIdOfTile, next );
                movePawn(new Pawn(pawnId1,new TileId(playerIdOfTile,next)));
            }
            return;
        }

        // normal route within section
        if(next > 0 && next <= 15 && !isPawnOnFinish(pawnId1, tileId)){
            System.out.println("normal route");
            // check if you can kill an opponent
            TileId nextTileId = new TileId(playerIdOfTile, next);
            if(canMoveToTile(pawnId1, nextTileId)){
                response.setPawnId1(pawnId1);
                storeToResponse(response, playerIdOfTile, next );
                movePawn(new Pawn(pawnId1,new TileId(playerIdOfTile,next)));
            }
            return;
        }

        // you go negative
        if(next < 0){
            System.out.println("pawn is going negative");
            // check if you can pass || otherwise turn back i.e. forward
            if (canMoveToTile(pawnId1, new TileId(playerIdOfTile,0))) {
                next = 16 + next;
                playerIdOfTile = previousPlayerId(playerIdOfTile);
            }else { // or turn back (forwards since next is negative)
                next = -next+2; // +1 : you can't move on tile 0 and would then move on tile 1 twice.
            }

            TileId nextTileId = new TileId(playerIdOfTile, next);
            if(canMoveToTile(pawnId1, nextTileId)){
                response.setPawnId1(pawnId1);
                storeToResponse(response, playerIdOfTile, next );
                movePawn(new Pawn(pawnId1,new TileId(playerIdOfTile,next)));
            }
            return;
        }

        if(next == 0){
            if (canMoveToTile(pawnId1, new TileId(playerIdOfTile,0))) {
                response.setPawnId1(pawnId1);
                storeToResponse(response, playerIdOfTile, next );
                movePawn(new Pawn(pawnId1,new TileId(playerIdOfTile,next)));
            }
        }

        // pawn is already on finish tile OR close to it
        if (isPawnOnFinish(pawnId1, tileId)){
            // moving is not possible when the pawn is directly between two other pawns
            if (isPawnTightlyClosedIn(pawnId1, tileId)){
                return;
            }

            TileId nextTileId = moveAndCheckEveryTile(pawnId1, tileId, nrSteps);
            response.setPawnId1(pawnId1);
            storeToResponse(response, nextTileId.getPlayerId(), nextTileId.getTileNr() );
            movePawn(new Pawn(pawnId1,nextTileId));
            return;
        }

        if((next > 15 && isPawnOnLastSection(playerId, playerIdOfTile))){
            TileId nextTileId = moveAndCheckEveryTile(pawnId1, tileId, nrSteps);
            response.setPawnId1(pawnId1);
            storeToResponse(response, nextTileId.getPlayerId(), nextTileId.getTileNr() );
            movePawn(new Pawn(pawnId1,nextTileId));
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
}
