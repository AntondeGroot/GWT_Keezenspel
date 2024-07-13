package gwtks;


import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;

public class Game {
    private Context2d ctxPawns;
    private Context2d ctxBoard;
    private Board board;
    private boolean isInitialized = false;

    public Game() {
        Document document = Document.get();

        ctxPawns = ((CanvasElement) document.getElementById("canvasPawns")).getContext2d();
        ctxBoard = ((CanvasElement) document.getElementById("canvasBoard")).getContext2d();

        board = new Board();
    }

    public void update(){
    }

    public void draw(){
        board.drawPawns(ctxPawns);

        if(!isInitialized){
            board.drawBoard(ctxBoard);
            isInitialized = true;
        }
    }

}
