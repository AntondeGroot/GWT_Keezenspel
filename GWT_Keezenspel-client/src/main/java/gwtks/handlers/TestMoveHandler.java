package gwtks.handlers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import gwtks.*;
import gwtks.animations.StepsAnimation;

import java.util.ArrayList;
import java.util.List;

public class TestMoveHandler implements ClickHandler {
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
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        moveMessage.setTileId(pawn1.getCurrentTileId());
        moveMessage.setStepsPawn1(Integer.parseInt(getStepsNrFieldValue()));

        movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.reset();
            }
            public void onSuccess(MoveResponse result) {
                GWT.log("Test Move successful: "+result.toString());
                List<TileId> tileIds = new ArrayList<TileId>();
                if(result.getMovePawn1() != null){
                    tileIds.add(result.getMovePawn1().getLast());
                    StepsAnimation.update(tileIds);
                }else{
                    StepsAnimation.reset();
                }
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