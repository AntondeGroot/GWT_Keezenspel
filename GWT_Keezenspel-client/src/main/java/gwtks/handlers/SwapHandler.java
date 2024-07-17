package gwtks.handlers;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import gwtks.*;
import gwtks.animations.StepsAnimation;

public class SwapHandler implements ClickHandler {
    private final MovingServiceAsync movingService = GWT.create(MovingService.class);
    /**
     * Fired when the user clicks on the sendButton.
     */
    public void onClick(ClickEvent event) {
        sendOnboardMessageToServer();
    }

    /**
     * Send the MoveMessage to the server and wait for a response.
     */
    private void sendOnboardMessageToServer() {
        // First, we validate the input.
//        errorLabel.setText("");

        MoveMessage moveMessage = new MoveMessage();
        int playerId = Integer.parseInt(getPlayerIdFieldValue());
        int pawnNr = Integer.parseInt(getPawnIdFieldValue());
        PawnId selectedPawnId = new PawnId(playerId, pawnNr);
        int playerId2 = Integer.parseInt(getPlayerId2FieldValue());
        int pawnNr2 = Integer.parseInt(getPawnId2FieldValue());
        PawnId selectedPawnId2 = new PawnId(playerId2, pawnNr2);

        moveMessage.setPawnId1(selectedPawnId);
        moveMessage.setPawnId2(selectedPawnId2);
        moveMessage.setMoveType(MoveType.SWITCH);
        moveMessage.setMessageType(MessageType.MAKE_MOVE);

        movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.reset();
            }

            public void onSuccess(MoveResponse result) {
                StepsAnimation.reset();
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

    public native String getPlayerId2FieldValue() /*-{
        return $doc.getElementById("playerId2").value;
    }-*/;

    public native String getPawnId2FieldValue() /*-{
        return $doc.getElementById("pawnId2").value;
    }-*/;
}
