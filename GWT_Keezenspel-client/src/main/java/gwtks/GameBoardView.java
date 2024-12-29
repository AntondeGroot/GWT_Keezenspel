package gwtks;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GameBoardView extends Composite {

    interface Binder extends UiBinder<Widget, GameBoardView> {}
    private static Binder uiBinder = GWT.create(Binder.class);
//    private ArrayList<TileMapping> tiles = new ArrayList<>(); // todo: set tiles when view is constructed
//    private double cellDistance;
//    private static ArrayList<PawnAnimationMapping> animationMappings = new ArrayList<>();

    @UiField
    CanvasElement canvasBoard;

    // todo: rename to steps animation or something like that
    @UiField
    CanvasElement canvasSteps;

    @UiField
    CanvasElement canvasPawns;

    @UiField
    CanvasElement canvasCards;

    @UiField
    Button sendButton;

    @UiField
    Button forfeitButton;

    @UiField
    Label errorLabel;

    @UiField
    VerticalPanel playerListContainer;


    //todo: would this work?
//    private Context2d ctxBoard;
//    private Context2d ctxPawns;
//    private Context2d ctxCards;

    @Override
    public void onLoad() {
        super.onLoad();
        // todo: would this work?
        // Get the Context2d from the CanvasElement
//        ctxBoard = canvasBoard.getContext2d();
//        ctxPawns = canvasPawns.getContext2d();
//        ctxCards = canvasCards.getContext2d();
    }

    public GameBoardView() {
        initWidget(uiBinder.createAndBindUi(this));
//        this.tiles = tiles;
//        this.cellDistance = celldistance;
    }

    public Button getSendButton(){
        return sendButton;
    }

    public Button getForfeitButton(){
        return forfeitButton;
    }

    public VerticalPanel getPlayerListContainer(){return playerListContainer;}

    public CanvasElement getCanvasBoard(){return canvasBoard;}

    public CanvasElement getCanvasPawns(){return canvasPawns;}

    public CanvasElement getCanvasCards(){return canvasCards;}

    public void drawPawns(ArrayList<Pawn> pawns){
        // TODO: ONLY DRAW PAWNS WHEN IT IS NECESSARY
//        pawns.sort(new PawnComparator());
//        for(Pawn pawn : pawns){
//            if (shouldBeAnimated(pawn)) {
//                Iterator<PawnAnimationMapping> iterator = animationMappings.iterator();
//                while (iterator.hasNext()) {
//                    PawnAnimationMapping animationMappings1 = iterator.next();
//                    // only animate the killing of a pawn after all other moves of other pawns were animated
//                    if(!animationMappings1.isAnimateLast()) {
//                        if (pawn.equals(animationMappings1.getPawn())) {
//                            if (animationMappings1.getPoints().isEmpty()) {
//                                iterator.remove(); // Remove the current element safely
//                            } else {
//                                LinkedList<Point> points = animationMappings1.getPoints();
//                                if (!points.isEmpty()) {
//                                    Point p = points.getFirst();
//                                    drawPawnAnimated(pawn, p);
//                                    points.removeFirst(); // Remove the first element safely
//                                }
//                            }
//                        }
//                    }else{
//                        // draw the pawn that is about to be killed statically
//                        drawPawnAnimated(animationMappings1.getPawn(), animationMappings1.getPoints().getFirst());
//                        // if no other pawns to be drawn, start drawing this one.
//                        if (animationMappings.size() == 1){
//                            animationMappings1.setAnimateLast(false);
//                        }
//                    }
//                }
//            }else{
//                drawPawn(pawn);
//            }
//        }
    }

    private void drawPawn(Pawn pawn){
//        // Load an image and draw it to the canvas
//        int playerId = pawn.getPlayerId();
//        Image image = new Image("/pawn"+playerId+".png");
//        Image image_outline = new Image("/pawn_outline.png");
//
//        int desiredWidth = 40;
//        int desiredHeight = 40;
//        Point point = new Point(0,0);
//        // Draw the image on the canvas once it's loaded
//
//        for (TileMapping mapping : tiles) {
//            if(mapping.getTileId().equals(pawn.getCurrentTileId())){
//                point = mapping.getPosition();
//            }
//        }
//        // draw  Pawn
//        ctxPawns.drawImage(ImageElement.as(image.getElement()), point.getX()-desiredWidth/2, point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight);
//        if(pawnWasSelected(pawn)){
//            // draw outline
//            ctxPawns.drawImage(ImageElement.as(image_outline.getElement()), point.getX()-desiredWidth/2, point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight);
//        }
    }

    private boolean pawnWasSelected(Pawn pawn){
//        return PawnAndCardSelection.getPawn1().equals(pawn) || PawnAndCardSelection.getPawn2().equals(pawn);
        return false;
    }

    public void drawBoard() {
//        GWT.log("drawing board");
//        double cellSize = 40.0;
//        String color = "";
//        int tileNr = 0;
//
//        for (TileMapping mapping : tiles) {
//            color = "#D3D3D3";
//            tileNr = mapping.getTileNr();
//            // only player tiles get a color
//            if (tileNr <= 0 || tileNr >= 16) {
//                color = PlayerColors.getHexColor(mapping.getPlayerId());
//            }
//            GWT.log("drawing tile " + mapping.getPlayerId() + " " + tileNr + ", "+mapping.getPosition());
//            drawCircle(mapping.getPosition().getX(), mapping.getPosition().getY(), cellDistance/2, color);
//        }
//        // todo: was this ever useful/necessary?
//        //context.save();
    }

    private void drawCircle(double x, double y, double radius, String color) {
//        Context2d context = ctxBoard;
//        context.beginPath();
//        context.arc(x, y, radius, 0, 2 * Math.PI);
//        context.setFillStyle(color);
//        context.fill();
//        context.setStrokeStyle("#000000");
//        context.stroke();
//        context.closePath();
    }

    private void drawPawnAnimated(Pawn pawn, Point point){
//        // Load an image and draw it to the canvas
//        int playerId = pawn.getPlayerId();
//        Image image = new Image("/pawn"+playerId+".png");
//
//        double[] xywh = PawnRect.getRect(point);
//        ctxPawns.drawImage(ImageElement.as(image.getElement()), xywh[0], xywh[1], xywh[2], xywh[3] );
    }

    public boolean shouldBeAnimated(Pawn pawn) {
//        if(animationMappings.isEmpty()){
//            return false;
//        }
//
//        for (PawnAnimationMapping animationMappings1 : animationMappings) {
//            if (pawn.equals(animationMappings1.getPawn())) {
//                return true;
//            }
//        }
        return false;
    }

    public static void movePawn(Pawn pawn, LinkedList<TileId> movePawn, boolean animateLast) {
        //todo implement
//        animationMappings.add(new PawnAnimationMapping(pawn, movePawn, animateLast));
//        pawn1.setCurrentTileId(movePawn.getLast());
    }

    public void drawCards(List<Card> cards){
        ImageElement img = Document.get().createImageElement();
        img.setSrc("/card-deck.png");
        // todo: old
        Document document  = Document.get();
        Context2d ctxCards = ((CanvasElement) document.getElementById("canvasCards")).getContext2d();
        ctxCards.clearRect(0,0,600,800);
        // todo: new
        canvasCards.getContext2d().clearRect(0,0, canvasCards.getWidth(), canvasCards.getHeight());
        // card width 25 is good to show how many cards they are still holding
        // card width 100 is good for your own hand
        int i=0;
        for (Card card: cards){
            // source image
            double sw = 1920/13.0;
            double sh = 1150/5.0;
            double sx = sw*card.getCardValue();
            double sy = sh*card.getSuit();
            // destination
            int dy = 600;
            double dw = 100.0;
            double dx = 10+(dw+10)* i;
            double dh = dw/sw*sh;
            // for spritesheets dx dy
            ctxCards.drawImage(img, sx,sy,sw,sh,dx,dy,dw,dh); //todo: old
            double lineThickness = 3;
            if(PawnAndCardSelection.getCard() != null && PawnAndCardSelection.getCard().equals(card) && PawnAndCardSelection.getCardNr() == i){
                // todo: old
                drawRoundedRect(ctxCards, dx-lineThickness/2, dy-lineThickness/2, dw+lineThickness, dh+lineThickness, 8);
            }
            i++;
        }
    }

    public void drawCards_new(List<Card> cards) {
        // Create an image to represent the card deck
        Image img = new Image("/card-deck.png");

        // Add a LoadHandler to ensure the image is fully loaded before drawing
        img.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                // Clear the canvas to prepare for drawing new cards
                canvasCards.getContext2d().clearRect(0, 0, canvasCards.getWidth(), canvasCards.getHeight());

                // Loop through the cards to draw them
                for (int i = 0; i < cards.size(); i++) {
                    Card card = cards.get(i);

                    // Define the source rectangle (from the sprite sheet)
                    double spriteWidth = 1920 / 13.0;
                    double spriteHeight = 1150 / 5.0;
                    double sourceX = spriteWidth * card.getCardValue();
                    double sourceY = spriteHeight * card.getSuit();

                    // Define the destination rectangle (on the canvas)
                    double destX = 10 + (100.0 + 10) * i; // Offset for each card
                    double destY = 600; // Fixed Y position
                    double destWidth = 100.0; // Card width on canvas
                    double destHeight = destWidth / spriteWidth * spriteHeight; // Maintain aspect ratio

                    // Draw the card image on the canvas
                    canvasCards.getContext2d().drawImage(
                            ImageElement.as(img.getElement()),
                            sourceX, sourceY, spriteWidth, spriteHeight,
                            destX, destY, destWidth, destHeight
                    );

                    // Highlight selected card, if any
                    if (PawnAndCardSelection.getCard() != null &&
                            PawnAndCardSelection.getCard().equals(card) &&
                            PawnAndCardSelection.getCardNr() == i) {
                        drawRoundedRect(canvasCards.getContext2d(), destX - 1.5, destY - 1.5, destWidth + 3, destHeight + 3, 8);
                    }
                }
            }
        });

        // Trigger the image loading by adding it to the DOM
        img.setVisible(false);
        RootPanel.get().add(img);
    }

    private void drawRoundedRect(Context2d context, double x, double y, double width, double height, double radius) {
        context.beginPath();
        context.moveTo(x + radius, y);
        context.lineTo(x + width - radius, y);
        context.quadraticCurveTo(x + width, y, x + width, y + radius);
        context.lineTo(x + width, y + height - radius);
        context.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
        context.lineTo(x + radius, y + height);
        context.quadraticCurveTo(x, y + height, x, y + height - radius);
        context.lineTo(x, y + radius);
        context.quadraticCurveTo(x, y, x + radius, y);
        context.closePath();
        context.setStrokeStyle("red");
        context.setLineWidth(2);
        context.stroke();
    }
}