package gwtks.animations;


import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import gwtks.Board;

public class GameAnimation {
    private Context2d ctxPawns;
    private Context2d ctxBoard;
    private Board board;
    private StepsAnimation stepsAnimation;
    private boolean isInitialized = false;

    public GameAnimation() {
        Document document = Document.get();

        ctxPawns = ((CanvasElement) document.getElementById("canvasPawns")).getContext2d();
        ctxBoard = ((CanvasElement) document.getElementById("canvasBoard")).getContext2d();

        board = new Board();
        stepsAnimation = new StepsAnimation();
    }

    public void update(){
        stepsAnimation.update();

    }

    public void draw(){
        stepsAnimation.draw();
        board.drawPawns(ctxPawns);

        if(!isInitialized){
            board.drawBoard(ctxBoard);
            isInitialized = true;
        }
    }

}
