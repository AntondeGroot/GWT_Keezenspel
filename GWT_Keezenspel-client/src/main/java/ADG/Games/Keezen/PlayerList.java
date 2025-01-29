package ADG.Games.Keezen;

import java.util.ArrayList;

public class PlayerList {
    private ArrayList<Player> players = new ArrayList<>();
    private boolean isUpToDate;

    public void refresh(){
        isUpToDate = false;
    }

    public void setPlayers(ArrayList<Player> players) {
        if(this.players.equals(players)){
            isUpToDate = true;
        }else{
            isUpToDate = false;
            this.players = players;
        }
    }
    public boolean isIsUpToDate(){
        return isUpToDate;
    }
}
