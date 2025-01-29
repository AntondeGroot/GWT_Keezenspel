package ADG.Games.Keezen.handlers;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.animations.StepsAnimation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

public class TestMoveHandler{// todo: is no longer a handler
    private static final MovingServiceAsync movingService = GWT.create(MovingService.class);
    /**
     * Send the MoveMessage to the server and wait for a response.
     */
    public static void sendMoveToServer(MoveMessage moveMessage) {
        GWT.log(moveMessage.toString());

        movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.resetStepsAnimation();
            }
            public void onSuccess(MoveResponse result) {
                // todo: maybe place the following in the presenter
                GWT.log("Test Move: "+result.toString());
                List<TileId> tileIds = new ArrayList<>();
                if(result.getMovePawn1() != null){
                    if(result.getMoveType()==MoveType.SPLIT){
                        // draw only where a pawn ends up
                        tileIds.add(result.getMovePawn1().getLast());
                        tileIds.add(result.getMovePawn2().getLast());
                    }else{
                        // draw only where a pawn ends up
                        tileIds.add(result.getMovePawn1().getLast());
                    }
                    StepsAnimation.updateStepsAnimation(tileIds);
                }else{
                    StepsAnimation.resetStepsAnimation();
                }
            }
        } );
    }
}