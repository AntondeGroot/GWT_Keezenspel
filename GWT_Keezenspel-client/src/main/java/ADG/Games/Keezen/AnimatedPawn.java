package ADG.Games.Keezen;

public class AnimatedPawn {
    private final Pawn pawn;
    private final Point point;

    public AnimatedPawn(Pawn pawn, Point point) {
        this.pawn = pawn;
        this.point = point;
    }

    public Pawn getPawn() {
        return pawn;
    }

    public Point getPoint() {
        return point;
    }
}
