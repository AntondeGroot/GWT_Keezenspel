package ADG.Games.Keezen.handlers;

import ADG.Games.Keezen.MoveController;
import ADG.Games.Keezen.PawnAndCardSelection;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ADG.Games.Keezen.*;
import ADG.Games.Keezen.animations.StepsAnimation;

import static ADG.Games.Keezen.MessageType.MAKE_MOVE;

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
        moveMessage.setStepsPawn1(PawnAndCardSelection.getNrStepsPawn1());
        if(PawnAndCardSelection.getMoveType()==MoveType.SPLIT){
            moveMessage.setStepsPawn2(PawnAndCardSelection.getNrStepsPawn2());
        }
        moveMessage.setPawnId2(PawnAndCardSelection.getPawnId2());

        GWT.log("Sending MoveMessage" + moveMessage);

        movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.resetStepsAnimation();
            }
            public void onSuccess(MoveResponse result) {
                StepsAnimation.resetStepsAnimation();
                MoveController.movePawn(result);
                PawnAndCardSelection.reset();
            }
        } );
    }
}