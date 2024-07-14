package gwtks;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;

import java.util.ArrayList;
import java.util.List;

public class CardsDeck {

    private static Context2d ctxCards;
    private static List<Card> cards= new ArrayList<>();

    public static void drawCards(){
        ImageElement img = Document.get().createImageElement();
        img.setSrc("/card-deck.png");
        Document document  = Document.get();
        ctxCards = ((CanvasElement) document.getElementById("canvasCards")).getContext2d();

        cards.add(new Card(0,6));
        cards.add(new Card(1,12));
        cards.add(new Card(2,8));
        cards.add(new Card(3,1));
        cards.add(new Card(1,11));
        // card width 25 is good to show how many cards they are still holding
        // card width 100 is good for your own hand
        int i=0;
        for (Card card: cards){
            // source image
            double sw = 1920/13.0;
            double sh = 1150/5.0;
            double sx = sw*card.getCard();
            double sy = sh*card.getSuit();
            // destination
            int dy = 600;
            double dw = 100.0;
            double dx = (dw+10)* i;
            double dh = dw/sw*sh;
            i++;
            // for spritesheets dx dy
            ctxCards.drawImage(img, sx,sy,sw,sh,dx,dy,dw,dh);
        }
    }

    public static Card pickCard(int i){
        if(!cards.isEmpty() && i > -1 && i < cards.size()){
            return cards.get(i);
        }
        return null;
    }
}
