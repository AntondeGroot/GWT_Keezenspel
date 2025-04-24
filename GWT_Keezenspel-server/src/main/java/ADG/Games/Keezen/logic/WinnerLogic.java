package ADG.Games.Keezen.logic;

import static ADG.Games.Keezen.logic.BoardLogic.isPawnOnFinish;

import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.Player;

import java.util.ArrayList;

public class WinnerLogic {

    public static void checkForWinners(ArrayList<String> winners){
        ArrayList<Pawn> pawns = GameState.getPawns();
        ArrayList<Player> players = GameState.getPlayers();

        for(Player player : players){
            int nrPawnsFinished = 0;
            String playerId = player.getUUID();
            for (Pawn pawn: pawns){
                if(playerId.equals(pawn.getPlayerId()) && isPawnOnFinish(pawn)){
                    nrPawnsFinished++;
                }
            }
            if(nrPawnsFinished == 4 && !winners.contains(playerId)){
                player.setPlace(winners.size()+1);
                winners.add(playerId);
            }
        }
    }
}
