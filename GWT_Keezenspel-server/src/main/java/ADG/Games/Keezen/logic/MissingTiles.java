package ADG.Games.Keezen.logic;

import ADG.Games.Keezen.TileId;

import java.util.LinkedList;

import static ADG.Games.Keezen.GameState.nextPlayerId;
import static ADG.Games.Keezen.GameState.previousPlayerId;

public class MissingTiles {
    // todo: maybe this is completely useless. It was first designed to highlight all 7 tiles when two pawns were selected with a 7
    // todo: but I decided against using it as using a textbox for number of steps is easier and not ambiguous when 2 trails overlap
    // todo: however this could still be used for utils, creating a path of tiles from A to B for testing.
    public static LinkedList<TileId> extrapolateMissingTiles(LinkedList<TileId> tileIds){
        if(tileIds.isEmpty()){
            return new LinkedList<>();
        }

        LinkedList<TileId> output = new LinkedList<>();
        LinkedList<TileId> output_cleanedUp = new LinkedList<>();
        for (int i = 0; i < tileIds.size()-1; i++) {
            TileId currentTile = tileIds.get(i);
            TileId nextTile = tileIds.get(i+1);

            boolean nextSection = !currentTile.getPlayerId().equals(nextTile.getPlayerId());
            boolean goesToFinish = nextTile.getTileNr() > 15 || currentTile.getTileNr() > 15;

            if((nextTile.getTileNr() > currentTile.getTileNr() && currentTile.getPlayerId().equals(nextTile.getPlayerId())) || nextPlayerId(currentTile.getPlayerId()).equals(nextTile.getPlayerId()) ){
                output.addAll(walkOverTiles(currentTile, nextTile,goesToFinish,1));

            }else{
                output.addAll(walkOverTiles(currentTile, nextTile,goesToFinish,-1));
            }
        }

        // clean up output
        for(TileId tile : output){
            if(output_cleanedUp.isEmpty() || !output_cleanedUp.getLast().equals(tile)){
                output_cleanedUp.add(tile);
            }
        }

        return output_cleanedUp;
    }

    private static LinkedList<TileId> walkOverTiles(TileId start, TileId end, Boolean goesToFinish, int direction){
        LinkedList<TileId> output = new LinkedList<>();
        String playerId = start.getPlayerId();
        int tileNr = start.getTileNr();
        output.add(start);
        if(start.equals(end)){
            return output;
        }

        for (int i = 0; i < 15; i++) {

            tileNr = tileNr + direction;
            if(tileNr == 16 && start.getTileNr() < 16){
                playerId = nextPlayerId(playerId);
            }
            if(tileNr == 15 && goesToFinish && start.getTileNr() > 15){
                playerId = previousPlayerId(playerId);
            }
            if(tileNr < 0){
                playerId = previousPlayerId(playerId);
                tileNr = 15;
            }
            if(!goesToFinish){
                tileNr = tileNr % 16;
            }
            TileId tileId = new TileId(playerId, tileNr);
            output.add(new TileId(playerId, tileNr));
            if(tileId.equals(end)){
                break;
            }
        }
        return output;
    }
}
