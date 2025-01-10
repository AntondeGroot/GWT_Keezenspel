package ADG.Games.Keezen.util;

import ADG.Games.Keezen.Player;

import java.util.ArrayList;

public class PlayerUUIDColor {

    public static String ColorToUUID(int Colorint, ArrayList<Player> players){
        for (Player p : players) {
            if(p.getColor() == Colorint){
                return p.getUUID();
            }
        }
        return "";
    }

    public static int UUIDtoColor(String UUID, ArrayList<Player> players){
        if(UUID == null){
            return 0;
        }

        for (Player p : players) {
            if(p.getUUID().equals(UUID)){
                return p.getColor();
            }
        }
        return 0;
    }
}
