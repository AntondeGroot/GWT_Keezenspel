package gwtks;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.List;

public class Pawn implements IsSerializable {

    public Image getImage(){

        String imagePath = "/pawn.png";
        Image image = new Image(imagePath);
        return image;
    }
    public void drawPawns(Canvas canvas){
        Context2d context = canvas.getContext2d();

        List<TileMapping> mappings = Board.getMappings();
        int i =0;
        for (TileMapping mapping : mappings) {
            if(mapping.getTileNr() < 0){

                drawPawn(context, mapping.getPosition(),i);
                i++;
            }
        }
    }

    private void drawPawn(Context2d context, Point point, int i){
        // Load an image and draw it to the canvas
        Image image = new Image("/pawn.png");
        image.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                int desiredWidth = 40;
                int desiredHeight = 40;
                // Draw the image on the canvas once it's loaded
                context.drawImage(ImageElement.as(image.getElement()), point.getX()-desiredWidth/2, point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight);
            }
        });

        // the image widget has to be added to the Rootpanel for it to trigger the OnLoad event
        image.setVisible(false); // Hide the image widget
        RootPanel.get().add(image);
    }
}
