package gwtks;

public class PawnMapping {
    private int playerId;
    private Point currentPosition;
    private Point nestPosition;

    public PawnMapping(int playerId, Point nestPosition) {
        this.playerId = playerId;
        this.currentPosition = new Point(nestPosition);
        this.nestPosition = new Point(nestPosition);
    }

    public void movePawn(Point position){
        currentPosition = new Point(position);
    }

    public void kill(){
        currentPosition = new Point(nestPosition);
    }

    public void switchPawn(PawnMapping pawn){
        Point tempPosition = new Point(this.currentPosition);
        this.currentPosition = new Point(pawn.currentPosition);
        pawn.nestPosition = new Point(tempPosition);
    }

    public int getPlayerId() {return playerId;}
}
