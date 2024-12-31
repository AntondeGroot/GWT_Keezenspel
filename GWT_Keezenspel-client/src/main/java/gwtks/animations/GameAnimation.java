package gwtks.animations;


import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import gwtks.Board;
import gwtks.CardsDeck;
import gwtks.GameBoardView;
import gwtks.PawnAndCardSelection;

public class GameAnimation {
    private Context2d ctxPawns;
    private Context2d ctxBoard;
    private Context2d ctxSteps;
    private Context2d ctxCards;
    private Board board;
    private boolean isInitialized = false;

    public GameAnimation() {
        Document document = Document.get();

        ctxPawns = ((CanvasElement) document.getElementById("canvasPawns")).getContext2d();
        ctxBoard = ((CanvasElement) document.getElementById("canvasBoard")).getContext2d();
        ctxSteps = ((CanvasElement) document.getElementById("canvasSteps")).getContext2d();
        ctxCards = ((CanvasElement) document.getElementById("canvasCards")).getContext2d();
    }

    public void update(){
        StepsAnimation.update();
        GameBoardView gameBoardView = new GameBoardView();
        ctxSteps.clearRect(0, 0, 600, 600);
        if(PawnAndCardSelection.getDrawCards()) {
            ctxCards.clearRect(0, 0, 600, 800);
            gameBoardView.getCanvasCards().getContext2d().clearRect(0,0, gameBoardView.getCanvasCards().getWidth(), gameBoardView.getCanvasCards().getHeight());
            GWT.log("cards context is cleared");
        }
    }

    public void draw(){
        StepsAnimation.draw();

        if(PawnAndCardSelection.getDrawCards()) {
            // todo: move to presenter
            GameBoardView gameBoardView = new GameBoardView();
            gameBoardView.drawCards(CardsDeck.getCards());// todo: remove the old method
            gameBoardView.drawCards_new(CardsDeck.getCards()); // todo: keep this one
            PawnAndCardSelection.setCardsAreDrawn();
        }
        board = new Board();
        board.drawPawns(ctxPawns);
        if(!isInitialized){
            board.drawBoard(ctxBoard);
            isInitialized = true;
        }
    }
}
