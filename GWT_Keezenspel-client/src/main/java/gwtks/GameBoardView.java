package gwtks;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GameBoardView extends Composite {

    private Document document;

    interface Binder extends UiBinder<Widget, GameBoardView> {}
    private static Binder uiBinder = GWT.create(Binder.class);
//    private ArrayList<TileMapping> tiles = new ArrayList<>(); // todo: set tiles when view is constructed
//    private double cellDistance;
//    private static ArrayList<PawnAnimationMapping> animationMappings = new ArrayList<>();

    @UiField
    Button sendButton;

    @UiField
    Button forfeitButton;

    @UiField
    Label errorLabel;

    @UiField
    VerticalPanel playerListContainer;

    //    needed to add click listeners to the Canvas. GWT Canvas does not have
    //    a dom element which you could find by .findElementById()
    @UiField
    HTMLPanel canvasWrapper;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public GameBoardView() {
        initWidget(uiBinder.createAndBindUi(this));
        document = Document.get();
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

    public Context2d getCanvasBoardContext(){return
            ((CanvasElement) document.getElementById("canvasBoard2")).getContext2d();}

    public Context2d getCanvasPawnsContext(){return ((CanvasElement) document.getElementById("canvasPawns2")).getContext2d();}

    public Context2d getCanvasStepsContext(){return ((CanvasElement) document.getElementById("canvasSteps2")).getContext2d();}

    public Context2d getCanvasCardsContext(){return ((CanvasElement) document.getElementById("canvasCards2")).getContext2d();}

    // todo: rename to canvasCards when old index.html is no longer used
    public CanvasElement getCanvasCards(){
        return (CanvasElement) document.getElementById("canvasCards2");
    }

    public CanvasElement getCanvasPawns(){
        return (CanvasElement) document.getElementById("canvasPawns2");
    }

    public void drawPlayers(ArrayList<Player> players){
        // todo: move this to CSS file
        String inactiveColor = "#c2bfb6";
        // todo: maybe check player whether they belong in column 1 or 2, so don't expand both when a winner has been declared
        List<Integer> column1 = Arrays.asList(0, 1, 2, 3);
        List<Integer> column2 = Arrays.asList(4, 5, 6, 7);
        int colCount = 2;
        int rowCount = (int) Math.ceil((double) players.size() / colCount);
        Grid grid = new Grid(rowCount, colCount);
        playerListContainer.clear();

        List<Player> winners = players.stream()
                .filter(player -> player.getPlace() > -1)
                .collect(Collectors.toList());

        GWT.log("winners: " + winners);
        int playerId = 0;
        for (Player player : players) {
            int imagePixelSize = 50;

            ImageElement img = Document.get().createImageElement();
            img.setSrc("/profilepics.png");
            HorizontalPanel hp = new HorizontalPanel();
            Label playerName = new Label(players.get(playerId).getName());
            playerName.getElement().getStyle().setMarginRight(30, Style.Unit.PX);
            playerName.getElement().getStyle().setMarginLeft(10, Style.Unit.PX);

            if(!players.get(playerId).isActive()){
                playerName.getElement().getStyle().setTextDecoration(Style.TextDecoration.LINE_THROUGH);
            }

            hp.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
            hp.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

            Canvas canvas = Canvas.createIfSupported();
            canvas.setWidth(imagePixelSize + "px");
            canvas.setHeight(imagePixelSize + "px");
            canvas.setCoordinateSpaceWidth(imagePixelSize);
            canvas.setCoordinateSpaceHeight(imagePixelSize);
            canvas.setStyleName("profilepic");
            if(players.get(playerId).isActive()){
                canvas.getElement().getStyle().setBorderColor(PlayerColors.getHexColor(playerId));
            }else{
                canvas.getElement().getStyle().setBorderColor(inactiveColor);
            }
            Context2d ctx = canvas.getContext2d();
            // source image
            double sw = 1024/4.0;
            double sh = 1024/4.0;
            double sx = sw*(playerId%4);
            double sy = sh*(playerId > 3 ? 1:0);
            // destination
            double dy = 0-5/2;
            double dx = 0-5/2;
            double dw = imagePixelSize+5;
            double dh = imagePixelSize+5;
            GWT.log("source image is "+sx+","+sh+", "+playerId);
            GWT.log("card size = "+dh+","+dw);
            // for spritesheets dx dy
            ctx.drawImage(img, sx,sy,sw,sh,dx,dy,dw,dh);
            canvas.asWidget().setStyleName("profilepic");
            if(player.isPlaying()) {
                hp.asWidget().setStyleName("playerPlaying");
            }

            // todo: the following text is not implemented
            // winners can be in either column 1 or 2: only add a medal to the winner,
            // but add an empty canvas to the other players in that column so that they are aligned
            if(!winners.isEmpty()){
                Canvas canvasMedal = Canvas.createIfSupported();
                canvasMedal.setWidth(imagePixelSize + "px");
                canvasMedal.setHeight(imagePixelSize + "px");
                canvasMedal.setCoordinateSpaceWidth(imagePixelSize);
                canvasMedal.setCoordinateSpaceHeight(imagePixelSize);

                ImageElement imgMedals = Document.get().createImageElement();
                imgMedals.setSrc("/medals.png");

                // image has 220px empty space on top and bottom
                // and is 2500px wide and 1668px high
                double sw1 = 2500/3.0;
                double sh1 = 1668-440;
                double sx1 = sw1*(player.getPlace()-1);
                double sy1 = 220;
                // destination
                double dy1 = 0;
                double dx1 = 0;
                double dh1 = imagePixelSize+5;
                double dw1 = dh1/sh1*sw1;

                Context2d ctxMedals = canvasMedal.getContext2d();
                if(winners.contains(player)){
                    ctxMedals.drawImage(imgMedals, sx1,sy1,sw1,sh1,dx1,dy1,dw1,dh1);
                }
                hp.add(canvasMedal.asWidget());
            }
            hp.add(canvas.asWidget());
            hp.add(playerName);
            int row = playerId % 4;
            int col = (playerId > 3) ? 1 : 0;
            grid.setWidget(row, col, hp);

            playerId++;
        }
        grid.getElement().getStyle().setMargin(50, Style.Unit.PX);
        playerListContainer.add(grid);
    }

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
        getCanvasCardsContext().clearRect(0,0, getCanvasCards().getWidth(), getCanvasCards().getHeight());
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
                getCanvasCardsContext().clearRect(0, 0, getCanvasCards().getWidth(), getCanvasCards().getHeight());

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
                    getCanvasCardsContext().drawImage(
                            ImageElement.as(img.getElement()),
                            sourceX, sourceY, spriteWidth, spriteHeight,
                            destX, destY, destWidth, destHeight
                    );

                    // Highlight selected card, if any
                    if (PawnAndCardSelection.getCard() != null &&
                            PawnAndCardSelection.getCard().equals(card) &&
                            PawnAndCardSelection.getCardNr() == i) {
                        drawRoundedRect(getCanvasCardsContext(), destX - 1.5, destY - 1.5, destWidth + 3, destHeight + 3, 8);
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