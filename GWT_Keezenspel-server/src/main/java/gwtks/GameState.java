package gwtks;

import java.util.List;

public class GameState {

    private static List<Pawn> pawns;
    private static int userId;
    private int numberOfPlayers = 4;

    public GameState() {
            player:
            for (int i = 0; i < numberOfPlayers; i++) {
                pawns:
                for (int j = 0; j < 4; j++) {
                    pawns.add(new Pawn(i));
                }

        }
    }

    public Integer nextTurn(){
        userId = (userId + 1) % numberOfPlayers;
        return userId;
    }

    public int getUserId(){
        return userId;
    }

    public List<Pawn> getPawns() {
        return pawns;
    }
}
