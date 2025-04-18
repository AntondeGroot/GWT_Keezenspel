package ADG.Games.Keezen.util;

import ADG.Games.Keezen.Player.Player;

import java.util.ArrayList;

public class PlayerUUIDUtil {
    //todo: maybe this can be made obsolete by adding PlayerInt to relevent tiles and players
    public static String playerIntToUUID(int playerIndex, ArrayList<Player> players){
        for (Player p : players) {
            if(p.getIndex() == playerIndex){// todo: replace get color with index
                return p.getUUID();
            }
        }
        return "";
    }

    //todo: maybe this can be made obsolete by adding PlayerInt to relevent tiles and players
    public static int UUIDtoInt(String UUID, ArrayList<Player> players){
        if(UUID == null){
            return 0;
        }

        for (Player p : players) {
            if(p.getUUID().equals(UUID)){
                return p.getIndex();
            }
        }
        return 0;
    }
}
