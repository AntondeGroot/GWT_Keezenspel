package ADG.Games.Keezen;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;
import ADG.Games.Keezen.util.PawnRect;

import java.util.*;

import static ADG.Games.Keezen.util.PlayerUUIDColor.colorToUUID;
import static ADG.Games.Keezen.util.PlayerUUIDColor.UUIDtoColor;

public class Board {

    private static final ArrayList<TileMapping> tiles = new ArrayList<>();
	private static ArrayList<PawnAnimationMapping> animationMappings = new ArrayList<>();
	private static ArrayList<Pawn> pawns = new ArrayList<>();
	private static double cellDistance;
	private static ArrayList<Player> players;

    public void createBoard(ArrayList<Player> players, double boardSize) {
		// Clear the mappings list before creating a new board
		tiles.clear();
		Board.players = players;
		int nrPlayers = players.size();
		cellDistance = CellDistance.getCellDistance(nrPlayers, boardSize);
		Point startPoint = CellDistance.getStartPoint(nrPlayers, boardSize);

		// create normal tiles
		for (int j = -9; j < 7; j++) {
			// for construction purposes it is easier to take one board section that consists only of 90 degrees angles
			// for game purposes it is easier that the starting point is position 0 and the last position is 15
			// therefore the construction goes from the last playerId, from position 7 til 15 and then starts with playerId 1 position 0 to 6
			// then all the tiles are rotated based on the number of players, where the playerId is updated based on the rotation.
			int playerId = (j < 0) ? nrPlayers - 1 : 0;
			int tileNr = (j < 0) ? j + 16 : j;
			tiles.add(new TileMapping(colorToUUID(playerId, players), tileNr, new Point(startPoint)));

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
		Point point = new Point(getPosition(colorToUUID(nrPlayers-1, players),15));
		for (int i = 1; i <= 4; i++) {
			point.setY(point.getY() - cellDistance);
			String playerUUID = colorToUUID(0, players);
			tiles.add(new TileMapping(playerUUID, 15+i, new Point(point)));
		}

		// create nest tiles
		// they will be assigned negative values to distinguish them from the playing field
		// they will be assigned different negative values to distinguish them from each other so that 2 pawns cannot end up on the same nest tile
		String playerUUID = colorToUUID(0, players);

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
				int colorInt = (UUIDtoColor(tile.getPlayerId(), players)+k)%nrPlayers;
				GWT.log("color int: " + colorInt);
				playerId = colorToUUID(colorInt, players);
				tempTiles.add(new TileMapping(playerId, tile.getTileNr(), tile.getPosition().rotate(new Point(300,300), 360.0/nrPlayers*k)));
			}
		}

        tiles.addAll(tempTiles);
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

	public void drawBoard(Context2d context) {
		GWT.log("drawing board");

		for (TileMapping mapping : tiles) {
			String color = "#D3D3D3";
			int tileNr = mapping.getTileNr();
			// only player tiles get a color
			if (tileNr <= 0 || tileNr >= 16) {
				color = PlayerColors.getHexColor(UUIDtoColor(mapping.getPlayerId(), players));// todo: convert UUID string to int
			}
			drawCircle(context, mapping.getPosition().getX(), mapping.getPosition().getY(), cellDistance/2, color);
		}
		context.save();
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

	private void drawCircle(Context2d context, double x, double y, double radius, String color) {
		context.beginPath();
		context.arc(x, y, radius, 0, 2 * Math.PI);
		context.setFillStyle(color);
		context.fill();
		context.setStrokeStyle("#000000");
		context.stroke();
		context.closePath();
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

	public static List<Pawn> getPawns() {return pawns;}

	public static Pawn getPawn(PawnId pawnId) {
		for (Pawn pawn : pawns) {
			if(pawn.getPawnId().equals(pawnId)){
				return pawn;
			}
		}
		return null;
	}

	// TODO: ONLY DRAW PAWNS WHEN IT IS NECESSARY// TODO: ONLY DRAW PAWNS WHEN IT IS NECESSARY
	public void drawPawns(Context2d context){
		// sort the pawns vertically so that they don't overlap weirdly when drawn
		pawns.sort(new PawnComparator());
		for(Pawn pawn : pawns){
			if (shouldBeAnimated(pawn)) {
				Iterator<PawnAnimationMapping> iterator = animationMappings.iterator();
				while (iterator.hasNext()) {
					PawnAnimationMapping animation_Pawn_i = iterator.next();
					// only animate the killing of a pawn after all other moves of other pawns were animated
					if(!animation_Pawn_i.isAnimateLast()) {
						if (pawn.equals(animation_Pawn_i.getPawn())) {
							if (animation_Pawn_i.getPoints().isEmpty()) {
								iterator.remove(); // Remove the current element safely
							} else {
								LinkedList<Point> points = animation_Pawn_i.getPoints();
								if (!points.isEmpty()) {
									Point p = points.getFirst();
									drawPawnAnimated(context, pawn, p);
									GWT.log("draw animated : "+ pawn);
									points.removeFirst(); // Remove the first element safely
								}
							}
						}
					}else{
						GWT.log("draw statically : "+ animation_Pawn_i.getPawn());
						// draw the pawn that is about to be killed statically
						drawPawnAnimated(context, animation_Pawn_i.getPawn(), animation_Pawn_i.getPoints().getFirst());
						// if no other pawns to be drawn, start drawing this one.
						if (onlyPawnsToBeKilledAreLeft() && animation_Pawn_i.isAnimateLast()){
							animation_Pawn_i.setAnimateLast(false);
						}
					}
				}
			}else{
				drawPawn(context, pawn);
			}
		}
	}

	public static boolean onlyPawnsToBeKilledAreLeft(){
		return animationMappings.stream().allMatch(PawnAnimationMapping::isAnimateLast);
	}

	public static void movePawn(Pawn pawn, LinkedList<TileId> movePawn, boolean animateLast) {
		for (Pawn pawn1 : pawns) {
			if(pawn1.equals(pawn)){
				animationMappings.add(new PawnAnimationMapping(pawn1, movePawn, animateLast));
				pawn1.setCurrentTileId(movePawn.getLast());
			}
		}
	}

	public boolean shouldBeAnimated(Pawn pawn) {
		if(animationMappings.isEmpty()){
			return false;
		}

		for (PawnAnimationMapping animationMappings1 : animationMappings) {
			if (pawn.equals(animationMappings1.getPawn())) {
				return true;
			}
		}
		return false;
	}

	//todo: do not draw the pawns too often
	private void drawPawn(Context2d context, Pawn pawn){
		// Load an image and draw it to the canvas
		String playerId = pawn.getPlayerId();
		int playerInt = UUIDtoColor(playerId, players);
		Image image = new Image("/pawn"+playerInt+".png");
		Image image_outline = new Image("/pawn_outline.png");

		double desiredWidth = 40;
		double desiredHeight = 40;
		Point point = new Point(0,0);
		// Draw the image on the canvas once it's loaded

		for (TileMapping mapping : tiles) {
			if(mapping.getTileId().equals(pawn.getCurrentTileId())){
				point = mapping.getPosition();
			}
		}
		context.drawImage(ImageElement.as(image.getElement()), point.getX()-desiredWidth/2, point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight);
		if(PawnAndCardSelection.getPawn1().equals(pawn) || PawnAndCardSelection.getPawn2().equals(pawn)){
			context.drawImage(ImageElement.as(image_outline.getElement()), point.getX()-desiredWidth/2, point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight);
		}
	}

	public static double getCellDistance() {
		return cellDistance;
	}

	private void drawPawnAnimated(Context2d context, Pawn pawn, Point point){
		// Load an image and draw it to the canvas
		String playerId = pawn.getPlayerId();
		int playerInt = UUIDtoColor(playerId, players);
		Image image = new Image("/pawn"+playerInt+".png");

		double[] xywh = PawnRect.getRect(point);
		context.drawImage(ImageElement.as(image.getElement()), xywh[0], xywh[1], xywh[2], xywh[3] );
	}
}
