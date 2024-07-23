package gwtks;

public class GameStateUtil {

    public static Pawn createPawnAndPlaceOnBoard(int playerId, TileId currentTileId){
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
    public static Card givePlayerCard(int playerId, int nrSteps){
        Card card = new Card(playerId, nrSteps-1);
        CardsDeck.setPlayerCard(playerId, card);
        return card;
    }
    public static Card givePlayerAce(int playerId){
        Card ace = new Card(playerId, 0);
        CardsDeck.setPlayerCard(playerId, ace);
        return ace;
    }
    public static Card givePlayerKing(int playerId){
        Card king = new Card(playerId, 11);
        CardsDeck.setPlayerCard(playerId, king);
        return king;
    }
    public static Card givePlayerJack(int playerId){
        Card jack = new Card(playerId, 10);
        CardsDeck.setPlayerCard(playerId, jack);
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
        moveMessage.setMoveType(MoveType.MOVE);
    }
    public static void sendForfeitMessage(int playerId){
        MoveMessage moveMessage = new MoveMessage();
        moveMessage.setPlayerId(playerId);
        moveMessage.setMoveType(MoveType.FORFEIT);
        moveMessage.setMessageType(MessageType.MAKE_MOVE);
        GameState.processOnForfeit(moveMessage);
    }
    public static void sendValidMoveMessage(int playerId){
        // get the first pawn of the player
        Pawn pawn = new Pawn(new PawnId(playerId,0),new TileId(playerId,-1));

        // place the pawn on the board (playerId,1)
        createPawnAndPlaceOnBoard(playerId, new TileId(playerId,1));

        // fake a valid card
        Card card = new Card(0,5);

        // replace a card from the players hand with this card
        CardsDeck.giveCardToPlayerForTesting(playerId, card);

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
}
