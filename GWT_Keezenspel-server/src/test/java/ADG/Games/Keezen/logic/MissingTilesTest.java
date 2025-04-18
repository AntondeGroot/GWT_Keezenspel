package ADG.Games.Keezen.logic;

import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.Player.Player;
import ADG.Games.Keezen.TileId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static ADG.Games.Keezen.logic.MissingTiles.extrapolateMissingTiles;
import static org.junit.jupiter.api.Assertions.*;

class MissingTilesTest {

    @BeforeEach
    void setUp() {
        GameState.stop();
        GameState.addPlayer(new Player("player0","0"));
        GameState.addPlayer(new Player("player1","1"));
        GameState.addPlayer(new Player("player2","2"));
        GameState.start();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void test_tileNrs0and1_unchanged(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("0",0));
        tiles.add(new TileId("0",1));

        assertEquals(tiles, extrapolateMissingTiles(tiles));
    }
    @Test
    void test_tileNrs1and0_unchanged(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("0",1));
        tiles.add(new TileId("0",0));

        assertEquals(tiles, extrapolateMissingTiles(tiles));
    }

    @Test
    void test_tileNrs0and2_addInBetween(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("0",0));
        tiles.add(new TileId("0",2));

        LinkedList<TileId> expectedTiles = new LinkedList<>();
        expectedTiles.add(new TileId("0",0));
        expectedTiles.add(new TileId("0",1));
        expectedTiles.add(new TileId("0",2));

        assertEquals(expectedTiles, extrapolateMissingTiles(tiles));
    }
    @Test
    void test_tileNrs2and0_addInBetween(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("0",2));
        tiles.add(new TileId("0",0));

        LinkedList<TileId> expectedTiles = new LinkedList<>();
        expectedTiles.add(new TileId("0",2));
        expectedTiles.add(new TileId("0",1));
        expectedTiles.add(new TileId("0",0));

        assertEquals(expectedTiles, extrapolateMissingTiles(tiles));
    }
    // next segment
    @Test
    void test_tileNrs14and1_addInBetween(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("0",14));
        tiles.add(new TileId("1",1));

        LinkedList<TileId> expectedTiles = new LinkedList<>();
        expectedTiles.add(new TileId("0",14));
        expectedTiles.add(new TileId("0",15));
        expectedTiles.add(new TileId("1",0));
        expectedTiles.add(new TileId("1",1));

        assertEquals(expectedTiles, extrapolateMissingTiles(tiles));
    }

    @Test
    void test_tileNrs1and14_addInBetweenBackwards(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("1",1));
        tiles.add(new TileId("0",14));

        LinkedList<TileId> expectedTiles = new LinkedList<>();
        expectedTiles.add(new TileId("1",1));
        expectedTiles.add(new TileId("1",0));
        expectedTiles.add(new TileId("0",15));
        expectedTiles.add(new TileId("0",14));

        assertEquals(expectedTiles, extrapolateMissingTiles(tiles));
    }

    // pingpong normal tiles
    @Test
    void test_pingpong_normaltiles(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("0",0));
        tiles.add(new TileId("0",5));
        tiles.add(new TileId("0",0));
        tiles.add(new TileId("0",2));

        LinkedList<TileId> expectedTiles = new LinkedList<>();
        expectedTiles.add(new TileId("0",0));
        expectedTiles.add(new TileId("0",1));
        expectedTiles.add(new TileId("0",2));
        expectedTiles.add(new TileId("0",3));
        expectedTiles.add(new TileId("0",4));
        expectedTiles.add(new TileId("0",5));
        expectedTiles.add(new TileId("0",4));
        expectedTiles.add(new TileId("0",3));
        expectedTiles.add(new TileId("0",2));
        expectedTiles.add(new TileId("0",1));
        expectedTiles.add(new TileId("0",0));
        expectedTiles.add(new TileId("0",1));
        expectedTiles.add(new TileId("0",2));

        assertEquals(expectedTiles, extrapolateMissingTiles(tiles));
    }
    // pingpong over start
    @Test
    void test_pingpong_starttiles(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("2",14));
        tiles.add(new TileId("0",1));
        tiles.add(new TileId("2",14));
        tiles.add(new TileId("0",1));

        LinkedList<TileId> expectedTiles = new LinkedList<>();
        expectedTiles.add(new TileId("2",14));
        expectedTiles.add(new TileId("2",15));
        expectedTiles.add(new TileId("0",0));
        expectedTiles.add(new TileId("0",1));
        expectedTiles.add(new TileId("0",0));
        expectedTiles.add(new TileId("2",15));
        expectedTiles.add(new TileId("2",14));
        expectedTiles.add(new TileId("2",15));
        expectedTiles.add(new TileId("0",0));
        expectedTiles.add(new TileId("0",1));

        assertEquals(expectedTiles, extrapolateMissingTiles(tiles));
    }
    // pingpong in finish
    @Test
    void test_pingpong_onFinishTiles(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("2",16));
        tiles.add(new TileId("2",19));
        tiles.add(new TileId("2",16));
        tiles.add(new TileId("2",19));

        LinkedList<TileId> expectedTiles = new LinkedList<>();
        expectedTiles.add(new TileId("2",16));
        expectedTiles.add(new TileId("2",17));
        expectedTiles.add(new TileId("2",18));
        expectedTiles.add(new TileId("2",19));
        expectedTiles.add(new TileId("2",18));
        expectedTiles.add(new TileId("2",17));
        expectedTiles.add(new TileId("2",16));
        expectedTiles.add(new TileId("2",17));
        expectedTiles.add(new TileId("2",18));
        expectedTiles.add(new TileId("2",19));

        assertEquals(expectedTiles, extrapolateMissingTiles(tiles));
    }
    // pingpong out of finish
    @Test
    void test_pingpong_outOfFinishTiles_startingOnFinish(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("2",18));
        tiles.add(new TileId("1",14));
        tiles.add(new TileId("2",18));
        tiles.add(new TileId("1",14));

        LinkedList<TileId> expectedTiles = new LinkedList<>();
        expectedTiles.add(new TileId("2",18));
        expectedTiles.add(new TileId("2",17));
        expectedTiles.add(new TileId("2",16));
        expectedTiles.add(new TileId("1",15));
        expectedTiles.add(new TileId("1",14));
        expectedTiles.add(new TileId("1",15));
        expectedTiles.add(new TileId("2",16));
        expectedTiles.add(new TileId("2",17));
        expectedTiles.add(new TileId("2",18));
        expectedTiles.add(new TileId("2",17));
        expectedTiles.add(new TileId("2",16));
        expectedTiles.add(new TileId("1",15));
        expectedTiles.add(new TileId("1",14));

        assertEquals(expectedTiles, extrapolateMissingTiles(tiles));
    }
    @Test
    void test_pingpong_outOfFinishTiles_startingOutsideFinish(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("1",14));
        tiles.add(new TileId("2",18));
        tiles.add(new TileId("1",14));
        tiles.add(new TileId("2",18));

        LinkedList<TileId> expectedTiles = new LinkedList<>();
        expectedTiles.add(new TileId("1",14));
        expectedTiles.add(new TileId("1",15));
        expectedTiles.add(new TileId("2",16));
        expectedTiles.add(new TileId("2",17));
        expectedTiles.add(new TileId("2",18));
        expectedTiles.add(new TileId("2",17));
        expectedTiles.add(new TileId("2",16));
        expectedTiles.add(new TileId("1",15));
        expectedTiles.add(new TileId("1",14));
        expectedTiles.add(new TileId("1",15));
        expectedTiles.add(new TileId("2",16));
        expectedTiles.add(new TileId("2",17));
        expectedTiles.add(new TileId("2",18));

        assertEquals(expectedTiles, extrapolateMissingTiles(tiles));
    }

    @Test
    void test_onSameTile(){
        LinkedList<TileId> tiles = new LinkedList<>();
        tiles.add(new TileId("1",5));
        tiles.add(new TileId("1",5));

        LinkedList<TileId> expectedTiles = new LinkedList<>();
        expectedTiles.add(new TileId("1",5));

        assertEquals(expectedTiles, extrapolateMissingTiles(tiles));
    }
}