package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Objects;

public class Player implements IsSerializable {
    private boolean isActive;
    private boolean isPlaying;
    private String name;
    private int place = -1;
    private String uuid;
    private int color = 0;

    public Player() {}

    public Player(String name, String uuid) {
        this.isActive = true;
        this.name = name;
        this.uuid = uuid;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor(){
        return color;
    }

    public void setPlace(int place) {
        this.place = place;
        this.isActive = false;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getUUID(){return uuid;}

    public void setIsPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    public Boolean isPlaying() {
        return isPlaying;
    }

    public int getPlace() {
        return place;
    }
    public String getName() {return name;}

    public boolean isActive() {return isActive;}

    public void setActive() {
        if(this.place == -1){
            // only allow players who haven't finished yet to be set to active when a new round starts
            isActive = true;
        }
    }

    public boolean hasFinished(){
        return place > 0;
    }

    public void setInactive(){
        isActive = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return isActive == player.isActive && isPlaying == player.isPlaying && place == player.place && color == player.color && Objects.equals(name, player.name) && Objects.equals(uuid, player.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isActive, isPlaying, name, place, uuid, color);
    }

    @Override
    public String toString() {
        return "\nPlayer{" +
                "name='" + name + '\'' +
                ", isPlaying=" + isPlaying +
                ", isActive=" + isActive +
                "}";
    }
}
