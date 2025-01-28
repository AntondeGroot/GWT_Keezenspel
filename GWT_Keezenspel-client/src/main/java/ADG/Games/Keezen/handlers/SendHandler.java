package ADG.Games.Keezen.handlers;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.animations.StepsAnimation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SendHandler implements ClickHandler {
    private static final MovingServiceAsync movingService = GWT.create(MovingService.class);
    /**
     * Fired when the user clicks on the sendButton.
     */
    public void onClick(ClickEvent event) {
        sendMoveToServer(PawnAndCardSelection.createMoveMessage());
    }

    /**
     * Send the MoveMessage to the server and wait for a response.
     */
    public static void sendMoveToServer(MoveMessage moveMessage) {
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