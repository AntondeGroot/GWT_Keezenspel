package ADG.Games.Keezen.handlers;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.animations.StepsAnimation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SendHandler {
    private static final MovingServiceAsync movingService = GWT.create(MovingService.class);

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
                //PawnAndCardSelection.reset();
                // todo: uncomment or remove
                // it used to reset after each player played his move, however when only 1 player is left you might want to keep playing
                // but after playing a jack it will still show the opponents pawn until you select a different card
            }
        } );
    }
}