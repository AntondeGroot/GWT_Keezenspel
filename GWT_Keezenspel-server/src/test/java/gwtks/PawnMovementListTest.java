package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static gwtks.GameStateUtil.createPawnAndPlaceOnBoard;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PawnMovementListTest {
    MoveMessage moveMessage = new MoveMessage();
    MoveResponse moveResponse = new MoveResponse();

    @BeforeEach
    void setUp() {
        GameState gameState = new GameState(8);
        moveMessage = new MoveMessage();
        moveResponse = new MoveResponse();
    }

    @AfterEach
    void tearDown() {
        GameState.tearDown();
        moveMessage = null;
        moveResponse = null;
    }

    @Test
    void pawnMovesAroundCorner7() {
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,5));

        // WHEN
        createMoveMessage(pawn1,4);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(0,5));
        expectedMovement.add(new TileId(0,7));
        expectedMovement.add(new TileId(0,9));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }

    @Test
    void pawnMovesAroundCorner1() {
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,0));

        // WHEN
        createMoveMessage(pawn1,3);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(0,0));
        expectedMovement.add(new TileId(0,1));
        expectedMovement.add(new TileId(0,3));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }

    @Test
    void pawnMovesAroundCorner13And0() {
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(2,new TileId(0,12));

        // WHEN
        createMoveMessage(pawn1,8);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(0,12));
        expectedMovement.add(new TileId(0,13));
        expectedMovement.add(new TileId(0,15));
        expectedMovement.add(new TileId(1,1));
        expectedMovement.add(new TileId(1,4));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMoveFrom9To12() {
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,9));

        // WHEN
        createMoveMessage(pawn1,3);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(0,9));
        expectedMovement.add(new TileId(0,12));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMoveFrom11To1() {
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(2,new TileId(0,11));

        // WHEN
        createMoveMessage(pawn1,6);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(0,11));
        expectedMovement.add(new TileId(0,13));
        expectedMovement.add(new TileId(0,15));
        expectedMovement.add(new TileId(1,1));


        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMoveHitsAllCorners() {
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(2,new TileId(0,0));

        // WHEN
        createMoveMessage(pawn1,19);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(0,0));
        expectedMovement.add(new TileId(0,1));
        expectedMovement.add(new TileId(0,7));
        expectedMovement.add(new TileId(0,13));
        expectedMovement.add(new TileId(0,15));
        expectedMovement.add(new TileId(1,1));
        expectedMovement.add(new TileId(1,3));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMovesIntoFinishAndOvershoots() {
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(1,new TileId(0,15));

        // WHEN
        createMoveMessage(pawn1,5);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(0,15));
        expectedMovement.add(new TileId(1,19));
        expectedMovement.add(new TileId(1,18));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMovesBackwardsOverCorners15_13_7_1() {
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(1,new TileId(1,16));

        // WHEN
        createMoveMessage(pawn1,-12);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(1,16));
        expectedMovement.add(new TileId(0,15));
        expectedMovement.add(new TileId(0,13));
        expectedMovement.add(new TileId(0,7));
        expectedMovement.add(new TileId(0,4));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMovesBackwardsOverCorners13_7() {
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,14));

        // WHEN
        createMoveMessage(pawn1,-8);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(0,14));
        expectedMovement.add(new TileId(0,13));
        expectedMovement.add(new TileId(0,7));
        expectedMovement.add(new TileId(0,6));


        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMovesBackwardsOverCorners7() {
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,8));

        // WHEN
        createMoveMessage(pawn1,-4);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(0,8));
        expectedMovement.add(new TileId(0,7));
        expectedMovement.add(new TileId(0,4));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }

    private void createMoveMessage(Pawn pawn, int steps){
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MoveType.MOVE);
        moveMessage.setTileId(pawn.getCurrentTileId());
        moveMessage.setStepsPawn1(steps);
    }
}
