package gwtks.handlers;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import gwtks.PawnAndCardSelection;

import static gwtks.MoveType.FORFEIT;

public class ForfeitHandler implements ClickHandler {

    public void sendMoveToServer(){
        Document document  = Document.get();
        InputElement moveType = (InputElement) document.getElementById("moveType");
        moveType.setValue("FORFEIT");
        PawnAndCardSelection.setMoveType(FORFEIT);

        SendHandler sendHandler = new SendHandler();
        sendHandler.sendMoveToServer();
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
        sendMoveToServer();

    }
}
