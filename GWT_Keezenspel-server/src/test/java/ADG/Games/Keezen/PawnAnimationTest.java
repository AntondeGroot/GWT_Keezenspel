package ADG.Games.Keezen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;

import static ADG.Games.Keezen.GameStateUtil.*;
import static ADG.Games.Keezen.MoveResult.CAN_MAKE_MOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PawnAnimationTest {
    MoveMessage moveMessage = new MoveMessage();
    MoveResponse moveResponse = new MoveResponse();

    @BeforeEach
    void setUp() {
        createGame_With_NPlayers(8);
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
        Card card = givePlayerCard(0,4);
        Pawn pawn1 = GameStateUtil.createPawnAndPlaceOnBoard("0",new TileId("0",5));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",5));
        expectedMovement.add(new TileId("0",7));
        expectedMovement.add(new TileId("0",9));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }

    @Test
    void pawnMovesAroundCorner1() {
        // GIVEN
        Card card = givePlayerCard(0,3);
        Pawn pawn1 = createPawnAndPlaceOnBoard("0",new TileId("0",0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",0));
        expectedMovement.add(new TileId("0",1));
        expectedMovement.add(new TileId("0",3));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }

    @Test
    void pawnMovesAroundCorner13And0() {
        // GIVEN
        Card card = givePlayerCard(2,8);
        Pawn pawn1 = createPawnAndPlaceOnBoard("2",new TileId("0",12));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",12));
        expectedMovement.add(new TileId("0",13));
        expectedMovement.add(new TileId("0",15));
        expectedMovement.add(new TileId("1",1));
        expectedMovement.add(new TileId("1",4));

        // response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMoveFrom9To12() {
        // GIVEN
        Card card = givePlayerCard(0,3);
        Pawn pawn1 = createPawnAndPlaceOnBoard("0",new TileId("0",9));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",9));
        expectedMovement.add(new TileId("0",12));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMoveFrom11To1() {
        // GIVEN
        Card card = givePlayerCard(2,6);
        Pawn pawn1 = createPawnAndPlaceOnBoard("2",new TileId("0",11));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",11));
        expectedMovement.add(new TileId("0",13));
        expectedMovement.add(new TileId("0",15));
        expectedMovement.add(new TileId("1",1));


        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMoveHitsAllCorners() {
        // GIVEN
        Card card = givePlayerCard(2,19);
        Pawn pawn1 = createPawnAndPlaceOnBoard("2",new TileId("0",0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",0));
        expectedMovement.add(new TileId("0",1));
        expectedMovement.add(new TileId("0",7));
        expectedMovement.add(new TileId("0",13));
        expectedMovement.add(new TileId("0",15));
        expectedMovement.add(new TileId("1",1));
        expectedMovement.add(new TileId("1",3));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMovesIntoFinishAndOvershoots() {
        // GIVEN
        Card card = givePlayerCard(1,5);
        Pawn pawn1 = createPawnAndPlaceOnBoard("1",new TileId("0",15));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",15));
        expectedMovement.add(new TileId("1",19));
        expectedMovement.add(new TileId("1",18));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMovesBackwardsOverCorners15_13_7_1() {
        // GIVEN
        Card card = givePlayerCard(1,-12);
        Pawn pawn1 = createPawnAndPlaceOnBoard("1",new TileId("1",16));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("1",16));
        expectedMovement.add(new TileId("0",15));
        expectedMovement.add(new TileId("0",13));
        expectedMovement.add(new TileId("0",7));
        expectedMovement.add(new TileId("0",4));

        // response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMovesBackwardsOverCorners13_7() {
        // GIVEN
        Card card = givePlayerCard(0,-8);
        Pawn pawn1 = createPawnAndPlaceOnBoard("0",new TileId("0",14));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",14));
        expectedMovement.add(new TileId("0",13));
        expectedMovement.add(new TileId("0",7));
        expectedMovement.add(new TileId("0",6));


        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMovesBackwardsOverCorners7() {
        // GIVEN
        Card card = givePlayerCard(0,-4);
        Pawn pawn1 = createPawnAndPlaceOnBoard("0",new TileId("0",8));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",8));
        expectedMovement.add(new TileId("0",7));
        expectedMovement.add(new TileId("0",4));

        // response message is correct
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }
    @Test
    void pawnMovesForwards_13_7_1_ToNewSection() {
        // setup
        GameState.tearDown();
        createGame_With_NPlayers(3);

        // GIVEN
        Card card = givePlayerCard(1,8);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId("1",1) ,new TileId("2",12));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId("1",2) ,new TileId("0",0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("2",12));
        expectedMovement.add(new TileId("2",13));
        expectedMovement.add(new TileId("2",15));
        expectedMovement.add(new TileId("0",1));
        expectedMovement.add(new TileId("0",4));

        // response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }

    @Test
    void pawnMoves_FinishIsFull_AnimateToRightTile_BugFix() {
        /**
         * When the finish tile (1,16) is taken for player1
         * And player 1 tries to move onto it:
         * The animation animates a move to tile (1,15)
         * instead of tile (0,15)
         * it also had the points (0,15)  (1,15) (0,15) so
         * it would unnecessarily be doubled.
         */

        // setup
        GameState.tearDown();
        createGame_With_NPlayers(3);

        // GIVEN
        Card card = givePlayerCard(1,8);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId("1",1) ,new TileId("1",16));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId("1",2) ,new TileId("0",12));

        // WHEN
        createMoveMessage(moveMessage, pawn2, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",12));
        expectedMovement.add(new TileId("0",13));
        expectedMovement.add(new TileId("0",15));
        expectedMovement.add(new TileId("0",13));
        expectedMovement.add(new TileId("0",10));

        // response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }


    @Test
    void pawnMoves_PingPong_3StepsOnFinish_test() {
        // setup
        GameState.tearDown();
        createGame_With_NPlayers(3);

        // GIVEN
        Card card = givePlayerCard(1,3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId("1",1) ,new TileId("1",17));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId("1",2) ,new TileId("1",19));
        Pawn pawn3 = createPawnAndPlaceOnBoard(new PawnId("1",3) ,new TileId("1",16));


        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));

        // response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }

    @Test
    void pawnMoves_PingPong_9StepsOnFinish_test() {
        // setup
        GameState.tearDown();
        createGame_With_NPlayers(3);

        // GIVEN
        Card card = givePlayerCard(1,9);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId("1",1) ,new TileId("1",17));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId("1",2) ,new TileId("1",19));
        Pawn pawn3 = createPawnAndPlaceOnBoard(new PawnId("1",3) ,new TileId("1",16));


        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));

        // response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(expectedMovement, moveResponse.getMovePawn1());
    }

    @Test
    void pawnMoves_PingPongMethod_9StepsOnFinish_test() {
        // setup
        GameState.tearDown();
        createGame_With_NPlayers(3);

        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId("1",1) ,new TileId("1",17));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId("1",2) ,new TileId("1",19));
        Pawn pawn3 = createPawnAndPlaceOnBoard(new PawnId("1",3) ,new TileId("1",16));


        // WHEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));

        ArrayList<TileId> actualMovement = GameState.pingpongMove(pawn1.getPawnId(), pawn1.getCurrentTileId(),9);
        // THEN
        System.out.println(actualMovement);
        assertEquals(expectedMovement, actualMovement);
    }

    @Test
    void pawnMoves_PingPongMethod_6StepsOnFinish_3places_test() {
        // setup
        int nrSteps = 6;
        GameState.tearDown();
        createGame_With_NPlayers(3);

        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId("1",1) ,new TileId("1",17));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId("1",3) ,new TileId("1",16));


        // WHEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",19));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",19));

        ArrayList<TileId> actualMovement = GameState.pingpongMove(pawn1.getPawnId(), pawn1.getCurrentTileId(),nrSteps);
        // THEN
        System.out.println(actualMovement);
        assertEquals(expectedMovement, actualMovement);
    }

    @Test
    void pawnMoves_PingPongMethod_5StepsOnFinish_3places_test() {
        // setup
        int nrSteps = 5;
        GameState.tearDown();
        createGame_With_NPlayers(3);

        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId("1",1) ,new TileId("1",17));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId("1",3) ,new TileId("1",16));


        // WHEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",19));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));

        ArrayList<TileId> actualMovement = GameState.pingpongMove(pawn1.getPawnId(), pawn1.getCurrentTileId(),nrSteps);
        // THEN
        System.out.println(actualMovement);
        assertEquals(expectedMovement, actualMovement);
    }

    @Test
    void pawnMoves_PingPongMethod_Negative4StepsOnFinish_3places_test() {
        // setup
        int nrSteps = -4;
        GameState.tearDown();
        createGame_With_NPlayers(3);

        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId("1",1) ,new TileId("1",17));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId("1",3) ,new TileId("1",16));


        // WHEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",19));
        expectedMovement.add(new TileId("1",17));

        ArrayList<TileId> actualMovement = GameState.pingpongMove(pawn1.getPawnId(), pawn1.getCurrentTileId(),nrSteps);
        // THEN
        System.out.println(actualMovement);
        assertEquals(expectedMovement, actualMovement);
    }

    @Test
    void pawnMoves_PingPongMethod_Negative4StepsOnFinish_test() {
        // setup
        GameState.tearDown();
        createGame_With_NPlayers(3);
        int nrSteps = -4;

        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId("1",1) ,new TileId("1",17));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId("1",2) ,new TileId("1",19));
        Pawn pawn3 = createPawnAndPlaceOnBoard(new PawnId("1",3) ,new TileId("1",16));


        // WHEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));
        expectedMovement.add(new TileId("1",18));
        expectedMovement.add(new TileId("1",17));

        ArrayList<TileId> actualMovement = GameState.pingpongMove(pawn1.getPawnId(), pawn1.getCurrentTileId(),nrSteps);
        // THEN
        System.out.println(actualMovement);
        assertEquals(expectedMovement, actualMovement);
    }
}
