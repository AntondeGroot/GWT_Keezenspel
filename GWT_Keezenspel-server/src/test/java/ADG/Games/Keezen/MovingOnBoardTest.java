package ADG.Games.Keezen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.GameStateUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MovingOnBoardTest {
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

    // onboarding
    @Test
    void putPlayerOnBoard_WhenPossible() {
        // GIVEN
        Card ace = givePlayerAce(0);
        Pawn pawn1 = GameStateUtil.createPawnAndPlaceOnBoard(0,new TileId(0,-2));

        // WHEN
        moveMessage.setPawnId1(pawn1.getPawnId());
        moveMessage.setMoveType(MoveType.ONBOARD);
        moveMessage.setCard(ace);
        moveMessage.setMessageType(MessageType.MAKE_MOVE);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN
        // response message is correct
        assertEquals(new TileId(0,0), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tileNr
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // GameState is correct
        assertEquals(new TileId(0,0) ,GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void putPlayerNotOnBoard_WhenSamePlayerIsAlreadyThere() {
        // GIVEN
        Card ace = givePlayerAce(0);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(0,-1));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(0,0));

        // WHEN
        moveMessage.setPawnId1(pawn1.getPawnId());
        moveMessage.setMoveType(MoveType.ONBOARD);
        moveMessage.setCard(ace);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN response msg is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN GameState is correct
        assertEquals(new TileId(0,-1) , GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void putPlayerNotOnBoard_WhenNotOnNestTiles(){
        // GIVEN
        Card ace = givePlayerAce(0);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,3));

        // WHEN
        moveMessage.setPawnId1(pawn1.getPawnId());
        moveMessage.setMoveType(MoveType.ONBOARD);
        moveMessage.setCard(ace);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN response msg is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getPawnId1());
        assertNull(moveResponse.getMovePawn1());
        // THEN GameState is correct
        assertEquals(3,GameState.getPawn(pawn1).getCurrentTileId().getTileNr());
    }

    @Test
    void putPlayerNotOnBoard_WhenOnFinishTiles(){
        // GIVEN
        Card king = givePlayerKing(0);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,17));

        // WHEN
        moveMessage.setPawnId1(pawn1.getPawnId());
        moveMessage.setMoveType(MoveType.ONBOARD);
        moveMessage.setCard(king);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN response msg is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getPawnId1());
        assertNull(moveResponse.getMovePawn1());
        // THEN GameState is correct
        assertEquals(17,GameState.getPawn(pawn1).getCurrentTileId().getTileNr());
    }
}