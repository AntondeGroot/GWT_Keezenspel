package gwtks.handlers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import gwtks.*;

public class KingHandler implements ClickHandler {
    private final MovingServiceAsync movingService = GWT.create(MovingService.class);
    /**
     * Fired when the user clicks on the sendButton.
     */
    public void onClick(ClickEvent event) {
        sendOnboardMessageToServer();
        GWT.log("King Clicked");
    }

    /**
     * Send the MoveMessage to the server and wait for a response.
     */
    private void sendOnboardMessageToServer() {
        int playerId = Integer.parseInt(getPlayerIdFieldValue());
        GWT.log("playerid from handler is "+Integer.parseInt(getPlayerIdFieldValue()));
        GWT.log("value : "+playerId);

//        errorLabel.setText("");

        MoveMessage moveMessage = new MoveMessage();
        int pawnNr = Integer.parseInt(getPawnIdFieldValue());
        PawnId selectedPawnId = new PawnId(playerId, pawnNr);

        moveMessage.setPawnId1(selectedPawnId);
        moveMessage.setMoveType(MoveType.ONBOARD);
        moveMessage.setMessageType(MessageType.MAKE_MOVE);

        movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(MoveResponse result) {
                MoveController.movePawn(result);
            }
        });
    }

    public native String getPlayerIdFieldValue() /*-{
        return $doc.getElementById("playerId").value;
    }-*/;

    public native String getPawnIdFieldValue() /*-{
        return $doc.getElementById("pawnId").value;
    }-*/;
}
