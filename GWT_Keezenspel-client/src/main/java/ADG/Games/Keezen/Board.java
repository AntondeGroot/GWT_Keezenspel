package ADG.Games.Keezen;

import com.google.gwt.core.client.GWT;

import java.util.*;

import static ADG.Games.Keezen.util.PlayerUUIDUtil.*;

public class Board {

    private static final ArrayList<TileMapping> tiles = new ArrayList<>();
	private static ArrayList<Pawn> pawns = new ArrayList<>();
	private static double cellDistance;
	private static ArrayList<Player> players;
	private static HashMap<String, ArrayList<Point>> cardsDeckPointsPerPlayer = new HashMap<>();

	public static ArrayList<Point> getCardsDeckPointsForPlayer(String UUID) {
		if(!cardsDeckPointsPerPlayer.containsKey(UUID)) {
			new ArrayList<>();
		}

		return cardsDeckPointsPerPlayer.get(UUID);
	}

    public void createBoard(ArrayList<Player> players, double boardSize) {
		// Clear the mappings list before creating a new board
		tiles.clear();
		Board.players = players;
		int nrPlayers = players.size();
		cellDistance = CellDistance.getCellDistance(nrPlayers, boardSize);
		Point startPoint = CellDistance.getStartPoint(nrPlayers, boardSize);

		int lastPlayerInt = nrPlayers - 1;
		// create normal tiles
		for (int j = -9; j < 7; j++) {
			// for construction purposes it is easier to take one board section that consists only of 90 degrees angles
			//
			// for game purposes it is easier that the starting point is position 0 and the last position is 15
			//
			// therefore the construction goes from the last playerId, from position 7 til 15 and then
			// starts with playerId 0 from position 0 to 6
			//
			// then all the tiles are rotated based on the number of players,
			// where the playerId is updated based on the rotation.
			int playerId = (j < 0) ? lastPlayerInt : 0;
			int tileNr = (j < 0) ? j + 16 : j;
			tiles.add(new TileMapping(playerIntToUUID(playerId, players), tileNr, new Point(startPoint)));

			if( j < -3){
				// move downwards for 6 tiles
				startPoint.setY(startPoint.getY() + cellDistance);
			} else if (j<1) {
				// move to the left of the screen for 4 tiles
				startPoint.setX(startPoint.getX() - cellDistance);
			}else {
				// move upwards for 5 tiles
				startPoint.setY(startPoint.getY() - cellDistance);
			}
		}

		// create finish tiles
		Point point = new Point(getPosition(playerIntToUUID(lastPlayerInt, players),15));
		for (int i = 1; i <= 4; i++) {
			point.setY(point.getY() - cellDistance);
			String playerUUID = playerIntToUUID(0, players);
			tiles.add(new TileMapping(playerUUID, 15+i, new Point(point)));
		}

		// create nest tiles
		// they will be assigned negative values to distinguish them from the playing field
		// they will be assigned different negative values to distinguish them from each other
		// so that 2 pawns cannot end up on the same nest tile
		String playerUUID = playerIntToUUID(0, players);

		point = new Point(getPosition(playerUUID,1));
		point.setX(point.getX() - 1.5*cellDistance);
		tiles.add(new TileMapping(playerUUID, -1, new Point(point)));
		point.setX(point.getX() - cellDistance);
		tiles.add(new TileMapping(playerUUID, -2, new Point(point)));
		point.setY(point.getY() - cellDistance);
		tiles.add(new TileMapping(playerUUID, -3, new Point(point)));
		point.setX(point.getX() + cellDistance);
		tiles.add(new TileMapping(playerUUID, -4, new Point(point)));

		// to create the tiles for other players rotate all tiles
		List<TileMapping> tempTiles = new ArrayList<>();
		String playerId;
		for (TileMapping tile : tiles) {
			for (int k = 1; k < nrPlayers; k++) {
				int colorInt = (UUIDtoInt(tile.getPlayerId(), players)+k)%nrPlayers;
				playerId = playerIntToUUID(colorInt, players);
				tempTiles.add(new TileMapping(playerId, tile.getTileNr(), tile.getPosition().rotate(new Point(300,300), 360.0/nrPlayers*k)));
			}
		}
		// create "Tiles" for where the player's cards should be placed
		ArrayList<Point> cardsDeckPoints = new ArrayList<>();
		Point beginPoint = getPosition(playerUUID,1);
		beginPoint = new Point(beginPoint.getX(), beginPoint.getY()+cellDistance+3);
		Point endPoint = getPosition(playerIntToUUID(lastPlayerInt, players),13);
		endPoint = new Point(endPoint.getX(), endPoint.getY()+cellDistance+3);
		GWT.log("\n\n\n beginpoint" + beginPoint);
		cardsDeckPoints.add(new Point(beginPoint.getX(),beginPoint.getY()));
		cardsDeckPoints.add(new Point(endPoint.getX(), endPoint.getY()));
		cardsDeckPointsPerPlayer.put(playerUUID, cardsDeckPoints);

		// add points for cards
		for(int k = 1; k < nrPlayers; k++) {
			beginPoint = beginPoint.rotate(new Point(300,300), 360.0/nrPlayers);
			GWT.log("\n\n\n rotated beginpoint"+beginPoint);
			GWT.log("rotation angle = "+360.0/nrPlayers);
			endPoint = endPoint.rotate(new Point(300,300), 360.0/nrPlayers);
			ArrayList<Point> cardsDeckPoints2 = new ArrayList<>();
			cardsDeckPoints2.add(beginPoint);
			cardsDeckPoints2.add(endPoint);
			cardsDeckPointsPerPlayer.put(playerIntToUUID(k, players), cardsDeckPoints2);
		}

        tiles.addAll(tempTiles);

		// rotate all tiles based on player UUID
		String uuid = Cookie.getPlayerId();
		int playerint = UUIDtoInt(uuid, players);
		for(TileMapping tile : tiles) {
			tile.setPosition(
					tile.getPosition().rotate(new Point(300,300), -360.0/nrPlayers*playerint)
			);
		}
		// rotate all the points where all the players' cards should be displayed
		for(Map.Entry<String, ArrayList<Point>> entry : cardsDeckPointsPerPlayer.entrySet()){
			String uuid_i = entry.getKey();
			ArrayList<Point> templist = new ArrayList<>();
			for (Point p: entry.getValue()){
				p = p.rotate(new Point(300,300), -360.0/nrPlayers*playerint);
				templist.add(p);
			}
			cardsDeckPointsPerPlayer.put(uuid_i, templist);
		}
    }

	public static TileId getTileId(double x, double y) {
		double distance = Double.MAX_VALUE;
		TileMapping closestTile = null;
		for(TileMapping tile : tiles) {
			// todo: I do not know why y should be lowered by cellDistance for it to select the correct tile
			double tempDistance = Math.sqrt(Math.pow((x-tile.getPosition().getX()),2) + Math.pow((y - tile.getPosition().getY())-cellDistance,2));
			if(tempDistance < distance) {
				distance = tempDistance;
				closestTile = tile;
			}
		}

		if(distance < cellDistance/2 && closestTile != null){
			GWT.log("closest tile: " + closestTile.getPlayerId());
			return closestTile.getTileId();
		}else{
			return null;
		}
	}

	public static boolean isInitialized(){
        return !Board.getPawns().isEmpty();
    }

	public static void setPawns(ArrayList<Pawn> pawns) {
		if(Board.pawns.isEmpty()){
			pawns.sort(new PawnComparator());
			Board.pawns = pawns;
		}
	}

	public static Point getPosition(String playerId, int tileNr) {
		for (TileMapping mapping : tiles) {
			if (Objects.equals(playerId, mapping.getPlayerId()) && mapping.getTileNr() == tileNr) {
				return mapping.getPosition();
			}
		}
		return null;
	}

	public static List<TileMapping> getTiles() {
		return tiles;
	}

	public static ArrayList<Pawn> getPawns() {return pawns;}

	public static Pawn getPawn(PawnId pawnId) {
		for (Pawn pawn : pawns) {
			if(pawn.getPawnId().equals(pawnId)){
				return pawn;
			}
		}
		return null;
	}

	public static double getCellDistance() {
		return cellDistance;
	}
}
