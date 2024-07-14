package gwtks.handlers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import gwtks.*;

import java.util.List;

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
            Event.sinkEvents(canvas,Event.ONCLICK);
            Event.setEventListener(canvas, new com.google.gwt.user.client.EventListener() {
                @Override
                public void onBrowserEvent(com.google.gwt.user.client.Event event) {
                    if (event.getTypeInt() == com.google.gwt.user.client.Event.ONCLICK) {
                        handleCanvasClick(event);
                    }
                }
            });
        }
    }

    private static void handleCanvasClick(Event event) {
        Element canvas = DOM.getElementById("canvasCards");
        int canvasTop = canvas.getAbsoluteTop();
        int canvasLeft = canvas.getAbsoluteLeft();
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
        GWT.log("cardNr = "+cardNr);
        if(cardNr > -1){
            Document document = Document.get();
            InputElement nrSteps = (InputElement) document.getElementById("stepsNr");
            nrSteps.setValue("");
            InputElement moveType = (InputElement) document.getElementById("moveType");
            moveType.setValue("");
            GWT.log("anton test");

            Card card = CardsDeck.pickCard(cardNr);
            if(card == null){return;}

            int cardValue = card.getCard() + 1; // go from spriteNr to face value of card
            if(cardValue == 1){
                // ace: onboard OR move
                moveType.setValue("ONBOARD|MOVE");
            }else if(cardValue == 11){
                // jack: switch pawns
                moveType.setValue("SWITCH");
            }else if(cardValue == 13){
                // king: onboard
                moveType.setValue("ONBOARD");
            }else{
                // move
                if(cardValue == 4){
                    cardValue = -4;
                }
                moveType.setValue("MOVE");
                nrSteps.setValue(String.valueOf(cardValue));
            }
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
                        GWT.log("You clicked on pawn"+pawn.getPawnId()+" position: "+tile.getPosition());
                        Document document = Document.get();
                        InputElement playerId = (InputElement) document.getElementById("playerId");
                        playerId.setValue(String.valueOf(pawn.getPlayerId()));
                        InputElement pawnId = (InputElement) document.getElementById("pawnId");
                        pawnId.setValue(String.valueOf(pawn.getPawnId().getPawnNr()));
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
