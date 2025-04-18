package ADG.Games.Keezen.ViewHelpers;

import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.Player;
import ADG.Games.Keezen.Player.PlayerColors;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ADG.Games.Keezen.Player.PlayerColors.*;

public class ViewDrawing {

    public static DivElement createPawn(Pawn pawn){
        // Create new <div> Element
        DivElement pawnElement = Document.get().createDivElement();
        // set Class and Id
        pawnElement.setClassName("pawnDiv");
        pawnElement.setId(pawn.getPawnId().toString());

        // set image
        pawnElement.getStyle().setProperty("backgroundImage", "url(pawn"+pawn.getColorInt()+".png)");
        pawnElement.getStyle().setProperty("backgroundSize", "contain");     // Scale image to fit div
        pawnElement.getStyle().setProperty("backgroundRepeat", "no-repeat"); // Prevent tiling
        pawnElement.getStyle().setProperty("backgroundPosition", "center");  // Center image

        // set overlay for when you want to select the pawn
        ImageElement overlayImage = Document.get().createImageElement();
        overlayImage.setSrc("/pawn_outline.png");
        overlayImage.setClassName(pawn.getPawnId()+"Overlay");
        overlayImage.getStyle().setPosition(Style.Position.ABSOLUTE);
        overlayImage.getStyle().setTop(0, Style.Unit.PX);
        overlayImage.getStyle().setLeft(0, Style.Unit.PX);
        overlayImage.getStyle().setWidth(100, Style.Unit.PCT);
        overlayImage.getStyle().setHeight(100, Style.Unit.PCT);
        overlayImage.getStyle().setVisibility(Style.Visibility.HIDDEN);

        // set position
        pawnElement.getStyle().setPosition(Style.Position.ABSOLUTE);
        pawnElement.appendChild(overlayImage);

        // set width
        pawnElement.getStyle().setHeight(40, Style.Unit.PX);
        pawnElement.getStyle().setWidth(40, Style.Unit.PX);

        Event.sinkEvents(pawnElement, Event.ONCLICK);
        Event.setEventListener(pawnElement, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (DOM.eventGetType(event) == Event.ONCLICK) {
                    Window.alert("Clicked pawn: " + pawn.getPawnId());
                }
            }
        });

        return pawnElement;
    }

    public static DivElement createCircle(double x, double y, double radius, String color){
        // Create a new <div> element
        DivElement circle = Document.get().createDivElement();

        // Add the 'circle' class to the element
        circle.setClassName("circle");

        // Set the position using absolute coordinates relative to the container
        circle.getStyle().setPosition(Style.Position.ABSOLUTE);
        circle.getStyle().setLeft(x, Style.Unit.PX);
        circle.getStyle().setTop(y, Style.Unit.PX);

        // Set the size of the circle dynamically
        //todo: by making the circle a little smaller '-3' the indicator for possible moves is no longer exactly aligned, this can be seen for larger values e.g. -5
        circle.getStyle().setWidth(radius*2-3, Style.Unit.PX);
        circle.getStyle().setHeight(radius*2-3, Style.Unit.PX);

        // Ensure it remains a circle by setting border-radius to 50%
        String darkColor = rgbToHex(darkenColor(hexToRgb(color)));
        String lightColor = rgbToHex(lightenColor(hexToRgb(color)));
        circle.getStyle().setProperty("backgroundColor", color);
        circle.getStyle().setProperty("boxShadow", "inset 3px 3px 4px "+darkColor+"," +
                "  inset -4px -4px 4px "+lightColor);

        return circle;
    }

    public static void drawTransparentCircle(Context2d context, double x, double y, double radius, double alpha) {
        //todo: maybe change this to a div, and animate it with CSS
        context.beginPath();
        String fillColor = "rgba(255, 165, 0, " + alpha + ")";
        String fillColorStroke = "rgba(0, 0, 0, " + alpha/2 + ")";
        if(alpha >= 0){
            context.arc(x, y, radius*2*alpha, 0, 2 * Math.PI);
        }
        context.setFillStyle(fillColor);
        context.fill();
        context.setStrokeStyle(fillColorStroke);
        context.stroke();
        context.closePath();
    }

    public static Grid createPlayerGrid(ArrayList<Player> players){
        // todo: maybe check player whether they belong in column 1 or 2, so don't expand both when a winner has been declared
        List<Integer> column1 = Arrays.asList(0, 1, 2, 3);
        List<Integer> column2 = Arrays.asList(4, 5, 6, 7);
        int colCount = 2;
        // for a maximum of 8 players, there are either 4 rows and 2 columns
        // or there are between 1 and 4 rows
        int rowCount = Math.min(players.size(), 4);
        Grid grid = new Grid(rowCount, colCount);

        List<Player> winners = players.stream()
                .filter(player -> player.getPlace() > -1)
                .collect(Collectors.toList());

        GWT.log("winners: " + winners);
        GWT.log("players: " + players);
        int playerId = 0;
        for (Player player : players) {
            int imagePixelSize = 50;

            ImageElement img = Document.get().createImageElement();
            img.setSrc("/profilepics.png");
            HorizontalPanel hp = new HorizontalPanel();
            Label playerName = new Label(player.getName());

            if(player.isActive()){
                playerName.getElement().addClassName("activePlayerName");
            }else{
                playerName.getElement().addClassName("inactivePlayerName");
            }

            hp.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
            hp.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

            Canvas canvas = Canvas.createIfSupported();
            canvas.setWidth(imagePixelSize + "px");
            canvas.setHeight(imagePixelSize + "px");
            canvas.setCoordinateSpaceWidth(imagePixelSize);
            canvas.setCoordinateSpaceHeight(imagePixelSize);
            canvas.setStyleName("profilepic");

            if(player.isActive()){
                canvas.getElement().getStyle().setBorderColor(PlayerColors.getHexColor(playerId));
            }else{
//                canvas.getElement().addClassName("inactivePlayer"); //todo: this did not work
                canvas.getElement().getStyle().setBorderColor("#c2bfb6");
            }

            Context2d ctx = canvas.getContext2d();
            // source image
            //todo: use img.width or something instead
            // move this to the server, users should have chosen their profile pic
            double sw = 1024/4.0;
            double sh = 1024/4.0;
            double sx = sw*(playerId%4);
            double sy = sh*(playerId > 3 ? 1:0);
            // destination
            double dy = -5/2.0;
            double dx = -5/2.0;
            double dw = imagePixelSize+5;
            double dh = imagePixelSize+5;

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
            int col = playerId > 3 ? 1 : 0;
            grid.setWidget(row, col, hp);

            playerId++;
        }
        grid.getElement().getStyle().setMargin(50, Style.Unit.PX);
        return grid;
    }
}
