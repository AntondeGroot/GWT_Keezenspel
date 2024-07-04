package gwtks.Board;

import java.util.ArrayList;
import java.util.List;

public class BoardSection {
    private long playerId;
    private List<Tile> tiles = new ArrayList<>();
    private List<Tile> nestTiles = new ArrayList<>();
    private List<Tile> finishTiles = new ArrayList<>();


    public BoardSection(long playerId) {
        this.playerId = playerId;

        createArmTiles();
        createFinishTiles();
        createNestTiles();
    }

    private void createNestTiles() {

        // place tiles
        nestTiles.add(new Tile(this.playerId, TileType.NEST));
        nestTiles.add(new Tile(this.playerId, TileType.NEST));
        nestTiles.add(new Tile(this.playerId, TileType.NEST));
        nestTiles.add(new Tile(this.playerId, TileType.NEST));
    }

    private void createFinishTiles() {
        // place tiles
        for (int i = 0; i < 4; i++) {
            finishTiles.add(new Tile(this.playerId, TileType.FINISH));
        }
    }

    private void createArmTiles() {
        createTiles(1, TileType.STARTCONSTRUCTION);
        createTiles(7, TileType.NORMAL);
        createTiles(1, TileType.LASTNORMAL);
        createTiles(1, TileType.START);
        createTiles(5,TileType.NORMAL);
        createTiles(1,TileType.ENDCONSTRUCTION);
    }

    public List<Tile> getAllTiles() {
        return this.tiles;
    }

    private void createTiles(int numberOfTiles,TileType tileType) {
        for (int i = 0; i < numberOfTiles; i++) {
            tiles.add(new Tile(this.playerId, tileType));
        }
    }
}
