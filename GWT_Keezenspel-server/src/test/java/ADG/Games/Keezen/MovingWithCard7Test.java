package ADG.Games.Keezen;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import java.util.LinkedList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.GameStateUtil.*;
import static ADG.Games.Keezen.Move.MessageType.CHECK_MOVE;
import static ADG.Games.Keezen.Move.MoveResult.CANNOT_MAKE_MOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MovingWithCard7Test {
    private MoveMessage moveMessage = new MoveMessage();
    private MoveResponse moveResponse = new MoveResponse();
    private final Card sevenCard = new Card(0, 7);

    private GameSession engine;
    private GameState gameState;
    private CardsDeckInterface cardsDeck;

    @BeforeEach
    void setUp() {
        engine = new GameSession();
        gameState = engine.getGameState();
        cardsDeck = engine.getCardsDeck();

        createGame_With_NPlayers(gameState, 8);
        moveMessage = new MoveMessage();
        moveResponse = new MoveResponse();
    }

    @AfterEach
    void tearDown() {
        gameState.tearDown();
        moveMessage = null;
        moveResponse = null;
        cardsDeck.reset();
    }

    @Test
    void test_moveTwoPawns_correct(){
        // when the pawn is on position 15 and takes two steps it will end up at position 13

        // GIVEN
        givePlayerSeven(cardsDeck , 0);
        Pawn pawn1 = placePawnOnBoard(gameState , new PawnId("0", 1), new TileId("0",0));
        Pawn pawn2 = placePawnOnBoard(gameState , new PawnId("0", 2), new TileId("0",10));

        // WHEN
        createSplitMessage(moveMessage, pawn1, 4, pawn2,3, sevenCard);
        gameState.processOnSplit(moveMessage, moveResponse);

        // THEN response is correct
        assertEquals(new TileId("0",4), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId("0",4), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile

        assertEquals(new TileId("0",13), gameState.getPawn(pawn2).getCurrentTileId());
        assertEquals(new TileId("0",13), moveResponse.getMovePawn2().getLast());  // moves the pawn to the correct tile
    }

    @Test
    void test_moveTwoPawns_TestSplitAll7TilesForBothPawns(){
        // GIVEN
        givePlayerSeven(cardsDeck , 0);
        Pawn pawn1 = placePawnOnBoard(gameState , new PawnId("0", 1), new TileId("0",0));
        Pawn pawn2 = placePawnOnBoard(gameState , new PawnId("0", 2), new TileId("0",5));

        // WHEN no decision was made how to split the 7 among the two pawns
        createSplitMessage(moveMessage, pawn1, 7, pawn2,7, sevenCard);
        moveMessage.setMessageType(CHECK_MOVE);
        gameState.processOnSplit(moveMessage, moveResponse);

        LinkedList<TileId> expectedTilesPawn1 = new LinkedList<>();
        expectedTilesPawn1.add(new TileId("0",0));
        expectedTilesPawn1.add(new TileId("0",1));
        expectedTilesPawn1.add(new TileId("0",7));

        LinkedList<TileId> expectedTilesPawn2 = new LinkedList<>();
        expectedTilesPawn2.add(new TileId("0",5));
        expectedTilesPawn2.add(new TileId("0",7));
        expectedTilesPawn2.add(new TileId("0",12));

        // THEN
        assertEquals(expectedTilesPawn1, moveResponse.getMovePawn1());
        assertEquals(expectedTilesPawn2, moveResponse.getMovePawn2());
    }

    @Test
    void test_moveTwoPawns_TestSplitForBothPawns_OneGoesToNextSegment(){
        // GIVEN
        givePlayerSeven(cardsDeck , 0);
        Pawn pawn1 = placePawnOnBoard(gameState , new PawnId("0", 1), new TileId("0",0));
        Pawn pawn2 = placePawnOnBoard(gameState , new PawnId("0", 2), new TileId("0",14));

        // WHEN no decision was made how to split the 7 among the two pawns
        createSplitMessage(moveMessage, pawn1, 3, pawn2,4, sevenCard); // the second is null because no choice was made
        moveMessage.setMessageType(CHECK_MOVE);
        gameState.processOnSplit(moveMessage, moveResponse);

        LinkedList<TileId> expectedTilesPawn1 = new LinkedList<>();
        expectedTilesPawn1.add(new TileId("0",0));
        expectedTilesPawn1.add(new TileId("0",1));
        expectedTilesPawn1.add(new TileId("0",3));

        LinkedList<TileId> expectedTilesPawn2 = new LinkedList<>();
        expectedTilesPawn2.add(new TileId("0",14));
        expectedTilesPawn2.add(new TileId("0",15));
        expectedTilesPawn2.add(new TileId("1",1));
        expectedTilesPawn2.add(new TileId("1",2));

        // THEN
        assertEquals(expectedTilesPawn1, moveResponse.getMovePawn1());
        assertEquals(expectedTilesPawn2, moveResponse.getMovePawn2());
    }
    @Test
    void test_moveTwoPawns_Pawn1WasOnFinish_OldPositionDoesNotBlockTestMovePawn2(){
        // bugfix, test move did not show it correctly, however when playing the card it did place the
        // pawns correctly. Assume the first pawn selected moves first

        // GIVEN
        givePlayerSeven(cardsDeck , 0);
        Pawn pawn1 = placePawnOnBoard(gameState , new PawnId("0", 1), new TileId("0",16));
        Pawn pawn2 = placePawnOnBoard(gameState , new PawnId("0", 2), new TileId("7",14));

        // WHEN
        createSplitMessage(moveMessage, pawn1, 3, pawn2,4, sevenCard);
        moveMessage.setMessageType(CHECK_MOVE);
        gameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(new TileId("0",19), moveResponse.getMovePawn1().getLast());
        assertEquals(new TileId("0",18), moveResponse.getMovePawn2().getLast());
    }

    @Test
    void CheckMove_moveTwoPawns_EndOnSameTile_CannotMove(){
        // GIVEN
        givePlayerSeven(cardsDeck , 0);
        Pawn pawn1 = placePawnOnBoard(gameState , new PawnId("0", 1), new TileId("0",5));
        Pawn pawn2 = placePawnOnBoard(gameState , new PawnId("0", 2), new TileId("0",2));

        // WHEN
        createSplitMessage(moveMessage, pawn1, 2, pawn2,5, sevenCard);
        moveMessage.setMessageType(CHECK_MOVE);
        gameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    }

    @Test
    void MakeMove_moveTwoPawns_EndOnSameTile_CannotMove(){
        // GIVEN
        givePlayerSeven(cardsDeck , 0);
        Pawn pawn1 = placePawnOnBoard(gameState , new PawnId("0", 1), new TileId("0",5));
        Pawn pawn2 = placePawnOnBoard(gameState , new PawnId("0", 2), new TileId("0",2));

        // WHEN
        createSplitMessage(moveMessage, pawn1, 2, pawn2,5, sevenCard);
        gameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId("0",5), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId("0",2), gameState.getPawn(pawn2).getCurrentTileId());
    }
}
