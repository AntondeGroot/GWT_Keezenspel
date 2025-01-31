package ADG.Games.Keezen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;

import static ADG.Games.Keezen.GameStateUtil.*;
import static ADG.Games.Keezen.MessageType.CHECK_MOVE;
import static ADG.Games.Keezen.MoveResult.CANNOT_MAKE_MOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MovingWithCard7Test {
    private MoveMessage moveMessage = new MoveMessage();
    private MoveResponse moveResponse = new MoveResponse();
    private final Card sevenCard = new Card(0, 7);

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
        CardsDeck.reset();
    }

    @Test
    void test_moveTwoPawns_correct(){
        // when the pawn is on position 15 and takes two steps it will end up at position 13

        // GIVEN
        givePlayerSeven(0);
        Pawn pawn1 = placePawnOnBoard(new PawnId("0", 1),new TileId("0",0));
        Pawn pawn2 = placePawnOnBoard(new PawnId("0", 2),new TileId("0",10));

        // WHEN
        createSplitMessage(moveMessage, pawn1, 4, pawn2,3, sevenCard);
        GameState.processOnSplit(moveMessage, moveResponse);

        // THEN response is correct
        assertEquals(new TileId("0",4), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId("0",4), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile

        assertEquals(new TileId("0",13), GameState.getPawn(pawn2).getCurrentTileId());
        assertEquals(new TileId("0",13), moveResponse.getMovePawn2().getLast());  // moves the pawn to the correct tile
    }

    @Test
    void test_moveTwoPawns_TestSplitAll7TilesForBothPawns(){
        // GIVEN
        givePlayerSeven(0);
        Pawn pawn1 = placePawnOnBoard(new PawnId("0", 1),new TileId("0",0));
        Pawn pawn2 = placePawnOnBoard(new PawnId("0", 2),new TileId("0",5));

        // WHEN no decision was made how to split the 7 among the two pawns
        createSplitMessage(moveMessage, pawn1, 7, pawn2,7, sevenCard);
        moveMessage.setMessageType(CHECK_MOVE);
        GameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(createExpectedMovement(0,7), moveResponse.getMovePawn1());
        assertEquals(createExpectedMovement(5,7), moveResponse.getMovePawn2());
    }

    @Test
    void test_moveTwoPawns_TestSplitForBothPawns_OneGoesToNextSegment(){
        // GIVEN
        givePlayerSeven(0);
        Pawn pawn1 = placePawnOnBoard(new PawnId("0", 1),new TileId("0",0));
        Pawn pawn2 = placePawnOnBoard(new PawnId("0", 2),new TileId("0",14));

        // WHEN no decision was made how to split the 7 among the two pawns
        createSplitMessage(moveMessage, pawn1, 3, pawn2,4, sevenCard); // the second is null because no choice was made
        moveMessage.setMessageType(CHECK_MOVE);
        GameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(createExpectedMovement(0,3), moveResponse.getMovePawn1());
        assertEquals(createExpectedMovement(14,4), moveResponse.getMovePawn2());
    }
    @Test
    void test_moveTwoPawns_Pawn1WasOnFinish_OldPositionDoesNotBlockTestMovePawn2(){
        // bugfix, test move did not show it correctly, however when playing the card it did place the
        // pawns correctly. Assume the first pawn selected moves first

        // GIVEN
        givePlayerSeven(0);
        Pawn pawn1 = placePawnOnBoard(new PawnId("0", 1),new TileId("0",16));
        Pawn pawn2 = placePawnOnBoard(new PawnId("0", 2),new TileId("7",14));

        // WHEN
        createSplitMessage(moveMessage, pawn1, 3, pawn2,4, sevenCard);
        moveMessage.setMessageType(CHECK_MOVE);
        GameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(new TileId("0",19), moveResponse.getMovePawn1().getLast());
        assertEquals(new TileId("0",18), moveResponse.getMovePawn2().getLast());
    }

    @Test
    void CheckMove_moveTwoPawns_EndOnSameTile_CannotMove(){
        // GIVEN
        givePlayerSeven(0);
        Pawn pawn1 = placePawnOnBoard(new PawnId("0", 1),new TileId("0",5));
        Pawn pawn2 = placePawnOnBoard(new PawnId("0", 2),new TileId("0",2));

        // WHEN
        createSplitMessage(moveMessage, pawn1, 2, pawn2,5, sevenCard);
        moveMessage.setMessageType(CHECK_MOVE);
        GameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    }

    @Test
    void MakeMove_moveTwoPawns_EndOnSameTile_CannotMove(){
        // GIVEN
        givePlayerSeven(0);
        Pawn pawn1 = placePawnOnBoard(new PawnId("0", 1),new TileId("0",5));
        Pawn pawn2 = placePawnOnBoard(new PawnId("0", 2),new TileId("0",2));

        // WHEN
        createSplitMessage(moveMessage, pawn1, 2, pawn2,5, sevenCard);
        GameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId("0",5), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId("0",2), GameState.getPawn(pawn2).getCurrentTileId());
    }


    private ArrayList<TileId> createExpectedMovement(int startTileNr, int nrSteps){
        ArrayList<TileId> expectedMoves = new ArrayList<>();
        Integer playerId = 0;
        int tileNr = startTileNr;
        for (int i = 0; i <= nrSteps; i++) {
            expectedMoves.add(new TileId(playerId.toString(),tileNr));
            tileNr++;
            if(tileNr > 15){
                tileNr = tileNr % 16;
                playerId++;
            }
        }
        return expectedMoves;
    }

}
