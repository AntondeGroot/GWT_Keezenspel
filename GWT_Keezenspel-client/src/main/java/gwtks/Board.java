package gwtks;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.ArrayList;
import java.util.List;

public class Board implements IsSerializable {

    private static List<TileMapping> tiles = new ArrayList<>();
	private static List<Pawn> pawns = new ArrayList<>();

	private double cellDistance;

    public void createBoard(int nrPlayers, double boardSize) {
		// Clear the mappings list before creating a new board
		tiles.clear();

		cellDistance = CellDistance.getCellDistance(nrPlayers, boardSize);
		Point startPoint = CellDistance.getStartPoint(nrPlayers, boardSize);

		// create normal tiles
		for (int j = -9; j < 7; j++) {
			// for construction purposes it is easier to take one boardsection that consists only of 90 degrees angles
			// for game purposes it is easier that the starting point is position 0 and the last position is 15
			// therefore the construction goes from the last playerId, from position 7 til 15 and then starts with playerId 1 position 0 to 6
			// then all the tiles are rotated based on the number of players, where the playerId is updated based on the rotation.
			int playerId = (j < 0) ? nrPlayers - 1 : 0;
			int tileNr = (j < 0) ? j + 16 : j;
			tiles.add(new TileMapping(playerId, tileNr, new Point(startPoint)));

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
		Point point = new Point(getPosition(nrPlayers-1,15));
		for (int i = 1; i <= 4; i++) {
			point.setY(point.getY() - cellDistance);
			tiles.add(new TileMapping(0, 15+i, new Point(point)));
		}

		// create nest tiles
		// they will be assigned negative values to distinguish them from the playing field
		// they will be assigned different negative values to distinguish them from each other so that 2 pawns cannot end up on the same nest tile
		point = new Point(getPosition(0,1));
		point.setX(point.getX() - 1.5*cellDistance);
		tiles.add(new TileMapping(0, -1, new Point(point)));
		point.setX(point.getX() - cellDistance);
		tiles.add(new TileMapping(0, -2, new Point(point)));
		point.setY(point.getY() - cellDistance);
		tiles.add(new TileMapping(0, -3, new Point(point)));
		point.setX(point.getX() + cellDistance);
		tiles.add(new TileMapping(0, -4, new Point(point)));

		// to create the tiles for other players rotate all tiles
		List<TileMapping> tempTiles = new ArrayList<>();
		int playerId = 0;
		for (TileMapping tile : tiles) {
			for (int k = 1; k < nrPlayers; k++) {
				playerId = (tile.getPlayerId()+k)%nrPlayers;
				tempTiles.add(new TileMapping(playerId, tile.getTileNr(), tile.getPosition().rotate(new Point(300,300), 360.0/nrPlayers*k)));
			}
		}

        tiles.addAll(tempTiles);

		// create pawns
		int pawnNr = 0;
		for (int i = 0; i < nrPlayers; i++) {
			pawnNr = 0;
			for (TileMapping tile : tiles) {
				if(tile.getPlayerId() == i && tile.getTileNr() < 0){//nest tiles are negative
					pawns.add(new Pawn(new PawnId(i,pawnNr), tile.getTileId()));
					pawnNr++;
				}
			}
		}
    }

	public Canvas drawBoard(Canvas canvas) {
		canvas.setHeight("600px");
		canvas.setWidth("600px");
		Context2d context = canvas.getContext2d();

		//context.clearRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
		context.clearRect(0, 0, 600, 600);
		double cellSize = 40.0;
		String color = "";
		int tileNr = 0;
		for (TileMapping mapping : tiles) {
			color = "#D3D3D3";
			tileNr = mapping.getTileNr();
			// only player tiles get a color
			if (tileNr <= 0 || tileNr >= 16) {
				color = Players.getColor(mapping.getPlayerId());
			}

			drawCircle(context, mapping.getPosition().getX(), mapping.getPosition().getY(), cellDistance/2, color);
		}

		return canvas;
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

	public static Point getPosition(int playerId, int tileNr) {
		for (TileMapping mapping : tiles) {
			if (mapping.getPlayerId() == playerId && mapping.getTileNr() == tileNr) {
				return mapping.getPosition();
			}
		}
		return null;
	}

    public static Point getPosition(TileId tileId) {
		for (TileMapping mapping : tiles) {
			if (mapping.getPlayerId() == tileId.getPlayerId() && mapping.getTileNr() == tileId.getTileNr()) {
				return mapping.getPosition();
			}
		}
		return null;
    }

	public static Point getTile(int playerId, int tileNr){
		for (TileMapping mapping : tiles) {
			if (mapping.getPlayerId() == playerId && mapping.getTileNr() == tileNr) {
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

	public void drawPawns(Canvas canvas){
		Context2d context = canvas.getContext2d();
//		context.clearRect(0, 0, context.getCanvas().getWidth(), context.getCanvas().getHeight());



		GWT.log("number of pawns to be drawn: "+ pawns.size());
		for(Pawn pawn : pawns){
			drawPawn(context, pawn);
		}
	}

	public static void movePawn(Pawn pawn, TileId tileId) {
		for (Pawn pawn1 : pawns) {
			if(pawn1.equals(pawn)){
				pawn1.setCurrentTileId(tileId);
			}
		}
	}

	private void drawPawn(Context2d context, Pawn pawn){
		// Load an image and draw it to the canvas
		Image image = new Image("/pawn.png");
		image.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				int desiredWidth = 40;
				int desiredHeight = 40;
				Point point = new Point(0,0);
				// Draw the image on the canvas once it's loaded

				for (TileMapping mapping : tiles) {
					if(mapping.getTileId().equals(pawn.getCurrentTileId())){
						point = mapping.getPosition();
						GWT.log("drew pawn "+pawn.getPawnId()+"," + pawn.getCurrentTileId());
					}
				}
				context.drawImage(ImageElement.as(image.getElement()), point.getX()-desiredWidth/2, point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight);
			}
		});

		// the image widget has to be added to the Rootpanel for it to trigger the OnLoad event
		image.setVisible(false); // Hide the image widget
		RootPanel.get().add(image);
	}
}
