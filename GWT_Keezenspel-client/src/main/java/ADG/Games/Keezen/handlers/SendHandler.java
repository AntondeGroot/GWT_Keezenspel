package ADG.Games.Keezen.handlers;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.animations.StepsAnimation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SendHandler {
    private static final MovingServiceAsync movingService = GWT.create(MovingService.class);

    public static void sendMoveToServer(MoveMessage moveMessage) {
        GWT.log("Sending MoveMessage" + moveMessage);

        movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.resetStepsAnimation();
            }
            public void onSuccess(MoveResponse result) {
                StepsAnimation.resetStepsAnimation();
            }
        } );
    }
}