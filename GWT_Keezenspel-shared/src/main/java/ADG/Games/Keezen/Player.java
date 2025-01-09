package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Player implements IsSerializable {
    private boolean isActive;
    private boolean isPlaying;
    private String name;
    private int place = -1;
    private String uuid;

    public Player() {}

    public Player(String name, String uuid) {
        this.isActive = true;
        this.name = name;
        this.uuid = uuid;
    }

    public void setPlace(int place) {
        this.place = place;
        this.isActive = false;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

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

    public void setInActive(){
        isActive = false;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", isPlaying=" + isPlaying +
                ", isActive=" + isActive +
                '}';
    }
}
