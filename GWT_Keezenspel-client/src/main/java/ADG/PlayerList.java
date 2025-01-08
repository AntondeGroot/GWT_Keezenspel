package ADG;

import com.google.gwt.core.client.GWT;

import java.util.ArrayList;

public class PlayerList {
    private static int playerIdPlaying;
    private final GameStateServiceAsync gameStateService = GWT.create(GameStateService.class);
    private static ArrayList<Integer> activePlayers = new ArrayList<>();
    private static ArrayList<Integer> winners = new ArrayList<>();
    private static boolean isUpToDate = false;
    private static int nrPlayers;

    public static void refresh(){
        isUpToDate = false;
    }

    public void setPlayerIdPlayingAndDrawPlayerList(int playerIdPlaying) {
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

    public static void setActivePlayers(ArrayList<Integer> activePlayers){
        PlayerList.activePlayers = activePlayers;
    }

    public static int getPlayerIdPlaying() {
        return playerIdPlaying;
    }

    public static void setWinners(ArrayList<Integer> winners) {
        PlayerList.winners = winners;
    }
}
