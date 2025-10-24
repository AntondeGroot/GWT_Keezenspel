package ADG.Games.Keezen.dto;

import ADG.Games.Keezen.TileId;

public class PawnClient {

  private final String playerId;

  private final String pawnId;

  private TileId currentTileId;

  private final TileId nestTileId;

  private final String uri;

  public PawnClient(PawnDTO pawnDTO) {
    this.playerId = pawnDTO.getPlayerId();
    this.pawnId = pawnDTO.getPlayerId() + "_" + pawnDTO.getPawnId().getPawnNr();
    this.currentTileId = new  TileId(pawnDTO.getCurrentTileId().getPlayerId(), pawnDTO.getCurrentTileId().getTileNr());
    this.nestTileId = new  TileId(pawnDTO.getNestTileId().getPlayerId(), pawnDTO.getNestTileId().getTileNr());
    this.uri = pawnDTO.getUri();
  }

  public String getPlayerId() {
    return playerId;
  }

  public String getPawnId() {
    return pawnId;
  }

  public TileId getCurrentTileId() {
    return currentTileId;
  }

  public void setCurrentTileId(TileId currentTileId) {
    this.currentTileId = currentTileId;
  }

  public TileId getNestTileId() {
    return nestTileId;
  }

  public String getUri() {
    return uri;
  }

}
