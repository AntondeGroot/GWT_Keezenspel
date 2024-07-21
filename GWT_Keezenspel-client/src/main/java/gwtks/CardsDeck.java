package gwtks;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;

import java.util.ArrayList;
import java.util.List;

public class CardsDeck {

    private static Context2d ctxCards;
    private static List<Card> cards = new ArrayList<>();

    public static boolean areCardsDifferent(List<Card> otherCards){
        return !cards.equals(otherCards);
    }

    public static void setCards(List<Card> cards){
        CardsDeck.cards = cards;
    }

    public static void drawCards(){
        ImageElement img = Document.get().createImageElement();
        img.setSrc("/card-deck.png");
        Document document  = Document.get();
        ctxCards = ((CanvasElement) document.getElementById("canvasCards")).getContext2d();
        ctxCards.clearRect(0,0,600,800);

        // card width 25 is good to show how many cards they are still holding
        // card width 100 is good for your own hand
        int i=0;
        for (Card card: cards){
            GWT.log("drawing card : "+i);
            // source image
            double sw = 1920/13.0;
            double sh = 1150/5.0;
            double sx = sw*card.getCard();
            double sy = sh*card.getSuit();
            // destination
            int dy = 600;
            double dw = 100.0;
            double dx = 10+(dw+10)* i;
            double dh = dw/sw*sh;
            GWT.log("card height = "+dh);
            i++;
            // for spritesheets dx dy
            ctxCards.drawImage(img, sx,sy,sw,sh,dx,dy,dw,dh);
            double lineThickness = 3;
            if(PawnAndCardSelection.getCard() != null && PawnAndCardSelection.getCard().equals(card)){
                drawRoundedRect(ctxCards, dx-lineThickness/2, dy-lineThickness/2, dw+lineThickness, dh+lineThickness, 8);
            }
        }
    }

    public static Card pickCard(int i){
        if(!cards.isEmpty() && i > -1 && i < cards.size()){
            return cards.get(i);
        }
        if(!cards.isEmpty() && i == -1){
            cards = null;
        }
        return null;
    }

    private static void drawRoundedRect(Context2d context, double x, double y, double width, double height, double radius) {
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
