package ADG.handlers;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import ADG.PawnAndCardSelection;

import static ADG.MoveType.FORFEIT;

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
