package gwtks.animations;


import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import gwtks.Board;
import gwtks.CardsDeck;
import gwtks.GameBoardView;
import gwtks.PawnAndCardSelection;

public class GameAnimation {
    private Context2d ctxPawns;
    private Context2d ctxBoard;
    private boolean isInitialized = false;

    public GameAnimation() {
        Document document = Document.get();
        ctxPawns = ((CanvasElement) document.getElementById("canvasPawns2")).getContext2d();
        ctxBoard = ((CanvasElement) document.getElementById("canvasBoard2")).getContext2d();
    }

    public void update(){
        StepsAnimation.update();
        GameBoardView gameBoardView = new GameBoardView();
        gameBoardView.getCanvasStepsContext().clearRect(0,0,600,600);//todo: make a clear function for all canvasses
        if(PawnAndCardSelection.getDrawCards()) {
            gameBoardView.getCanvasCardsContext().clearRect(0,0, gameBoardView.getCanvasCards().getWidth(), gameBoardView.getCanvasCards().getHeight());//todo: new
        }
    }

    public void draw(){
        StepsAnimation.draw();

        if(PawnAndCardSelection.getDrawCards()) {
            // todo: move to presenter
            GameBoardView gameBoardView = new GameBoardView();
            gameBoardView.drawCards(CardsDeck.getCards());
            PawnAndCardSelection.setCardsAreDrawn();
        }
        Board board = new Board();
        board.drawPawns(ctxPawns);
        if(!isInitialized){
            board.drawBoard(ctxBoard);
            isInitialized = true;
        }
    }
}
