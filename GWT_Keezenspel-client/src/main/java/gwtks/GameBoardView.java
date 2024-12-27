package gwtks;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import gwtks.util.PawnRect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class GameBoardView extends Composite {

    interface Binder extends UiBinder<Widget, GameBoardView> {}
    private static Binder uiBinder = GWT.create(Binder.class);
    private ArrayList<TileMapping> tiles = new ArrayList<>(); // todo: set tiles when view is constructed
    private double cellDistance;
    private static ArrayList<PawnAnimationMapping> animationMappings = new ArrayList<>();

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
    Grid playerListContainer;

    private Context2d ctxBoard;
    private Context2d ctxPawns;
    private Context2d ctxCards;

    @Override
    public void onLoad() {
        super.onLoad();
        // Get the Context2d from the CanvasElement
        ctxBoard = canvasBoard.getContext2d();
        ctxPawns = canvasPawns.getContext2d();
        ctxCards = canvasCards.getContext2d();
    }

    public GameBoardView(ArrayList<TileMapping> tiles, double celldistance) {
        initWidget(uiBinder.createAndBindUi(this));
        this.tiles = tiles;
        this.cellDistance = celldistance;
    }

    public Button getSendButton(){
        return sendButton;
    }

    public Button getForfeitButton(){
        return forfeitButton;
    }

    public void drawPawns(ArrayList<Pawn> pawns){
        // TODO: ONLY DRAW PAWNS WHEN IT IS NECESSARY
        pawns.sort(new PawnComparator());
        for(Pawn pawn : pawns){
            if (shouldBeAnimated(pawn)) {
                Iterator<PawnAnimationMapping> iterator = animationMappings.iterator();
                while (iterator.hasNext()) {
                    PawnAnimationMapping animationMappings1 = iterator.next();
                    // only animate the killing of a pawn after all other moves of other pawns were animated
                    if(!animationMappings1.isAnimateLast()) {
                        if (pawn.equals(animationMappings1.getPawn())) {
                            if (animationMappings1.getPoints().isEmpty()) {
                                iterator.remove(); // Remove the current element safely
                            } else {
                                LinkedList<Point> points = animationMappings1.getPoints();
                                if (!points.isEmpty()) {
                                    Point p = points.getFirst();
                                    drawPawnAnimated(pawn, p);
                                    points.removeFirst(); // Remove the first element safely
                                }
                            }
                        }
                    }else{
                        // draw the pawn that is about to be killed statically
                        drawPawnAnimated(animationMappings1.getPawn(), animationMappings1.getPoints().getFirst());
                        // if no other pawns to be drawn, start drawing this one.
                        if (animationMappings.size() == 1){
                            animationMappings1.setAnimateLast(false);
                        }
                    }
                }
            }else{
                drawPawn(pawn);
            }
        }
    }

    private void drawPawn(Pawn pawn){
        // Load an image and draw it to the canvas
        int playerId = pawn.getPlayerId();
        Image image = new Image("/pawn"+playerId+".png");
        Image image_outline = new Image("/pawn_outline.png");

        int desiredWidth = 40;
        int desiredHeight = 40;
        Point point = new Point(0,0);
        // Draw the image on the canvas once it's loaded

        for (TileMapping mapping : tiles) {
            if(mapping.getTileId().equals(pawn.getCurrentTileId())){
                point = mapping.getPosition();
            }
        }
        // draw  Pawn
        ctxPawns.drawImage(ImageElement.as(image.getElement()), point.getX()-desiredWidth/2, point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight);
        if(pawnWasSelected(pawn)){
            // draw outline
            ctxPawns.drawImage(ImageElement.as(image_outline.getElement()), point.getX()-desiredWidth/2, point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight);
        }
    }

    private boolean pawnWasSelected(Pawn pawn){
        return PawnAndCardSelection.getPawn1().equals(pawn) || PawnAndCardSelection.getPawn2().equals(pawn);
    }

    public void drawBoard() {
        GWT.log("drawing board");
        double cellSize = 40.0;
        String color = "";
        int tileNr = 0;

        for (TileMapping mapping : tiles) {
            color = "#D3D3D3";
            tileNr = mapping.getTileNr();
            // only player tiles get a color
            if (tileNr <= 0 || tileNr >= 16) {
                color = PlayerColors.getHexColor(mapping.getPlayerId());
            }
            GWT.log("drawing tile " + mapping.getPlayerId() + " " + tileNr + ", "+mapping.getPosition());
            drawCircle(mapping.getPosition().getX(), mapping.getPosition().getY(), cellDistance/2, color);
        }
        // todo: was this ever useful/necessary?
        //context.save();
    }

    private void drawCircle(double x, double y, double radius, String color) {
        Context2d context = ctxBoard;
        context.beginPath();
        context.arc(x, y, radius, 0, 2 * Math.PI);
        context.setFillStyle(color);
        context.fill();
        context.setStrokeStyle("#000000");
        context.stroke();
        context.closePath();
    }

    private void drawPawnAnimated(Pawn pawn, Point point){
        // Load an image and draw it to the canvas
        int playerId = pawn.getPlayerId();
        Image image = new Image("/pawn"+playerId+".png");

        double[] xywh = PawnRect.getRect(point);
        ctxPawns.drawImage(ImageElement.as(image.getElement()), xywh[0], xywh[1], xywh[2], xywh[3] );
    }

    public boolean shouldBeAnimated(Pawn pawn) {
        if(animationMappings.isEmpty()){
            return false;
        }

        for (PawnAnimationMapping animationMappings1 : animationMappings) {
            if (pawn.equals(animationMappings1.getPawn())) {
                return true;
            }
        }
        return false;
    }

    public static void movePawn(Pawn pawn, LinkedList<TileId> movePawn, boolean animateLast) {
        //todo implement
//        animationMappings.add(new PawnAnimationMapping(pawn, movePawn, animateLast));
//        pawn1.setCurrentTileId(movePawn.getLast());
    }
}
