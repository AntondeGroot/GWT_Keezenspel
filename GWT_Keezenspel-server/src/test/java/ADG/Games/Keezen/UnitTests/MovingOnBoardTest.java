package ADG.Games.Keezen.UnitTests;

import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.Card;
import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.MoveResult;
import com.adg.openapi.model.MoveType;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import com.adg.openapi.model.TempMessageType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MovingOnBoardTest {

  MoveRequest moveMessage = new MoveRequest();
  MoveResponse moveResponse = new MoveResponse();

  private GameState gameState;
  private CardsDeckInterface cardsDeck;

  @BeforeEach
  void setUp() {
    GameSession engine = new GameSession();
    gameState = engine.getGameState();
    cardsDeck = engine.getCardsDeck();

    createGame_With_NPlayers(gameState, 3);
    moveMessage = new MoveRequest();
    moveResponse = new MoveResponse();
  }

  @AfterEach
  void tearDown() {
    gameState.tearDown();
    cardsDeck.reset();
    moveMessage = null;
    moveResponse = null;
  }

  // onboarding
  @Test
  void putPlayerOnBoard_WhenPossible() {
    // GIVEN
    Card ace = givePlayerAce(cardsDeck, 0);
    Pawn pawn1 = GameStateUtil.placePawnOnNest(gameState, "0", new PositionKey("0", -2));

    // WHEN
    createOnBoardMessage("0", pawn1, ace);
    gameState.processOnBoard(moveMessage, moveResponse);

    // THEN
    // response message is correct
    assertEquals(new PositionKey("0", 0),
        moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tileNr
    assertEquals(pawn1, moveResponse.getPawn1());                          // moves the correct pawn
    // GameState is correct
    assertEquals(new PositionKey("0", 0), gameState.getPawn(pawn1).getCurrentTileId());
  }

  @Test
  void putPlayerOnBoard_ThenNextPlayerPlays() {
    // GIVEN
    Card ace = givePlayerAce(cardsDeck, 0);
    Pawn pawn1 = GameStateUtil.placePawnOnNest(gameState, "0", new PositionKey("0", -2));

    // WHEN
    createOnBoardMessage("0", pawn1, ace);
    gameState.processOnBoard(moveMessage, moveResponse);

    // THEN
    assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void putPlayerOnBoardAsLastCard_ThenNextPlayerPlays() {
    // GIVEN
    gameState.setPlayerIdTurn("0");
    for (int i = 0; i < 4; i++) {
      sendValidMoveRequest(gameState, cardsDeck, "0");
      sendValidMoveRequest(gameState, cardsDeck, "1");
      sendValidMoveRequest(gameState, cardsDeck, "2");
    }

    Card ace = givePlayerAce(cardsDeck, 0);
    Pawn pawn1 = GameStateUtil.placePawnOnNest(gameState, "0", new PositionKey("0", -2));

    // WHEN
    createOnBoardMessage("0", pawn1, ace);
    gameState.processOnBoard(moveMessage, moveResponse);

    // THEN
    assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void putPlayerNotOnBoard_WhenSamePlayerIsAlreadyThere() {
    // GIVEN
    Card ace = givePlayerAce(cardsDeck, 0);
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0", 0), new PositionKey("0", -1));
    Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0", 1), new PositionKey("0", 0));

    // WHEN
    createOnBoardMessage("0", pawn1, ace);
    gameState.processOnBoard(moveMessage, moveResponse);

    // THEN response msg is correct
    assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertTrue(moveResponse.getMovePawn1().isEmpty());
    assertNull(moveResponse.getPawn1());
    // THEN GameState is correct
    assertEquals(new PositionKey("0", -1), gameState.getPawn(pawn1).getCurrentTileId());
  }

  @Test
  void putPlayerNotOnBoard_WhenNotOnNestTiles() {
    // GIVEN
    Card ace = givePlayerAce(cardsDeck, 0);
    Pawn pawn1 = placePawnOnNest(gameState, "0", new PositionKey("0", 3));

    // WHEN
    createOnBoardMessage("0", pawn1, ace);
    gameState.processOnBoard(moveMessage, moveResponse);

    // THEN response msg is correct
    assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertTrue(moveResponse.getMovePawn1().isEmpty());
    // THEN GameState is correct
    assertEquals(3, gameState.getPawn(pawn1).getCurrentTileId().getTileNr());
  }

  @Test
  void putPlayerNotOnBoard_WhenOnFinishTiles() {
    // GIVEN
    Card king = givePlayerKing(cardsDeck, 0);
    Pawn pawn1 = placePawnOnNest(gameState, "0", new PositionKey("0", 17));

    // WHEN
    createOnBoardMessage("0", pawn1, king);
    gameState.processOnBoard(moveMessage, moveResponse);

    // THEN response msg is correct
    assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
    assertNull(moveResponse.getPawn1());
    assertTrue(moveResponse.getMovePawn1().isEmpty());
    // THEN GameState is correct
    assertEquals(17, gameState.getPawn(pawn1).getCurrentTileId().getTileNr());
  }

  public void createOnBoardMessage(String playerId, Pawn pawn, Card card) {
    moveMessage.setPlayerId(playerId);
    moveMessage.setPawn1(pawn);
    moveMessage.setMoveType(MoveType.ON_BOARD);
    moveMessage.setCard(card);
    moveMessage.setTempMessageType(TempMessageType.MAKE_MOVE);
  }
}