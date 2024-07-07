package gwtks;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    private static List<Pawn> pawns;
    private static int playerIdTurn;
    private int numberOfPlayers = 8;
    private static final List<TileId> tiles = new ArrayList<>();

    public GameState() {
        if(pawns == null || pawns.isEmpty()){
            pawns = new ArrayList<Pawn>();
            for (int playerId = 0; playerId < numberOfPlayers; playerId++) {
                for (int pawnNr = 0; pawnNr < 4; pawnNr++) {
                    pawns.add(new Pawn(playerId,pawnNr));
                }
            }
        }
    }

    public Integer nextTurn(){
        playerIdTurn = (playerIdTurn + 1) % numberOfPlayers;
        return playerIdTurn;
    }

    public int getPlayerIdTurn(){
        return playerIdTurn;
    }

    public List<Pawn> getPawns() {
        return pawns;
    }
}
