package ADG.Games.Keezen.UnitTests;


import com.adg.openapi.model.Card;
import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.Move.MessageType;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Move.MoveResult;
import ADG.Games.Keezen.Move.MoveType;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static ADG.Games.Keezen.Move.MoveResult.CAN_MAKE_MOVE;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TurnBasedTest {

  MoveMessage moveMessage = new MoveMessage();
  MoveResponse moveResponse = new MoveResponse();
  ArrayList<String> activePlayers = new ArrayList<>();
  HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();

  private GameSession engine;
  private GameState gameState;
  private CardsDeckInterface cardsDeck;

  @BeforeEach
  void setUp() {
    engine = new GameSession();
    gameState = engine.getGameState();
    cardsDeck = engine.getCardsDeck();

    createGame_With_NPlayers(gameState, 3);
    moveMessage = new MoveMessage();
    moveResponse = new MoveResponse();
    activePlayers.add("0");
    activePlayers.add("1");
    activePlayers.add("2");
  }

  @AfterEach
  void tearDown() {
    gameState.tearDown();
    moveMessage = null;
    moveResponse = null;
    activePlayers.clear();
    cardsDeck.reset();
    nrCardsPerPlayer = new HashMap<>();
    gameState.stop();
  }

  @Test
  void player1Forfeits_OtherPlayersStillActive() {
    // WHEN
    sendForfeitMessage(gameState, "0");

    // THEN
    activePlayers.remove("0");
    Assertions.assertEquals(activePlayers, gameState.getActivePlayers());
    Assertions.assertEquals(2, gameState.getActivePlayers().size());
  }

  @Test
  void player1Forfeits_Player1NoCards_OthersStillHaveCards() {
    // WHEN
    sendForfeitMessage(gameState, "0");

    // THEN
    HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();
    nrCardsPerPlayer.put("0", 0);
    nrCardsPerPlayer.put("1", 5);
    nrCardsPerPlayer.put("2", 5);
    assertEquals(nrCardsPerPlayer, cardsDeck.getNrOfCardsPerPlayer());
  }

  @Test
  void player1Forfeits_Player2IsNowPlaying() {
    // WHEN
    sendForfeitMessage(gameState, "0");

    // THEN
    activePlayers.remove("0");
    assertEquals("1", gameState.getPlayerIdTurn());
    assertEquals(activePlayers, gameState.getActivePlayers());
  }

  @Test
  void player1PlaysCard_4_5_5_CardsInTheGame() {
    // WHEN
    sendValidMoveMessage(gameState, cardsDeck, "0");

    // THEN
    HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();
    nrCardsPerPlayer.put("0", 4);
    nrCardsPerPlayer.put("1", 5);
    nrCardsPerPlayer.put("2", 5);
    assertEquals(nrCardsPerPlayer, cardsDeck.getNrOfCardsPerPlayer());
  }

  @Test
  void player1Forfeits_Player1GetsSkippedInNextTurn() {
    // GIVEN
    sendForfeitMessage(gameState, "0");
    sendValidMoveMessage(gameState, cardsDeck, "1");

    // WHEN
    sendValidMoveMessage(gameState, cardsDeck, "2");

    // THEN
    assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void allPlayersForfeit_AllPlayersActiveAgain() {
    // WHEN
    sendForfeitMessage(gameState, "0");
    sendForfeitMessage(gameState, "1");
    sendForfeitMessage(gameState, "2");

    // THEN
    Assert.assertEquals(activePlayers, gameState.getActivePlayers());
    Assert.assertEquals(3, gameState.getActivePlayers().size());
  }

  @Test
  void allPlayersForfeit_AllPlayersHave4Cards() {
    // WHEN
    sendForfeitMessage(gameState, "0");
    sendForfeitMessage(gameState, "1");
    sendForfeitMessage(gameState, "2");

    // THEN
    HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();
    nrCardsPerPlayer.put("0", 4);
    nrCardsPerPlayer.put("1", 4);
    nrCardsPerPlayer.put("2", 4);
    assertEquals(nrCardsPerPlayer, cardsDeck.getNrOfCardsPerPlayer());
  }

  @Test
  void allPlayersForfeit2Rounds_AllPlayersHave4Cards() {
    // WHEN
    for (int i = 0; i < 2; i++) {
      sendForfeitMessage(gameState, "0");
      sendForfeitMessage(gameState, "1");
      sendForfeitMessage(gameState, "2");
    }

    // THEN
    HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();
    nrCardsPerPlayer.put("0", 4);
    nrCardsPerPlayer.put("1", 4);
    nrCardsPerPlayer.put("2", 4);
    assertEquals(nrCardsPerPlayer, cardsDeck.getNrOfCardsPerPlayer());
  }

  @Test
  void allPlayersForfeit3Rounds_AllPlayersHave5Cards() {
    // WHEN
    for (int i = 0; i < 3; i++) {
      sendForfeitMessage(gameState, "0");
      sendForfeitMessage(gameState, "1");
      sendForfeitMessage(gameState, "2");
    }

    // THEN
    HashMap<String, Integer> nrCardsPerPlayer = new HashMap<>();
    nrCardsPerPlayer.put("0", 5);
    nrCardsPerPlayer.put("1", 5);
    nrCardsPerPlayer.put("2", 5);
    assertEquals(nrCardsPerPlayer, cardsDeck.getNrOfCardsPerPlayer());
  }

  @Test
  void player2Forfeits_WhenPlayer1HasPlayed_Player3WillPlayNext() {
    // GIVEN
    sendValidMoveMessage(gameState, cardsDeck, "0");
    sendForfeitMessage(gameState, "1");
    sendValidMoveMessage(gameState, cardsDeck, "2");

    // WHEN
    sendValidMoveMessage(gameState, cardsDeck, "0");

    // THEN
    assertTrue(cardsDeck.getCardsForPlayer("1").isEmpty());
    assertEquals("2", gameState.getPlayerIdTurn());
  }

  @Test
  void allPlayersExcept1Forfeit_RemainingPlayerKeepsPlayingUntilLastCard() {
    // GIVEN
    sendForfeitMessage(gameState, "0");
    sendForfeitMessage(gameState, "1");

    for (int i = 0; i < 5; i++) {
      // WHEN
      sendValidMoveMessage(gameState, cardsDeck, "2");

      // THEN
      if (i < 4) {
        Assert.assertEquals("playerId turn NOT last round", "2", gameState.getPlayerIdTurn());
        Assert.assertEquals("cards remaining for player 2", 4 - i,
            cardsDeck.getCardsForPlayer("2").size());
      }
    }
  }

  @Test
  void playerCanOnlyMoveHisOwnPawn() {

//        // WHEN
//        sendValidMoveMessage(gameState , cardsDeck , "0");
//
//        // send a valid move for the wrong player
//        Pawn pawn = new Pawn(new PawnId("0",0),new PositionKey("0",6));
//
//        // fake a valid card
//        Card card = new Card().suit(0).value(5);
//
//        // replace a card from the players hand with this card
//        cardsDeck.giveCardToPlayerForTesting("1", card);
//
//        // send move message
//        MoveMessage moveMessage = new MoveMessage();
//        moveMessage.setPlayerId("1");
//        moveMessage.setPawnId1(pawn.getPawnId());
//        moveMessage.setMoveType(MoveType.MOVE);
//        moveMessage.setStepsPawn1(card.getValue());
//        moveMessage.setCard(card);
//        moveMessage.setMessageType(MessageType.MAKE_MOVE);
//
//        // process
//        MoveResponse moveResponse = new MoveResponse();
//        gameState.processOnMove(moveMessage, moveResponse);
//
//        // THEN
//        assertNull(moveResponse.getMovePawn1());
//        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
    Assertions.fail();
  }

  @Test
  void playersPlayAllTheirCards_ExceptLastPlayer_OnlyLastPlayerIsActive() {
    // GIVEN
    for (int i = 0; i < 4; i++) {
      sendValidMoveMessage(gameState, cardsDeck, "0");
      sendValidMoveMessage(gameState, cardsDeck, "1");
      sendValidMoveMessage(gameState, cardsDeck, "2");
    }

    // WHEN
    sendValidMoveMessage(gameState, cardsDeck, "0");
    sendValidMoveMessage(gameState, cardsDeck, "1");

    // THEN
    assertEquals(stringsToList(new String[]{"2"}), gameState.getActivePlayers());
  }

  @Test
  void players0and2Finished_OnlyPlayer1PlayingAndForfeiting_DoesNotSwitchToWinner() {
    // GIVEN
    ArrayList<String> winners = new ArrayList<>();
    place4PawnsOnFinish(gameState, "0");
    gameState.checkForWinners(winners);
    place4PawnsOnFinish(gameState, "2");
    gameState.checkForWinners(winners);

    // WHEN
    sendForfeitMessage(gameState, "0");
    playRemainingCards(gameState, cardsDeck, "1");
    sendForfeitMessage(gameState, "2");

    // THEN
    assertEquals("1", gameState.getPlayerIdTurn());
    sendForfeitMessage(gameState, "1");
    assertEquals("1", gameState.getPlayerIdTurn());
    sendForfeitMessage(gameState, "1");
    assertEquals("1", gameState.getPlayerIdTurn());
    sendForfeitMessage(gameState, "1");
    assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void players1Starts_WhenPlayer1PlaysLastCard_Player2ShouldPlay_Bugfix() {
    // GIVEN
    gameState.setPlayerIdTurn("1");
    for (int i = 0; i < 4; i++) {
      sendValidMoveMessage(gameState, cardsDeck, "1");
      sendValidMoveMessage(gameState, cardsDeck, "2");
      sendValidMoveMessage(gameState, cardsDeck, "0");
    }

    // WHEN
    sendValidMoveMessage(gameState, cardsDeck, "1");

    // THEN
    Assertions.assertEquals("2", gameState.getPlayerIdTurn());
  }

  @Test
  void players2Starts_WhenPlayer2PlaysLastCard_Player0ShouldPlay_Bugfix() {
    // GIVEN
    gameState.setPlayerIdTurn("2");
    for (int i = 0; i < 4; i++) {
      sendValidMoveMessage(gameState, cardsDeck, "2");
      sendValidMoveMessage(gameState, cardsDeck, "0");
      sendValidMoveMessage(gameState, cardsDeck, "1");
    }

    // WHEN
    sendValidMoveMessage(gameState, cardsDeck, "2");

    // THEN
    Assertions.assertEquals("0", gameState.getPlayerIdTurn());
  }

  @Test
  void players2Starts_WhenPlayer2PlaysLastCard_AndPlayer0Forfeits_Player1ShouldPlay() {
    // GIVEN
    gameState.setPlayerIdTurn("2");
    for (int i = 0; i < 4; i++) {
      sendValidMoveMessage(gameState, cardsDeck, "2");
      sendValidMoveMessage(gameState, cardsDeck, "0");
      sendValidMoveMessage(gameState, cardsDeck, "1");
    }

    // WHEN
    sendValidMoveMessage(gameState, cardsDeck, "2");
    sendForfeitMessage(gameState, "0");

    // THEN
    Assertions.assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void players0Starts_WhenPlayer0and2Forfeit_Player1PlaysLastCard_Player1ShouldPlay_bugfix() {
    // GIVEN
    gameState.setPlayerIdTurn("0");
    sendForfeitMessage(gameState, "0");
    sendValidMoveMessage(gameState, cardsDeck, "1");
    sendForfeitMessage(gameState, "2");

    for (int i = 0; i < 3; i++) {
      sendValidMoveMessage(gameState, cardsDeck, "1");
    }

    // WHEN send last card
    sendValidMoveMessage(gameState, cardsDeck, "1");

    // THEN
    Assertions.assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void oneRound_player0LastPlayer_nextPlayer1() {
    // GIVEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN
    sendValidMoveMessage(gameState, cardsDeck, "0");
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    playRemainingCards(gameState, cardsDeck, "0");

    // THEN
    assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void oneRound_player1LastPlayer_nextPlayer1() {
    // GIVEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN
    sendValidMoveMessage(gameState, cardsDeck, "0");
    sendValidMoveMessage(gameState, cardsDeck, "1");
    gameState.forfeitPlayer("2");
    gameState.forfeitPlayer("0");
    playRemainingCards(gameState, cardsDeck, "1");

    // THEN
    assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void oneRound_player2LastPlayer_nextPlayer1_Forfeit() {
    // GIVEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN
    gameState.forfeitPlayer("0");
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");

    // THEN
    assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void oneRound_player2LastPlayer_nextPlayer1_byPlaying() {
    // GIVEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN
    gameState.forfeitPlayer("0");
    gameState.forfeitPlayer("1");
    playRemainingCards(gameState, cardsDeck, "2");
    // THEN
    assertEquals("1", gameState.getPlayerIdTurn());
  }

  @Test
  void twoRounds_player0LastPlayer_nextPlayer2() {
    // GIVEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN round 1
    gameState.forfeitPlayer("0");
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    // WHEN round 2
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    playRemainingCards(gameState, cardsDeck, "0");

    // THEN
    assertEquals("2", gameState.getPlayerIdTurn());
  }

  @Test
  void twoRounds_player1LastPlayer_nextPlayer2() {
    // GIVEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN round 1
    gameState.forfeitPlayer("0");
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    // WHEN round 2
    sendValidMoveMessage(gameState, cardsDeck, "1");
    gameState.forfeitPlayer("2");
    gameState.forfeitPlayer("0");
    playRemainingCards(gameState, cardsDeck, "1");

    // THEN
    assertEquals("2", gameState.getPlayerIdTurn());
  }

  @Test
  void twoRounds_player2LastPlayer_nextPlayer2() {
    // GIVEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN round 1
    gameState.forfeitPlayer("0");
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    // WHEN round 2
    gameState.forfeitPlayer("1");
    sendValidMoveMessage(gameState, cardsDeck, "2");
    gameState.forfeitPlayer("0");
    playRemainingCards(gameState, cardsDeck, "2");
    // THEN
    assertEquals("2", gameState.getPlayerIdTurn());
  }

  @Test
  void threeRounds_player0LastPlayer_nextPlayer0() {
    // GIVEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN round 1
    gameState.forfeitPlayer("0");
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    // WHEN round 2
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    gameState.forfeitPlayer("0");
    // WHEN round 3
    gameState.forfeitPlayer("2");
    sendValidMoveMessage(gameState, cardsDeck, "0");
    gameState.forfeitPlayer("1");
    playRemainingCards(gameState, cardsDeck, "0");

    // THEN
    assertEquals("0", gameState.getPlayerIdTurn());
  }

  @Test
  void threeRounds_player1LastPlayer_nextPlayer0() {
    // GIVEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN round 1
    gameState.forfeitPlayer("0");
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    // WHEN round 2
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    gameState.forfeitPlayer("0");
    // WHEN round 3
    gameState.forfeitPlayer("2");
    gameState.forfeitPlayer("0");
    playRemainingCards(gameState, cardsDeck, "1");

    // THEN
    assertEquals("0", gameState.getPlayerIdTurn());
  }

  @Test
  void threeRounds_player2LastPlayer_nextPlayer0() {
    /// GIVEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN round 1
    gameState.forfeitPlayer("0");
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    // WHEN round 2
    gameState.forfeitPlayer("1");
    gameState.forfeitPlayer("2");
    gameState.forfeitPlayer("0");
    // WHEN round 3
    sendValidMoveMessage(gameState, cardsDeck, "2");
    gameState.forfeitPlayer("0");
    gameState.forfeitPlayer("1");
    playRemainingCards(gameState, cardsDeck, "2");

    // THEN
    assertEquals("0", gameState.getPlayerIdTurn());
  }

  @Test
  void test_whenSplitIsPlayed_nextPlayerIs() {
//        /// GIVEN
//        createGame_With_NPlayers(gameState , 3);
//        // send a valid move for the wrong player
//        Pawn pawn1 = new Pawn(new PawnId("0",1),new PositionKey("0",6));
//        Pawn pawn2 = new Pawn(new PawnId("0",2),new PositionKey("0",0));
//        placePawnOnBoard(gameState, pawn1);
//        placePawnOnBoard(gameState, pawn2);
//        // fake a valid card
//        Card card = new Card().suit(0).value(7);
//        // replace a card from the players hand with this card
//        cardsDeck.giveCardToPlayerForTesting("0", card);
//
//        createSplitMessage(moveMessage, pawn1, 3, pawn2, 4, card);
//        // process
//        MoveResponse moveResponse = new MoveResponse();
//        gameState.processOnSplit(moveMessage, moveResponse);
//
//        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
//        assertEquals("1", gameState.getPlayerIdTurn());
    Assertions.fail();
  }

  @Test
  void ThreeRoundsArePlayed_NumberOfUniqueCards_39_bugfix() {
    // I discovered that not enough kings and aces were given

    // GIVEN, WHEN
    ArrayList<Card> cards = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      gameState.setPlayerIdTurn("0");
      cards.addAll(cardsDeck.getCardsForPlayer("0"));
      sendForfeitMessage(gameState, "0");
      cards.addAll(cardsDeck.getCardsForPlayer("1"));
      sendForfeitMessage(gameState, "1");
      cards.addAll(cardsDeck.getCardsForPlayer("2"));
      sendForfeitMessage(gameState, "2");
    }

    // THEN
    HashSet<Card> cardSet = new HashSet<>(cards);
    assertEquals(13 * 3, cards.size());
    assertEquals(13 * 3, cardSet.size());
  }
}
