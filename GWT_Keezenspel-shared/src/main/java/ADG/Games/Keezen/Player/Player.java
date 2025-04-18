package ADG.Games.Keezen.Player;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Objects;

public class Player implements IsSerializable {
    private boolean isActive;
    private boolean isPlaying;
    private String name;
    private int place = -1;
    private String uuid;
    private int index;

    public Player() {}

    public Player(String name, String uuid) {
        this.isActive = true;
        this.name = name;
        this.uuid = uuid;
    }

    /**
     * The client sends messages based on their UUID
     * Index is used for drawing some stuff but not validating whether moves are legal
     */
    public void setIndex(int index) {
        this.index = index;
    }
    /**
     * The client sends messages based on their UUID
     * Index is used for drawing some stuff but not validating whether moves are legal
     */
    public int getIndex(){
        return index;
    }

    public void setPlace(int place) {
        this.place = place;
        this.isActive = false;
    }
    /**
     * The client sends messages based on their UUID
     * Index is used for drawing some stuff but not validating whether moves are legal
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
    /**
     * The client sends messages based on their UUID
     * Index is used for drawing some stuff but not validating whether moves are legal
     */
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
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        Player player = (Player) o;
        return isActive == player.isActive && isPlaying == player.isPlaying && place == player.place && index == player.index && Objects.equals(name, player.name) && Objects.equals(uuid, player.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isActive, isPlaying, name, place, uuid, index);
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
