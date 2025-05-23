package ADG.Games.Keezen.UnitTests;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Move.MoveResult;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.TileId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MovingOnBoardSamePlayerBlockingTest {
    MoveMessage moveMessage = new MoveMessage();
    MoveResponse moveResponse = new MoveResponse();

    private GameState gameState;
    private CardsDeckInterface cardsDeck;

    @BeforeEach
    void setUp() {
        GameSession engine = new GameSession();
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
    void twoPawnsOnePlayer_CannotEndOnTheSameTile(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, 1);
        Pawn pawn1 = GameStateUtil.placePawnOnBoard(gameState, new PawnId("0",0), new TileId("0",9));
        Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0",1), new TileId("0",10));

        // WHEN
        createMoveMessage(moveMessage, pawn1,card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("0",9), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId("0",10), gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void twoPawnsOnePlayer_CannotEndOnTheSameTile_DoesNotPlayCard(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, -1);
        Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0",0), new TileId("0",16));
        Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0",1), new TileId("7",15));
        int nrCardsBeforePlaying = cardsDeck.getCardsForPlayer("0").size();

        // WHEN
        createMoveMessage(moveMessage, pawn1,card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertEquals(nrCardsBeforePlaying, cardsDeck.getCardsForPlayer("0").size());
    }
    @Test
    void PawnOn15_PawnCannotBePlacedThere_Forwards(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, 1);
        Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0",0), new TileId("0",14));
        Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0",1), new TileId("0",15));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("0",14), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId("0",15), gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOn15_PawnCannotBePlacedThere_Backwards(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, -1);
        Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0",0), new TileId("1",0));
        Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0",1), new TileId("0",15));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId("1",0), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId("0",15), gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOnOtherStart_PawnCannotBePlacedThere_Forwards(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, 1);
        Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0",0), new TileId("0",15));
        Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0",1), new TileId("1",0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId("0",15), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId("1",0), gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOnOtherStart_PawnCannotBePlacedThere_Backwards(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, -1);
        Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0",0), new TileId("1",1));
        Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0",1), new TileId("1",0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",1), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId("1",0), gameState.getPawn(pawn2).getCurrentTileId());
    }
}