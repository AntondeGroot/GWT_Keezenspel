package ADG.Games.Keezen.UnitTests;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Move.MoveType;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.Player.Player;
import ADG.Games.Keezen.TileId;
import java.util.ArrayList;
import java.util.Collections;

import static ADG.Games.Keezen.Move.MessageType.MAKE_MOVE;
import static ADG.Games.Keezen.Move.MoveType.*;

public class GameStateUtil {

    public static Pawn placePawnOnNest(GameState gameState, String playerId, TileId currentTileId){
        // for creating pawns for different players
        PawnId pawnId1 = new PawnId(playerId,0);
        Pawn pawn1 = new Pawn(pawnId1, new TileId(playerId, -1));
        pawn1.setCurrentTileId(currentTileId);
        gameState.movePawn(pawn1);
        return pawn1;
    }

    public static Pawn placePawnOnBoard(GameState gameState, PawnId pawnId, TileId currentTileId){
        // for creating multiple pawns for the same player
        Pawn pawn1 = new Pawn(pawnId, new TileId(pawnId.getPlayerId(), -pawnId.getPawnNr()-1));
        pawn1.setCurrentTileId(currentTileId);
        gameState.movePawn(pawn1);
        return pawn1;
    }
    public static Pawn placePawnOnBoard(GameState gameState, Pawn pawn){
        // for creating multiple pawns for the same player
        pawn.setCurrentTileId(pawn.getCurrentTileId());
        gameState.movePawn(pawn);
        return pawn;
    }
    public static Card givePlayerCard(CardsDeckInterface cardsDeck, int playerInt, int nrSteps){
        String playerId = String.valueOf(playerInt);
        Card card = new Card(0, nrSteps);
        cardsDeck.setPlayerCard(playerId, card);
        return card;
    }
    public static Card givePlayerAce(CardsDeckInterface cardsDeck, int playerInt){
        String playerId = String.valueOf(playerInt);
        Card ace = new Card(0, 1);
        cardsDeck.giveCardToPlayerForTesting(playerId, ace);
        return ace;
    }
    public static Card givePlayerKing(CardsDeckInterface cardsDeck, int playerInt){
        String playerId = String.valueOf(playerInt);
        Card king = new Card(0, 12);
        cardsDeck.giveCardToPlayerForTesting(playerId, king);
        return king;
    }
    public static Card givePlayerSeven(CardsDeckInterface cardsDeck, int playerInt){
        String playerId = String.valueOf(playerInt);
        Card sevenCard = new Card(0, 7);
        cardsDeck.giveCardToPlayerForTesting(playerId, sevenCard);
        return sevenCard;
    }
    public static Card givePlayerJack(CardsDeckInterface cardsDeck, int playerInt) {
        String playerId = String.valueOf(playerInt);
        Card jack = new Card(0, 11);
        cardsDeck.giveCardToPlayerForTesting(playerId, jack);
        return jack;
    }
    public static void createSplitMessage(MoveMessage moveMessage, Pawn pawn, int nrSteps1, Pawn pawn2, Integer nrSteps2, Card card){
        moveMessage.setPlayerId(pawn.getPlayerId());
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setStepsPawn1(nrSteps1);
        moveMessage.setStepsPawn2(nrSteps2);
        moveMessage.setPawnId2(pawn2.getPawnId());
        moveMessage.setMoveType(SPLIT);
        moveMessage.setCard(card);
        moveMessage.setMessageType(MAKE_MOVE);
    }
    public static void createMoveMessage(MoveMessage moveMessage, Pawn pawn, Card card){
        moveMessage.setPlayerId(pawn.getPlayerId());
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MOVE);
        moveMessage.setStepsPawn1(card.getCardValue());
        moveMessage.setCard(card);
        moveMessage.setMessageType(MAKE_MOVE);
    }
    public static void createSwitchMessage(MoveMessage moveMessage, Pawn pawn1, Pawn pawn2, Card card){
        moveMessage.setPlayerId(pawn1.getPlayerId());
        moveMessage.setPawnId1(pawn1.getPawnId());
        moveMessage.setPawnId2(pawn2.getPawnId());
        moveMessage.setCard(card);
        moveMessage.setMoveType(SWITCH);
        moveMessage.setMessageType(MAKE_MOVE);
    }
    public static void sendForfeitMessage(GameState gameState, String playerId){
        MoveMessage moveMessage = new MoveMessage();
        moveMessage.setPlayerId(playerId);
        moveMessage.setMoveType(MoveType.FORFEIT);
        moveMessage.setMessageType(MAKE_MOVE);
        gameState.processOnForfeit(moveMessage);
    }
    public static void sendValidMoveMessage(GameState gameState, CardsDeckInterface cardsDeck,
        String playerId){
        // get the first pawn of the player
        Pawn pawn = new Pawn(new PawnId(playerId,0),new TileId(playerId,-1));

        // place the pawn on the board (playerId,1)
        placePawnOnNest(gameState, playerId, new TileId(playerId,1));

        // fake a valid card
        Card card = new Card(0,5);

        // replace a card from the players hand with this card
        cardsDeck.giveCardToPlayerForTesting(playerId, card);

        // send move message
        MoveMessage moveMessage = new MoveMessage();
        moveMessage.setPlayerId(playerId);
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MOVE);
        moveMessage.setStepsPawn1(card.getCardValue());
        moveMessage.setCard(card);
        moveMessage.setMessageType(MAKE_MOVE);

        // process
        MoveResponse moveResponse = new MoveResponse();
        gameState.processOnMove(moveMessage, moveResponse);
    }
    public static void playRemainingCards(GameState gameState, CardsDeckInterface cardsDeck, String playerId){
        int nrCards = cardsDeck.getCardsForPlayer(playerId).size();
        for (int i = 0; i < nrCards; i++) {
            sendValidMoveMessage(gameState, cardsDeck, playerId);
        }
    }

    public static ArrayList<String> stringsToList(String[] strings){
        ArrayList<String> result = new ArrayList<>();
        Collections.addAll(result, strings);
        return result;
    }

    public static void place4PawnsOnFinish(GameState gameState, String playerId){
        placePawnOnBoard(gameState, new PawnId(playerId,0), new TileId(playerId,16));
        placePawnOnBoard(gameState, new PawnId(playerId,1), new TileId(playerId,17));
        placePawnOnBoard(gameState, new PawnId(playerId,2), new TileId(playerId,18));
        placePawnOnBoard(gameState, new PawnId(playerId,3), new TileId(playerId,19));
    }

    public static void createGame_With_NPlayers(GameState gameState, int nrPlayers){
        gameState.stop();
        for (int i = 0; i < nrPlayers; i++) {
            gameState.addPlayer(new Player(String.valueOf(i),String.valueOf(i)));
        }
        gameState.start();
    }
}
