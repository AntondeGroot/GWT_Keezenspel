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
        createGame_With_NPlayers(3);
        moveMessage = new MoveMessage();
        moveResponse = new MoveResponse();
    }

    @AfterEach
    void tearDown() {
        GameState.tearDown();
        CardsDeck.reset();
        moveMessage = null;
        moveResponse = null;
    }

    // onboarding
    @Test
    void putPlayerOnBoard_WhenPossible() {
        // GIVEN
        Card ace = givePlayerAce(0);
        Pawn pawn1 = GameStateUtil.placePawnOnNest("0",new TileId("0",-2));

        // WHEN
        createOnBoardMessage("0", pawn1, ace);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN
        // response message is correct
        assertEquals(new TileId("0",0), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tileNr
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // GameState is correct
        assertEquals(new TileId("0",0) ,GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void putPlayerOnBoard_ThenNextPlayerPlays() {
        // GIVEN
        Card ace = givePlayerAce(0);
        Pawn pawn1 = GameStateUtil.placePawnOnNest("0",new TileId("0",-2));

        // WHEN
        createOnBoardMessage("0", pawn1, ace);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN
        assertEquals("1" ,GameState.getPlayerIdTurn());
    }

    @Test
    void putPlayerOnBoardAsLastCard_ThenNextPlayerPlays() {
        // GIVEN
        GameState.setPlayerIdTurn("0");
        for (int i = 0; i < 4; i++) {
            sendValidMoveMessage("0");
            sendValidMoveMessage("1");
            sendValidMoveMessage("2");
        }

        Card ace = givePlayerAce(0);
        Pawn pawn1 = GameStateUtil.placePawnOnNest("0",new TileId("0",-2));

        // WHEN
        createOnBoardMessage("0", pawn1, ace);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN
        assertEquals("1" ,GameState.getPlayerIdTurn());
    }

    @Test
    void putPlayerNotOnBoard_WhenSamePlayerIsAlreadyThere() {
        // GIVEN
        Card ace = givePlayerAce(0);
        Pawn pawn1 = placePawnOnBoard(new PawnId("0",0), new TileId("0",-1));
        Pawn pawn2 = placePawnOnBoard(new PawnId("0",1), new TileId("0",0));

        // WHEN
        createOnBoardMessage("0", pawn1, ace);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN response msg is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN GameState is correct
        assertEquals(new TileId("0",-1) , GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void putPlayerNotOnBoard_WhenNotOnNestTiles(){
        // GIVEN
        Card ace = givePlayerAce(0);
        Pawn pawn1 = placePawnOnNest("0",new TileId("0",3));

        // WHEN
        createOnBoardMessage("0", pawn1, ace);
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
        Pawn pawn1 = placePawnOnNest("0",new TileId("0",17));

        // WHEN
        createOnBoardMessage("0", pawn1, king);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN response msg is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getPawnId1());
        assertNull(moveResponse.getMovePawn1());
        // THEN GameState is correct
        assertEquals(17,GameState.getPawn(pawn1).getCurrentTileId().getTileNr());
    }

    public void createOnBoardMessage(String playerId, Pawn pawn, Card card){
        moveMessage.setPlayerId(playerId);
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MoveType.ONBOARD);
        moveMessage.setCard(card);
        moveMessage.setMessageType(MessageType.MAKE_MOVE);
    }
}