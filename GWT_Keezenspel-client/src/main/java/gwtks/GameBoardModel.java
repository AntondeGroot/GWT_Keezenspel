package gwtks;

import java.util.ArrayList;

public class GameBoardModel {
    // todo: is this model useful?
    private ArrayList<Player> players = new ArrayList<>();

    public GameBoardModel(){
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
