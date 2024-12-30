package gwtks.handlers;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import gwtks.PawnAndCardSelection;

import static gwtks.MoveType.FORFEIT;

public class ForfeitHandler implements ClickHandler {
    // todo: is this all still useful?
    public void sendMoveToServer(){
        PawnAndCardSelection.setMoveType(FORFEIT);
        new SendHandler().sendMoveToServer();
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
        sendMoveToServer();
    }
}
