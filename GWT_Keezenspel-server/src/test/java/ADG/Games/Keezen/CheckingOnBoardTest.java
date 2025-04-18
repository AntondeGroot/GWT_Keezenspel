package ADG.Games.Keezen;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Move.MessageType;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.GameStateUtil.*;
import static ADG.Games.Keezen.Move.MoveResult.CANNOT_MAKE_MOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckingOnBoardTest {
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
        ADG.Games.Keezen.CardsDeck.reset();
    }

    @Test
    void checkOnBoard_DoesNotRemoveCardFromHand() {
        // GIVEN
        Card card = givePlayerAce(0);
        int nrCards = ADG.Games.Keezen.CardsDeck.getCardsForPlayer("0").size();
        Pawn pawn1 = new Pawn(new PawnId("0",1), new TileId("0", -1));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        GameState.processOnBoard(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(nrCards, ADG.Games.Keezen.CardsDeck.getCardsForPlayer("0").size());
    }

    @Test
    void checkOnBoard_WrongCard_DoesNotShow() {
        // GIVEN
        Card card = givePlayerCard(0,3);
        Pawn pawn1 = new Pawn(new PawnId("0",1), new TileId("0", -1));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        GameState.processOnBoard(moveMessage, moveResponse);

        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    }
}
