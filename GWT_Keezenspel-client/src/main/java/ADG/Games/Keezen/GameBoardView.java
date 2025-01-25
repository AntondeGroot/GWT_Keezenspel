package ADG.Games.Keezen;

import ADG.Games.Keezen.util.PawnRect;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.*;

import static ADG.Games.Keezen.ViewHelpers.ViewDrawing.createCircle;
import static ADG.Games.Keezen.ViewHelpers.ViewDrawing.createPlayerGrid;
import static ADG.Games.Keezen.util.PlayerUUIDUtil.UUIDtoInt;

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
    VerticalPanel playerListContainer2;

    //    needed to add click listeners to the Canvas. GWT Canvas does not have
    //    a dom element which you could find by .findElementById()
    @UiField
    HTMLPanel canvasWrapper;

    @UiField
    HTMLPanel pawnBoard;

    @UiField
    HorizontalPanel pawnIntegerBoxes;

    @UiField
    TextBox stepsPawn1;

    @UiField
    TextBox stepsPawn2;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public GameBoardView() {
        initWidget(uiBinder.createAndBindUi(this));
        document = Document.get();
        stepsPawn1.setValue("7");
        stepsPawn2.setValue("0");
    }

    public int getStepsPawn1() {
        if(stepsPawn1.getValue().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(stepsPawn1.getValue());
    }

    public int getStepsPawn2() {
        if(stepsPawn2.getValue().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(stepsPawn2.getValue());
    }

    public void showPawnTextBoxes(boolean show){
        pawnIntegerBoxes.setVisible(show);
    }

    public Button getSendButton(){
        return sendButton;
    }

    public Button getForfeitButton(){
        return forfeitButton;
    }

    public VerticalPanel getPlayerListContainer(){return playerListContainer2;}

    public Context2d getCanvasBoardContext(){return
            ((CanvasElement) document.getElementById("canvasBoard2")).getContext2d();}

    public Context2d getCanvasPawnsContext(){return ((CanvasElement) document.getElementById("canvasPawns2")).getContext2d();}

    public Context2d getCanvasStepsContext(){return ((CanvasElement) document.getElementById("canvasSteps2")).getContext2d();}

    public Context2d getCanvasCardsContext(){return ((CanvasElement) document.getElementById("canvasCards2")).getContext2d();}

    // todo: rename to canvasCards when old index.html is no longer used
    public CanvasElement getCanvasCards(){
        return (CanvasElement) document.getElementById("canvasCards2");
    }

    public CanvasElement getCanvasSteps(){
        return (CanvasElement) document.getElementById("canvasSteps2");
    }

    public CanvasElement getCanvasPawns(){
        return (CanvasElement) document.getElementById("canvasPawns2");
    }

    public void drawPlayers(ArrayList<Player> players){
        playerListContainer2.clear();
        playerListContainer2.add(createPlayerGrid(players));
    }

    public void drawPawns(ArrayList<Pawn> pawns){
        // sort the pawns vertically so that they don't overlap weirdly when drawn
        Context2d context = getCanvasPawnsContext();
        pawns.sort(new PawnComparator());
        for(Pawn pawn : pawns){
            if (shouldBeAnimated(pawn)) {
                Iterator<PawnAnimationMapping> iterator = AnimationModel.animationMappings.iterator();
                while (iterator.hasNext()) {
                    PawnAnimationMapping animation_Pawn_i = iterator.next();
                    // only animate the killing of a pawn after all other moves of other pawns were animated
                    if(!animation_Pawn_i.isAnimateLast()) {
                        if (pawn.equals(animation_Pawn_i.getPawn())) {
                            if (animation_Pawn_i.getPoints().isEmpty()) {
                                iterator.remove(); // Remove the current element safely
                            } else {
                                LinkedList<Point> points = animation_Pawn_i.getPoints();
                                if (!points.isEmpty()) {
                                    Point p = points.getFirst();
                                    drawPawnAnimated(context, pawn, p);
                                    GWT.log("draw animated : "+ pawn);
                                    points.removeFirst(); // Remove the first element safely
                                }
                            }
                        }
                    }else{
                        GWT.log("draw statically : "+ animation_Pawn_i.getPawn());
                        // draw the pawn that is about to be killed statically
                        drawPawnAnimated(context, animation_Pawn_i.getPawn(), animation_Pawn_i.getPoints().getFirst());
                        // if no other pawns to be drawn, start drawing this one.
                        if (AnimationModel.onlyPawnsToBeKilledAreLeft() && animation_Pawn_i.isAnimateLast()){
                            animation_Pawn_i.setAnimateLast(false);
                        }
                    }
                }
            }else{
                drawPawn(context, pawn);
            }
        }
    }

    private boolean pawnWasSelected(Pawn pawn){
//        return PawnAndCardSelection.getPawn1().equals(pawn) || PawnAndCardSelection.getPawn2().equals(pawn);
        return false;
    }

    public void drawBoard(List<TileMapping> tiles, ArrayList<Player> players, double cellDistance) {
        GWT.log("drawing board");

        for (TileMapping mapping : tiles) {
            String color = "#f2f2f2";
            int tileNr = mapping.getTileNr();
            // only player tiles get a color
            if (tileNr <= 0 || tileNr >= 16) {
                color = PlayerColors.getHexColor(UUIDtoInt(mapping.getPlayerId(), players));
            }
            drawCircle(mapping.getPosition().getX()-cellDistance/2, mapping.getPosition().getY()-cellDistance/2, cellDistance/2, color);
        }
    }

    public void enableButtons(Boolean enabled){
        sendButton.setEnabled(enabled);
        forfeitButton.setEnabled(enabled);
    }

    private void drawCircle(double x, double y, double radius, String color) {
        DivElement circle = createCircle(x, y, radius, color);
        pawnBoard.getElement().appendChild(circle);
    }

    public void drawCardsIcons(HashMap<String, Integer> nrCardsPerPlayerUUID, Image spriteImage){

        for (Map.Entry<String, Integer> entry : nrCardsPerPlayerUUID.entrySet()) {
            String uuid = entry.getKey();
            if(uuid.equals(Cookie.getPlayerId())){
                // skip drawing card icons for the current player, their hand is already drawn showing the cards
                // so you don't need to draw the back side to indicate how many cards they currently have
                continue;
            }
            ArrayList<Point> cardpoints = Board.getCardsDeckPointsForPlayer(uuid);
            Point startPoint = cardpoints.get(0);
            Point endPoint = cardpoints.get(1);
            double dx = (endPoint.getX() - startPoint.getX()) / 4;
            double dy = (endPoint.getY() - startPoint.getY()) / 4;
            GWT.log("drawCardsIcons \n"+
                    "PlayerUUID "+uuid+"\n"+
                    "Start "+startPoint +"\n"+
                    "End "+endPoint+"\n"+
                    "dx "+dx+"\n"+
                    "dy "+dy
            );

            Integer nrCards = entry.getValue();
            for (int i = 0; i < nrCards; i++) {
                // Define the source rectangle (from the sprite sheet): this image belongs to the backside image
                double spriteWidth = 1920 / 13.0;
                double spriteHeight = 1150 / 5.0;
                double sourceX = spriteWidth * (2);
                double sourceY = spriteHeight * 4;

                // Define the destination rectangle (on the canvas)
                double imageWidth = 20;
                double destWidth = imageWidth; // Card width on canvas
                double destHeight = destWidth / spriteWidth * spriteHeight; // Maintain aspect ratio
                double destX = startPoint.getX() - destWidth/2 +dx*i; // Offset for each card
                double destY = startPoint.getY() - destHeight/2 +dy*i;

                // Draw the card image on the canvas
                getCanvasCardsContext().drawImage(
                        ImageElement.as(spriteImage.getElement()),
                        sourceX, sourceY, spriteWidth, spriteHeight,
                        destX, destY, destWidth, destHeight
                );
            }
        }
    }

    private void drawPlayerCardsInHand(List<Card> cards, Image spriteImage){
        // Loop through the cards to draw them
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);

            // Define the source rectangle (from the sprite sheet)
            double spriteWidth = 1920 / 13.0;
            double spriteHeight = 1150 / 5.0;
            double sourceX = spriteWidth * (card.getCardValue()-1);
            double sourceY = spriteHeight * card.getSuit();

            // Define the destination rectangle (on the canvas)
            double destX = 10 + (100.0 + 10) * i; // Offset for each card
            double destY = 600; // Fixed Y position
            double destWidth = 100.0; // Card width on canvas
            double destHeight = destWidth / spriteWidth * spriteHeight; // Maintain aspect ratio

            // Draw the card image on the canvas
            getCanvasCardsContext().drawImage(
                    ImageElement.as(spriteImage.getElement()),
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

    public void drawCards(List<Card> cards, HashMap<String, Integer> nrCardsPerPlayerUUID, ArrayList<Card> playedCards) {
        // Create an image to represent the card deck
        Image img = new Image("/card-deck.png");

        // Add a LoadHandler to ensure the image is fully loaded before drawing
        img.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                // Clear the canvas to prepare for drawing new cards
                getCanvasCardsContext().clearRect(0, 0, getCanvasCards().getWidth(), getCanvasCards().getHeight());

                GWT.log("\n\ndrawing cards");
                drawPlayerCardsInHand(cards, img);
                drawCardsIcons(nrCardsPerPlayerUUID, img);
                drawPlayedCards(playedCards, img);
            }
        });

        // Trigger the image loading by adding it to the DOM
        removeCardDeckImage();
        img.setVisible(false);
        RootPanel.get().add(img);
    }

    private void drawPlayedCards(ArrayList<Card> playedCards, Image spriteImage) {
        // Loop through the cards to draw them
        // the cards are drawn rotating with an angle of 45 degrees
        // meaning that after 8 cards you will draw a card over a previous drawn card
        // In order to not do any unnecessary drawing, we will skip the first N cards
        // if more than 8 cards were drawn.
        int angleDegrees = 45;
        int startDrawingFromCardIndex = 0;
        if(playedCards.size() > 8){
            startDrawingFromCardIndex = playedCards.size() - 8;
        }

        for (int i = 0; i < playedCards.size(); i++) {
            angleDegrees = angleDegrees + 45;

            if(i >= startDrawingFromCardIndex) {
                Card card = playedCards.get(i);
                double angleRadians = Math.toRadians(angleDegrees);

                // Define the source rectangle (from the sprite sheet)
                double spriteWidth = 1920 / 13.0;
                double spriteHeight = 1150 / 5.0;
                double sourceX = spriteWidth * (card.getCardValue() - 1);
                double sourceY = spriteHeight * card.getSuit();

                // Define the destination rectangle (on the canvas)
                double destWidth = 60.0; // Card width on canvas
                double destHeight = destWidth / spriteWidth * spriteHeight; // Maintain aspect ratio
                double destX = 300 - destWidth / 4 + 10 * Math.cos(angleRadians); // rotate for each card 45 degrees
                double destY = 300 - destHeight / 2 + 10 * Math.sin(angleRadians);

                // Draw the card image on the canvas
                getCanvasCardsContext().drawImage(
                        ImageElement.as(spriteImage.getElement()),
                        sourceX, sourceY, spriteWidth, spriteHeight,
                        destX, destY, destWidth, destHeight
                );
            }
        }
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

    public void removeCardDeckImage() {
        // todo: can this be done any other way? how was it done for the pawns? There it didn't need a loadHandler
        // Get the parent element (e.g., RootPanel)
        Element parentElement = RootPanel.get().getElement();

        // Iterate through child elements to find the matching <img>
        NodeList<Element> imgElements = parentElement.getElementsByTagName("img");
        for (int i = 0; i < imgElements.getLength(); i++) {
            Element imgElement = imgElements.getItem(i);

            // Check if the <img> has the desired `src` attribute
            if ("/card-deck.png".equals(imgElement.getAttribute("src"))) {
                imgElement.removeFromParent(); // Remove the element from the DOM
                GWT.log("Image removed successfully.");
                return;
            }
        }

        GWT.log("Image not found in DOM.");
    }

    public void clearCanvasSteps(){
        getCanvasStepsContext().clearRect(0,0, getCanvasSteps().getWidth(), getCanvasSteps().getHeight());
    }
    public void clearCanvasCards(){
        getCanvasCardsContext().clearRect(0,0,getCanvasCards().getWidth(),getCanvasCards().getWidth());
    }
    public void clearCanvasPawns(){
        getCanvasPawnsContext().clearRect(0, 0, getCanvasPawns().getWidth(), getCanvasPawns().getHeight());
    }
//todo: move to util
    private void drawPawnAnimated(Context2d context, Pawn pawn, Point point){
        // Load an image and draw it to the canvas
        String playerId = pawn.getPlayerId();
        int colorInt = pawn.getColorInt();
        Image image = new Image("/pawn"+colorInt+".png");

        double[] xywh = PawnRect.getRect(point);
        context.drawImage(ImageElement.as(image.getElement()), xywh[0], xywh[1], xywh[2], xywh[3] );
    }

    //todo: move to util
    //todo: do not draw the pawns too often
    private void drawPawn(Context2d context, Pawn pawn){
        // Load an image and draw it to the canvas
        int colorInt = pawn.getColorInt();
        Image image = new Image("/pawn"+colorInt+".png");
        Image image_outline = new Image("/pawn_outline.png");

        double desiredWidth = 40;
        double desiredHeight = 40;
        Point point = new Point(0,0);
        // Draw the image on the canvas once it's loaded

        for (TileMapping mapping : Board.getTiles()) {
            if(mapping.getTileId().equals(pawn.getCurrentTileId())){
                point = mapping.getPosition();
            }
        }
        context.drawImage(ImageElement.as(image.getElement()), point.getX()-desiredWidth/2, point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight);
        if(PawnAndCardSelection.getPawn1().equals(pawn) || PawnAndCardSelection.getPawn2().equals(pawn)){
            context.drawImage(ImageElement.as(image_outline.getElement()), point.getX()-desiredWidth/2, point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight);
        }
    }

    public boolean shouldBeAnimated(Pawn pawn) {
        if(AnimationModel.animationMappings.isEmpty()){
            return false;
        }

        for (PawnAnimationMapping animationMappings1 : AnimationModel.animationMappings) {
            if (pawn.equals(animationMappings1.getPawn())) {
                return true;
            }
        }
        return false;
    }

}