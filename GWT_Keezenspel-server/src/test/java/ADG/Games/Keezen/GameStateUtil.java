package ADG.Games.Keezen;

import ADG.Games.Keezen.*;

import java.util.ArrayList;

public class GameStateUtil {

    public static Pawn createPawnAndPlaceOnBoard(String playerId, TileId currentTileId){
        // for creating pawns for different players
        PawnId pawnId1 = new PawnId(playerId,0);
        Pawn pawn1 = new Pawn(pawnId1, new TileId(playerId, -1));
        pawn1.setCurrentTileId(currentTileId);
        GameState.movePawn(pawn1);
        return pawn1;
    }

    public static Pawn createPawnAndPlaceOnBoard(PawnId pawnId, TileId currentTileId){
        // for creating multiple pawns for the same player
        Pawn pawn1 = new Pawn(pawnId, new TileId(pawnId.getPlayerId(), -pawnId.getPawnNr()-1));
        pawn1.setCurrentTileId(currentTileId);
        GameState.movePawn(pawn1);
        return pawn1;
    }
    public static Card givePlayerCard(String playerId, int nrSteps){
        Card card = new Card(0, nrSteps-1);
        ADG.Games.Keezen.CardsDeck.setPlayerCard(playerId, card);
        return card;
    }
    public static Card givePlayerAce(String playerId){
        Card ace = new Card(0, 0);
        ADG.Games.Keezen.CardsDeck.giveCardToPlayerForTesting(playerId, ace);
        return ace;
    }
    public static Card givePlayerKing(String playerId){
        Card king = new Card(0, 11);
        ADG.Games.Keezen.CardsDeck.giveCardToPlayerForTesting(playerId, king);
        return king;
    }
    public static Card givePlayerJack(String playerId){
        Card jack = new Card(0, 10);
        ADG.Games.Keezen.CardsDeck.giveCardToPlayerForTesting(playerId, jack);
        return jack;
    }
    public static void createMoveMessage(MoveMessage moveMessage, Pawn pawn, Card card){
        moveMessage.setPlayerId(pawn.getPlayerId());
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MoveType.MOVE);
        moveMessage.setStepsPawn1(card.getCardValue()+1);
        moveMessage.setCard(card);
        moveMessage.setMessageType(MessageType.MAKE_MOVE);
    }
    public static void createSwitchMessage(MoveMessage moveMessage, Pawn pawn1, Pawn pawn2, Card card){
        moveMessage.setPlayerId(pawn1.getPlayerId());
        moveMessage.setPawnId1(pawn1.getPawnId());
        moveMessage.setPawnId2(pawn2.getPawnId());
        moveMessage.setCard(card);
        moveMessage.setMoveType(MoveType.SWITCH);
        moveMessage.setMessageType(MessageType.MAKE_MOVE);
    }
    public static void sendForfeitMessage(String playerId){
        MoveMessage moveMessage = new MoveMessage();
        moveMessage.setPlayerId(playerId);
        moveMessage.setMoveType(MoveType.FORFEIT);
        moveMessage.setMessageType(MessageType.MAKE_MOVE);
        GameState.processOnForfeit(moveMessage);
    }
    public static void sendValidMoveMessage(String playerId){
        // get the first pawn of the player
        Pawn pawn = new Pawn(new PawnId(playerId,0),new TileId(playerId,-1));

        // place the pawn on the board (playerId,1)
        createPawnAndPlaceOnBoard(playerId, new TileId(playerId,1));

        // fake a valid card
        Card card = new Card(0,5);

        // replace a card from the players hand with this card
        ADG.Games.Keezen.CardsDeck.giveCardToPlayerForTesting(playerId, card);

        // send move message
        MoveMessage moveMessage = new MoveMessage();
        moveMessage.setPlayerId(playerId);
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MoveType.MOVE);
        moveMessage.setStepsPawn1(card.getCardValue()+1);
        moveMessage.setCard(card);
        moveMessage.setMessageType(MessageType.MAKE_MOVE);

        // process
        MoveResponse moveResponse = new MoveResponse();
        GameState.processOnMove(moveMessage, moveResponse);
    }
    public static void playRemainingCards(String playerId){
        int nrCards = CardsDeck.getCardsForPlayer(playerId).size();
        for (int i = 0; i < nrCards; i++) {
            sendValidMoveMessage(playerId);
        }
    }
    public static ArrayList<Integer> intsToList(int[] integers){
        ArrayList<Integer> result = new ArrayList<>();
        for (int integer : integers) {
            result.add(integer);
        }
        return result;
    }
    public static void place4PawnsOnFinish(String playerId){
        createPawnAndPlaceOnBoard(new PawnId(playerId,0) , new TileId(playerId,16));
        createPawnAndPlaceOnBoard(new PawnId(playerId,1) , new TileId(playerId,17));
        createPawnAndPlaceOnBoard(new PawnId(playerId,2) , new TileId(playerId,18));
        createPawnAndPlaceOnBoard(new PawnId(playerId,3) , new TileId(playerId,19));
    }

    public static void createGame_With_NPlayers(String nrPlayers){
        for (int i = 0; i < nrPlayers; i++) {
            GameState.addPlayer(new Player("player"+i,"123-i"));
        }
        GameState.start();
    }
}
