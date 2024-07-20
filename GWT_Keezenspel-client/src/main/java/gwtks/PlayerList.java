package gwtks;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;

import static java.awt.SystemColor.window;

public class PlayerList {

    public void createListElement(){
        String[] playerNames = {"Player1", "Player2", "Player3", "Player4", "Player5", "Player6", "Player7", "Player8"};
        boolean isPlayerActive = true;
        String inactiveColor = "#c2bfb6";
        int colCount = 2;
        int rowCount = (int) Math.ceil((double) playerNames.length / colCount);
        Grid grid = new Grid(rowCount, colCount);

        for (int i = 0; i < playerNames.length; i++) {

            int imagePixelSize = 50;

            ImageElement img = Document.get().createImageElement();
            img.setSrc("/profilepics.png");

            HorizontalPanel hp = new HorizontalPanel();
            Label playerName = new Label(playerNames[i]);
            playerName.getElement().getStyle().setMarginRight(30, Style.Unit.PX);
            playerName.getElement().getStyle().setMarginLeft(10, Style.Unit.PX);
            if(!isPlayerActive) {
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
            if(isPlayerActive){
                canvas.getElement().getStyle().setBorderColor(PlayerColors.getHexColor(i));
            }else{
                canvas.getElement().getStyle().setBorderColor(inactiveColor);
            }
            Context2d ctx = canvas.getContext2d();
            // source image
            double sw = 1024/4.0;
            double sh = 1024/4.0;
            double sx = sw*(i%4);
            double sy = sh*(i > 3 ? 1:0);
            // destination
            double dy = 0-5/2;
            double dx = 0-5/2;
            double dw = imagePixelSize+5;
            double dh = imagePixelSize+5;
            GWT.log("source image is "+sx+","+sh+", "+i);
            GWT.log("card size = "+dh+","+dw);
            // for spritesheets dx dy
            ctx.drawImage(img, sx,sy,sw,sh,dx,dy,dw,dh);
            canvas.asWidget().setStyleName("profilepic");
            hp.add(canvas.asWidget());
            hp.add(playerName);

            int row = i % 4;
            int col = (i > 3) ? 1 : 0;
            grid.setWidget(row, col, hp);

        }
        grid.getElement().getStyle().setMargin(50, Style.Unit.PX);
        RootPanel.get("playerListContainer").add(grid);

    }
}