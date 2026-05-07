package adg.keezen;


public class TileMapping {

  private final TileId tileId;
  private Point position;

  public TileMapping(String playerId, int tileNr, Point position) {
    this.tileId = new TileId(playerId, tileNr);
    this.position = new Point(position);
  }

  public String getPlayerId() {
    return tileId.getPlayerId();
  }

  public int getTileNr() {
    return tileId.getTileNr();
  }

  public void setPosition(Point position) {
    this.position = new Point(position);
  }

  public Point getPosition() {
    return new Point(position);
  }

  public TileId getTileId() {
    return new TileId(tileId);
  }

  @Override
  public String toString() {
    return "Mapping{" + tileId + " -> [" + position + "]}";
  }
}
