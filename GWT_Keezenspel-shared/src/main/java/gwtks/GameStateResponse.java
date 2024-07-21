package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;

public class GameStateResponse implements IsSerializable {
    private ArrayList<Pawn> pawns;
    private int playerIdTurn;
    private int nrPlayers;

    public GameStateResponse() {
    }

    public ArrayList<Pawn> getPawns() {
        return pawns;
    }

    public void setPawns(ArrayList<Pawn> pawns) {
        this.pawns = pawns;
    }

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

    @Override
    public String toString() {
        return "GameStateResponse{" +
                "pawns=" + pawns +
                ", playerIdTurn=" + playerIdTurn +
                ", nrPlayers=" + nrPlayers +
                '}';
    }
}
