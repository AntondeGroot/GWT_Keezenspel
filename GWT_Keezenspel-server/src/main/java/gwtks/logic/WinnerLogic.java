package gwtks.logic;

import gwtks.GameState;
import gwtks.Pawn;
import gwtks.Player;

import java.util.ArrayList;

public class WinnerLogic {

    public static void checkForWinners(ArrayList<Integer> winners){
        ArrayList<Pawn> pawns = GameState.getPawns();
        ArrayList<Player> players = GameState.getPlayers();
        int player_i = 0;
        for(Player player : players){
            int nrPawnsFinished = 0;
            for (Pawn pawn: pawns){
                if(pawn.getPlayerId() == player_i && pawn.getCurrentTileId().getTileNr() > 15){
                    nrPawnsFinished++;
                }
            }
            if(nrPawnsFinished == 4 && !winners.contains(player_i)){
                player.setPlace(winners.size()+1);
                winners.add(player_i);
            }
            player_i++;
        }
    }
}
