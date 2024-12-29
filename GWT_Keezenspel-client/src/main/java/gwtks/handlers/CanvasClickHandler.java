package gwtks.handlers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import gwtks.*;
import gwtks.Card;

import java.util.List;

import static com.google.gwt.user.client.Event.ONCLICK;

public class CanvasClickHandler {

    public static void addClickHandler(){
        GWT.log("handler added");
        Element canvas = DOM.getElementById("canvasCards");

        // Ensure the canvas element exists
        if(canvas == null){
            GWT.log("canvas is null");
        }
        if (canvas != null) {
            // Add a click handler to the canvas element
            Event.sinkEvents(canvas, ONCLICK);
            Event.setEventListener(canvas, new com.google.gwt.user.client.EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    if (event.getTypeInt() == ONCLICK) {
                        handleCanvasClick(event);
                    }
                }
            });
        }
    }

    private static void handleCanvasClick(Event event) {
        Element canvas = DOM.getElementById("canvasCards");
        // Use getBoundingClientRect for accurate canvas position
        // todo: replace getAbsoluteLeft / getAbsoluteTop by a non-deprecated way
        int canvasLeft = DOM.getAbsoluteLeft((Element) canvas) - Window.getScrollLeft();
        int canvasTop = DOM.getAbsoluteTop((Element) canvas) - Window.getScrollTop();
        int x = event.getClientX() - canvasLeft;
        int y = event.getClientY() - canvasTop + 30;

        if(y>600){
            handleOnCardsDeckClick(x,y);
        }else{
            handleOnBoardClick(x,y);
        }
    }

    private static void handleOnCardsDeckClick(int x, int y){
        GWT.log("Clicked on CardsDeck : "+x+","+y);
        int padding = 10;
        int start = 0;
        int end = 0;
        int cardNr = -1;

        //TODO: make it independent on hard coded values
        for (int i = 0; i < 5; i++) {
            start = (100+padding)*i;
            end = start + 100;
            if(start < x && end > x){
                cardNr = i;
                break;
            }
        }
        if(y < 620 || y>620+158){
            cardNr = -1;
        }

        GWT.log("cardNr = "+cardNr);
        if(cardNr > -1){
            Document document = Document.get();
            InputElement nrSteps = (InputElement) document.getElementById("stepsNr");
            nrSteps.setValue("0");
            InputElement moveType = (InputElement) document.getElementById("moveType");
            moveType.setValue("");

            Card card = CardsDeck.pickCard(cardNr);
            PawnAndCardSelection.setCardNr(cardNr);
            if(card == null){return;}

            PawnAndCardSelection.setCard(card);
            PawnAndCardSelection.setCardNr(cardNr);

            if(PawnAndCardSelection.getPawn1() != null && PawnAndCardSelection.getCard() != null){
                TestMoveHandler testMoveHandler = new TestMoveHandler();
                testMoveHandler.sendMoveToServer();
            }
        }
        else{
            PawnAndCardSelection.reset();
        }
    }

    private static void handleOnBoardClick(int x, int y){
        List<TileMapping> tiles = Board.getTiles();
        List<Pawn> pawns = Board.getPawns();
        GWT.log("Clicked on Board : "+x+","+y);
        for (Pawn pawn : pawns) {
            TileId tileOfPawn = pawn.getCurrentTileId();
            for (TileMapping tile : tiles) {
                if (tile.getTileId().equals(tileOfPawn)) {
                    if(isWithinDistance(tile.getPosition(), new Point(x,y))){
                        PawnAndCardSelection.addPawn(pawn);

                        // for debugging
                        GWT.log("You clicked on pawn"+pawn.getPawnId()+" position: "+tile.getPosition());
                        Document document = Document.get();
                        InputElement playerId = (InputElement) document.getElementById("playerId");
                        playerId.setValue(String.valueOf(PawnAndCardSelection.getPlayerId()));
                        InputElement pawnId = (InputElement) document.getElementById("pawnId");
                        pawnId.setValue(String.valueOf(pawn.getPawnId().getPawnNr()));
                        //

                        if(PawnAndCardSelection.getPawn1() != null && PawnAndCardSelection.getCard() != null){
                            TestMoveHandler testMoveHandler = new TestMoveHandler();
                            testMoveHandler.sendMoveToServer();
                        }
                        break;
                    }
                }
            }
        }


    }
    public static boolean isWithinDistance(Point p1, Point p2) {
        int distance = 15;
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        double dx = x2 - x1;
        double dy = y2 - y1;

        return (dx * dx + dy * dy) <= (distance * distance);
    }
}
