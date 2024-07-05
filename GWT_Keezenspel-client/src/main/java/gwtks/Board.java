package gwtks;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Board implements IsSerializable {

    private static List<TileMapping> mappings = new ArrayList<>();

	private double cellDistance;

    public void createBoard(int nrPlayers, double boardSize) {
		// Clear the mappings list before creating a new board
		mappings.clear();

		cellDistance = CellDistance.getCellDistance(nrPlayers, boardSize);
		Point startPoint = CellDistance.getStartPoint(nrPlayers, boardSize);

		for (int j = -9; j < 7; j++) {
			// for construction purposes it is easier to take one boardsection that consists only of 90 degrees angles
			// for game purposes it is easier that the starting point is position 0 and the last position is 15
			// therefore the construction goes from the last userId, from position 7 til 15 and then starts with userId 1 position 0 to 6
			// then all the tiles are rotated based on the number of players, where the userId is updated based on the rotation.
			int userId = (j < 0) ? nrPlayers - 1 : 1;
			int tileNr = (j < 0) ? j + 16 : j;
			mappings.add(new TileMapping(userId, tileNr, new Point(startPoint)));

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

		List<TileMapping> tempMappings = new ArrayList<>();
		for (TileMapping mapping : mappings) {
			for (int k = 1; k < nrPlayers; k++) {
				tempMappings.add(new TileMapping(k, mapping.getTileNr(), mapping.getPosition().rotate(new Point(300,300), 360.0/nrPlayers*k)));
			}
		}

        mappings.addAll(tempMappings);
		for (TileMapping mapping : mappings) {
			GWT.log(mapping.toString());
		}
    }

	public Canvas drawBoard(Canvas canvas) {
		canvas.setHeight("600px");
		canvas.setWidth("600px");
		Context2d context = canvas.getContext2d();

		//context.clearRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
		context.clearRect(0, 0, 600, 600);
		double cellSize = 40.0;

		for (TileMapping mapping : mappings) {
			drawCircle(context, mapping.getPosition().getX(), mapping.getPosition().getY(), cellDistance/2);
		}

		GWT.log(mappings.toString());
		return canvas;
	}

	private void drawCircle(Context2d context, double x, double y, double radius) {
		context.beginPath();
		context.arc(x, y, radius, 0, 2 * Math.PI);
		context.setFillStyle("#D3D3D3");
		context.fill();
		context.setStrokeStyle("#000000");
		context.stroke();
		context.closePath();
	}

    public static Point getPosition(int userId, int tileNr) {
		for (TileMapping mapping : mappings) {
			if (mapping.getUserId() == userId && mapping.getTileNr() == tileNr) {
				return mapping.getPosition();
			}
		}
		return null;
    }

	public static List<TileMapping> getMappings() {
		return mappings;
	}
}
