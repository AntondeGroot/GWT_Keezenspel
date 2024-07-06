package gwtks;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pawn {
    private int playerId;
    private TileId currentPosition;
    private TileId nestTileId;

    public Pawn(int playerId, int nestTileId) {
        this.playerId = playerId;
        this.currentPosition = new TileId(playerId, nestTileId);
        this.nestTileId = new TileId(playerId, nestTileId);
    }
}
