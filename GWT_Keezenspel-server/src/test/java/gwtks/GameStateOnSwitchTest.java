package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameStateOnSwitchTest {

    @BeforeEach
    void setUp() {
        GameState gameState = new GameState(8);
    }

    @AfterEach
    void tearDown() {
        GameState.tearDown();
    }
    // onboarding
    @Test
    void putPlayerOnBoardWhenPossible() {
        // GIVEN
        PawnId pawnId = new PawnId(0,0);
        Pawn pawn = new Pawn(pawnId, new TileId(0,-2));

        MoveMessage moveMessage = new MoveMessage();
        MoveResponse moveResponse = new MoveResponse();

        // WHEN
        moveMessage.setPawnId1(pawnId);
        moveMessage.setMoveType(MoveType.ONBOARD);
        moveMessage.setTileId(new TileId(0,-2));
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN
        // response message is correct
        assertEquals(pawnId, moveResponse.getPawnId1());                          // moves the correct pawn
        assertEquals(0,moveResponse.getMovePawn1().get(0).getTileNr());  // moves the pawn to the correct tileNr
        // GameState is correct
        assertEquals(0,GameState.getPawn(pawn).getCurrentTileId().getTileNr());
    }

    @Test
    void putPlayerNotOnBoardWhenSamePlayerIsAlreadyThere() {
        // GIVEN
        PawnId pawnId1 = new PawnId(0,0);
        PawnId pawnId2 = new PawnId(0,1);
        Pawn pawn1 = new Pawn(pawnId1, new TileId(0,0));
        Pawn pawn2 = new Pawn(pawnId2, new TileId(0,0));

        MoveMessage moveMessage = new MoveMessage();
        MoveMessage moveMessage2 = new MoveMessage();
        MoveResponse moveResponse = new MoveResponse();
        MoveResponse moveResponse2 = new MoveResponse();

        // WHEN
        moveMessage.setPawnId1(pawnId1);
        moveMessage.setMoveType(MoveType.ONBOARD);
        moveMessage.setTileId(new TileId(0,-1));
        GameState.processOnBoard(moveMessage, moveResponse);
        // when
        moveMessage2.setPawnId1(pawnId2);
        moveMessage2.setMoveType(MoveType.ONBOARD);
        moveMessage2.setTileId(new TileId(0,-2));
        GameState.processOnBoard(moveMessage2, moveResponse2);

        // THEN
        // response message is correct
        assertEquals(null, moveResponse2.getPawnId1());                          // moves the correct pawn
        assertEquals(null, moveResponse2.getMovePawn1());  // moves the pawn to the correct tileNr
        // GameState is correct
        assertEquals(-2,GameState.getPawn(pawn2).getCurrentTileId().getTileNr());
    }

    @Test
    void putPlayerNotOnBoardWhenNotOnNestTiles(){
        assertEquals(0,1);
    }
    // moving
    @Test
    void passingStartTile_NotPossibleWhenTilesPlayerIsThere_Forward(){
        assertEquals(0,1);
    }

    @Test
    void passingStartTile_NotPossibleWhenTilesPlayerIsThere_Backward(){
        assertEquals(0,1);
    }
    @Test
    void passingStartTile_PossibleWhenOtherPlayerIsThere_Forward(){
        assertEquals(0,1);
    }
    @Test
    void passingStartTile_PossibleWhenOtherPlayerIsThere_Backward(){
        assertEquals(0,1);
    }
    // moving to finish tiles
    @Test
    void moveOnFinishTileWhenAlmostThere(){
        assertEquals(0,1);
    }
    @Test
    void MoveOnFinishTileWhenAlreadyOnAFinishTile(){
        assertEquals(0,1);
    }
    @Test
    void MoveBackwardsOnFinishTileWhenAlreadyOnFinishTile(){
        assertEquals(0,1);
    }
    @Test
    void MoveOutOfFinishTileWhenAlreadyOnFinishTile(){
        assertEquals(0,1);
    }
    @Test
    void MoveOnFinishTilesBackAndForthWhenAlreadyOnFinishTileAndBlockedByOwnPawns(){
        assertEquals(0,1);
    }

    // switching pawns
    @Test
    void someTest(){
        assertEquals(0,1);
    }
}