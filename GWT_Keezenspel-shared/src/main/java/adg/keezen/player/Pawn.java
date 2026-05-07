package adg.keezen.player;

import adg.keezen.TileId;
import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.Objects;

public class Pawn implements IsSerializable {
  private PawnId pawnId;
  private TileId currentTileId;
  private TileId nestTileId;
  private String playerId;
  private Integer colorInt; // on server side there will be 8 colors to chose from

  public Pawn() {}

  public Pawn(PawnId pawnId, TileId nestTileId) {
    this.playerId = pawnId.getPlayerId();
    this.pawnId = new PawnId(pawnId);
    this.nestTileId = new TileId(nestTileId);
    this.currentTileId = new TileId(nestTileId);
  }

  public Pawn(PawnId pawnId, TileId nestTileId, Integer colorInt) {
    this.playerId = pawnId.getPlayerId();
    this.pawnId = new PawnId(pawnId);
    this.nestTileId = new TileId(nestTileId);
    this.currentTileId = new TileId(nestTileId);
    this.colorInt = colorInt;
  }

  public PawnId getPawnId() {
    return pawnId == null ? null : new PawnId(pawnId);
  }

  public void setPawnId(PawnId pawnId) {
    this.pawnId = pawnId == null ? null : new PawnId(pawnId);
  }

  public String getPlayerId() {
    return playerId;
  }

  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

  public Integer getColorInt() {
    return colorInt;
  }

  public void setColorInt(Integer colorInt) {
    this.colorInt = colorInt;
  }

  public TileId getCurrentTileId() {
    return currentTileId == null ? null : new TileId(currentTileId);
  }

  public void setCurrentTileId(TileId currentTileId) {
    if (currentTileId != null) {
      this.currentTileId = new TileId(currentTileId);
    }
  }

  public TileId getNestTileId() {
    return nestTileId == null ? null : new TileId(nestTileId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Pawn pawn = (Pawn) o;
    return Objects.equals(pawnId, pawn.pawnId);
  }

  /***
   * Normal pawn comparison only compares if they have the sameId
   * This also compares current position which is needed to update the PawnAndCardSelection
   * Otherwise it will think a pawn on the board is still in the nest
   * @param other
   * @return
   */
  public boolean equalsByIdAndPosition(Pawn other) {
    return other != null
        && Objects.equals(this.pawnId, other.pawnId)
        && Objects.equals(this.currentTileId, other.currentTileId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(pawnId);
  }

  @Override
  public String toString() {
    return "\nPawn{" + pawnId + " on " + currentTileId + "}";
  }
}
