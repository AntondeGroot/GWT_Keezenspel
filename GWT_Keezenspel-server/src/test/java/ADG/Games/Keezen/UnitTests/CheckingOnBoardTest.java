package ADG.Games.Keezen.UnitTests;

import com.adg.openapi.model.Card;
import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.Move.MessageType;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static ADG.Games.Keezen.Move.MoveResult.CANNOT_MAKE_MOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckingOnBoardTest {
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
    void checkOnBoard_DoesNotRemoveCardFromHand() {
        // GIVEN
        Card card = givePlayerAce(cardsDeck, 0);
        int nrCards = cardsDeck.getCardsForPlayer("0").size();
        Pawn pawn1 = new Pawn(new PawnId("0",1), new PositionKey("0", -1));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        gameState.processOnBoard(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(nrCards, cardsDeck.getCardsForPlayer("0").size());
    }

    @Test
    void checkOnBoard_WrongCard_DoesNotShow() {
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, 3);
        Pawn pawn1 = new Pawn(new PawnId("0",1), new PositionKey("0", -1));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        gameState.processOnBoard(moveMessage, moveResponse);

        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
    }
}
