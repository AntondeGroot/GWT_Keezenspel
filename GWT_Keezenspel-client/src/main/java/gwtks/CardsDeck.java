package gwtks;

import com.google.gwt.canvas.dom.client.Context2d;
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

    public static List<Card> getCards(){
        return cards;
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
