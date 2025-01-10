package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;

public class GameStateResponse implements IsSerializable {
    private ArrayList<Pawn> pawns;
    private ArrayList<Player> players;
    private HashMap<String, Integer> playerColors;
    private String playerIdTurn;
    private int nrPlayers;
    private ArrayList<String> activePlayers;
    private ArrayList<String> winners;

    public GameStateResponse() {
    }

    public void setPlayerColors(HashMap<String, Integer> playerColors) {
        this.playerColors = playerColors;
    }

    public HashMap<String, Integer> getPlayerColors() {
        return playerColors;
    }

    public ArrayList<Pawn> getPawns() {
        return pawns;
    }

    public void setPawns(ArrayList<Pawn> pawns) {
        this.pawns = pawns;
    }
    //todo: you could remove activePlayers and winners as that info is already contained within Players

    public ArrayList<Player> getPlayers() {return players;}

    public void setPlayers(ArrayList<Player> players) {this.players = players;}

    public String getPlayerIdTurn() {
        return playerIdTurn;
    }

    public void setPlayerIdTurn(String playerIdTurn) {
        this.playerIdTurn = playerIdTurn;
    }

    public int getNrPlayers() {
        return nrPlayers;
    }

    public void setNrPlayers(int nrPlayers) {
        this.nrPlayers = nrPlayers;
    }

    public ArrayList<String> getActivePlayers() {
        return activePlayers;
    }

    public void setActivePlayers(ArrayList<String> activePlayers) {
        this.activePlayers = activePlayers;
    }

    public ArrayList<String> getWinners() {
        return winners;
    }

    public void setWinners(ArrayList<String> winners) {
        this.winners = winners;
    }

    @Override
    public String toString() {
        return "GameStateResponse{" +
                "pawns=" + pawns +
                ", playerIdTurn=" + playerIdTurn +
                ", nrPlayers=" + nrPlayers +
                ", activePlayers=" + activePlayers +
                ", winners=" + winners +
                '}';
    }
}
