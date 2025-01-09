package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;

public class GameStateResponse implements IsSerializable {
    private ArrayList<Pawn> pawns;
    private ArrayList<Player> players;
    private int playerIdTurn;
    private int nrPlayers;
    private ArrayList<Integer> activePlayers;
    private ArrayList<Integer> winners;

    public GameStateResponse() {
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

    public int getPlayerIdTurn() {
        return playerIdTurn;
    }

    public void setPlayerIdTurn(int playerIdTurn) {
        this.playerIdTurn = playerIdTurn;
    }

    public int getNrPlayers() {
        return nrPlayers;
    }

    public void setNrPlayers(int nrPlayers) {
        this.nrPlayers = nrPlayers;
    }

    public ArrayList<Integer> getActivePlayers() {
        return activePlayers;
    }

    public void setActivePlayers(ArrayList<Integer> activePlayers) {
        this.activePlayers = activePlayers;
    }

    public ArrayList<Integer> getWinners() {
        return winners;
    }

    public void setWinners(ArrayList<Integer> winners) {
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
