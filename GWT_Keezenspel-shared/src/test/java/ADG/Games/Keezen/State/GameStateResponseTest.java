package ADG.Games.Keezen.State;

import static org.junit.Assert.assertFalse;

import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.TileId;
import java.util.ArrayList;
import org.junit.Test;

public class GameStateResponseTest {

  @Test
  public void testEquals() {
    GameStateResponse response = new GameStateResponse();
    GameStateResponse responseNew = new GameStateResponse();
    TileId tileIdOld = new TileId("0",1);
    TileId tileIdNew = new TileId("0",2);

    ArrayList<Pawn> pawns = new ArrayList<>();
    pawns.add(new Pawn(new PawnId("0",0), tileIdOld));
    pawns.add(new Pawn(new PawnId("0",0), tileIdOld));
    response.setPawns(pawns);

    ArrayList<Pawn> pawnsNew = new ArrayList<>();
    pawnsNew.add(new Pawn(new PawnId("0",0), tileIdNew));
    pawnsNew.add(new Pawn(new PawnId("0",0), tileIdNew));
    responseNew.setPawns(pawnsNew);

    // THEN
    assertFalse(response.equals(responseNew));
  }
}