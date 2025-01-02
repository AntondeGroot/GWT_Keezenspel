package gwtks.animations;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import gwtks.Board;
import gwtks.GameBoardView;
import gwtks.TileId;
import gwtks.TileMapping;

import java.util.ArrayList;
import java.util.List;

public class StepsAnimation {
    private static List<TileMapping> tiles = new ArrayList<>();
    private static double loopAlpha = 0.6;
    private static double ALPHA_MAX = 0.6;
    private static List<TileId> tileIds;
    private static double cellDistance;

    public static void update(){
        if(cellDistance == 0){
            cellDistance = Board.getCellDistance();
        }
    }

    public static void update(List<TileId> tileIds){
        StepsAnimation.tileIds = tileIds;
    }

    public static void draw() {
        if(tileIds == null){return;}

        GameBoardView gameBoardView = new GameBoardView();
        gameBoardView.getCanvasStepsContext().clearRect(0,0,600,600);

        loopAlpha -= 0.005;
        if (loopAlpha <= 0.0) {
            loopAlpha = ALPHA_MAX;
        }

        tiles = Board.getTiles();
        for (TileId tileId : tileIds) {
            for (TileMapping mapping : tiles) {
                if (mapping.getTileId().equals(tileId)) {
                    drawCircle(gameBoardView.getCanvasStepsContext(), mapping.getPosition().getX(), mapping.getPosition().getY(),cellDistance/2, loopAlpha);
                }
            }
        }
    }

    private static void drawCircle(Context2d context, double x, double y, double radius, double alpha) {
        context.beginPath();
        String fillColor = "rgba(255, 165, 0, " + alpha + ")";
        String fillColorStroke = "rgba(0, 0, 0, " + alpha/2 + ")";
        if(alpha >= 0){
            context.arc(x, y, radius*2*alpha, 0, 2 * Math.PI);
        }
        context.setFillStyle(fillColor);
        context.fill();
        context.setStrokeStyle(fillColorStroke);
        context.stroke();
        context.closePath();
    }

    public static void reset(){
        tileIds = null;
    }
}
