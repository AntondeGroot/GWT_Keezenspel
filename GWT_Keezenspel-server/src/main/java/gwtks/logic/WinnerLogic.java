package gwtks.logic;

import gwtks.GameState;
import gwtks.Pawn;

import java.util.ArrayList;

public class WinnerLogic {

    public static void checkForWinners(ArrayList<Integer> winners){
        ArrayList<Pawn> pawns = GameState.getPawns();
        int nrPlayers = GameState.getNrPlayers();

        for (int player_i = 0; player_i < nrPlayers; player_i++) {
            int nrPawnsFinished = 0;
            for (Pawn pawn: pawns){
                if(pawn.getPlayerId() == player_i && pawn.getCurrentTileId().getTileNr() > 15){
                    nrPawnsFinished++;
                }
            }
            if(nrPawnsFinished == 4 && !winners.contains(player_i)){
                winners.add(player_i);
            }
        }
    }
}
