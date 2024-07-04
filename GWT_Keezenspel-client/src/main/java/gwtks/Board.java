package gwtks;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.List;

public class Board implements IsSerializable {
    private static List<TileMapping> mappings = new ArrayList<>();
	private double cellDistance;

    public void createBoard(int nrPlayers, double boardSize) {
		// Clear the mappings list before creating a new board
		mappings.clear();

		cellDistance = CellDistance.getCellDistance(nrPlayers, boardSize);
		Point startPoint = CellDistance.getStartPoint(nrPlayers, boardSize);

			for (int j = 0; j < 16; j++) {
				mappings.add(new TileMapping(1, j, new Point(startPoint)));
				GWT.log(startPoint.toString());
				if( j < 6){
					startPoint.setY((int) (startPoint.getY()+cellDistance));
				} else if (j<10) {
					startPoint.setX(startPoint.getX()-cellDistance);
				}else {
					startPoint.setY(startPoint.getY()-cellDistance);
				}
			}

		List<TileMapping> tempMappings = new ArrayList<>();
		for (TileMapping mapping : mappings) {
			for (int k = 2; k <= nrPlayers; k++) {
				tempMappings.add(new TileMapping(k, mapping.getTileNr(), mapping.getPosition().rotate(new Point(300,300), 360.0/nrPlayers*(k-1))));
			}
		}

        mappings.addAll(tempMappings);
		mappings.add(new TileMapping(1,1,new Point(300,300)));
		for (TileMapping mapping : mappings) {
			GWT.log(mapping.toString());
		}
    }

	public Canvas drawBoard(Canvas canvas, int nrPlayers, double boardSize) {
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
}
