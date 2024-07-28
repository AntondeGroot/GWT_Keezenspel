package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gwtks.GameStateUtil.*;
import static gwtks.MoveResult.CANNOT_MAKE_MOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckingOnBoardTest {
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
        CardsDeck.reset();
    }

    @Test
    void checkOnBoard_DoesNotRemoveCardFromHand() {
        // GIVEN
        Card card = givePlayerAce(0);
        int nrCards = CardsDeck.getCardsForPlayer(0).size();
        Pawn pawn1 = new Pawn(new PawnId(0,1), new TileId(0, -1));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(nrCards, CardsDeck.getCardsForPlayer(0).size());
    }

    @Test
    void checkOnBoard_WrongCard_DoesNotShow() {
        // GIVEN
        Card card = givePlayerCard(0,3);
        int nrCards = CardsDeck.getCardsForPlayer(0).size();
        Pawn pawn1 = new Pawn(new PawnId(0,1), new TileId(0, -1));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        GameState.processOnBoard(moveMessage, moveResponse);

        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    }
}
