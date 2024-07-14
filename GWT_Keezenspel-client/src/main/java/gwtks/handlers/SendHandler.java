package gwtks.handlers;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import gwtks.*;

public class SendHandler implements ClickHandler {
    private final MovingServiceAsync movingService = GWT.create(MovingService.class);
    /**
     * Fired when the user clicks on the sendButton.
     */
    public void onClick(ClickEvent event) {
        sendMoveToServer();
    }

    /**
     * Send the MoveMessage to the server and wait for a response.
     */
    private void sendMoveToServer() {
        // First, we validate the input.
//        errorLabel.setText("");

        MoveMessage moveMessage = new MoveMessage();
        int playerId = Integer.parseInt(getPlayerIdFieldValue());
        int pawnNr = Integer.parseInt(getPawnIdFieldValue());
        PawnId selectedPawnId = new PawnId(playerId,pawnNr);
        Pawn pawn1 = Board.getPawn(selectedPawnId);

        moveMessage.setPawnId1(selectedPawnId);
        moveMessage.setMoveType(MoveType.MOVE);
        moveMessage.setTileId(pawn1.getCurrentTileId());
        moveMessage.setStepsPawn1(Integer.parseInt(getStepsNrFieldValue()));

        movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
            public void onFailure(Throwable caught) {}
            public void onSuccess(MoveResponse result) {
                MoveController.movePawn( result);
            }
        } );
    }

    public native String getPlayerIdFieldValue() /*-{
        return $doc.getElementById("playerId").value;
    }-*/;

    public native String getPawnIdFieldValue() /*-{
        return $doc.getElementById("pawnId").value;
    }-*/;

    public native String getStepsNrFieldValue() /*-{
        return $doc.getElementById("stepsNr").value;
    }-*/;
}