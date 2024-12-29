package gwtks;

public class Player {
    private boolean isActive;
    private final String name;
    private int place = -1;

    public Player(String name) {
        this.isActive = true;
        this.name = name;
    }

    public void setPlace(int place) {
        this.place = place;
        this.isActive = false;
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

    public void setInActive(){
        isActive = false;
    }
}
