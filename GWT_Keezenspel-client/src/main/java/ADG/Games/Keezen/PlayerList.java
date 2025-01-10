package ADG.Games.Keezen;

import ADG.Games.Keezen.GameStateService;
import ADG.Games.Keezen.GameStateServiceAsync;
import com.google.gwt.core.client.GWT;

import java.util.ArrayList;

public class PlayerList {
    private static String playerIdPlaying;
    private final GameStateServiceAsync gameStateService = GWT.create(GameStateService.class);
    private static ArrayList<String> activePlayers = new ArrayList<>();
    private static ArrayList<String> winners = new ArrayList<>();
    private static boolean isUpToDate = false;
    private static int nrPlayers;

    public static void refresh(){
        isUpToDate = false;
    }

    public void setPlayerIdPlayingAndDrawPlayerList(String playerIdPlaying) {
        if (playerIdPlaying == PlayerList.playerIdPlaying && isUpToDate) {
            return;
        }
        PlayerList.playerIdPlaying = playerIdPlaying;
        isUpToDate = true;
    }
    public static boolean isIsUpToDate(){
        return isUpToDate;
    }
    public static void setNrPlayers(int nrPlayers){
        PlayerList.nrPlayers = nrPlayers;
    }

    public static void setActivePlayers(ArrayList<String> activePlayers){
        PlayerList.activePlayers = activePlayers;
    }

    public static String getPlayerIdPlaying() {
        return playerIdPlaying;
    }

    public static void setWinners(ArrayList<String> winners) {
        PlayerList.winners = winners;
    }
}
