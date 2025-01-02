package gwtks;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
