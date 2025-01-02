package gwtks.handlers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import gwtks.*;
import gwtks.animations.StepsAnimation;

import static gwtks.MessageType.MAKE_MOVE;

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
    public void sendMoveToServer() {
        // First, we validate the input.
//        errorLabel.setText("");

        MoveMessage moveMessage = new MoveMessage();
        moveMessage.setPlayerId(PawnAndCardSelection.getPlayerId());
        moveMessage.setPawnId1(PawnAndCardSelection.getPawnId1());
        moveMessage.setCard(PawnAndCardSelection.getCard());
        moveMessage.setMoveType(PawnAndCardSelection.getMoveType());
        moveMessage.setMessageType(MAKE_MOVE);
        moveMessage.setStepsPawn1(PawnAndCardSelection.getNrSteps());
        moveMessage.setPawnId2(PawnAndCardSelection.getPawnId2());

        GWT.log("... Sending MoveMessage" + moveMessage);

        movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.reset();
            }
            public void onSuccess(MoveResponse result) {
                StepsAnimation.reset();
                MoveController.movePawn(result);
                PawnAndCardSelection.reset();
            }
        } );
    }
}