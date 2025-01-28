package ADG.Games.Keezen.handlers;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.animations.StepsAnimation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
    public void sendMoveToServer() {
        MoveMessage moveMessage = new MoveMessage();
        moveMessage.setPlayerId(PawnAndCardSelection.getPlayerId());
        moveMessage.setMoveType(PawnAndCardSelection.getMoveType());
        moveMessage.setCard(PawnAndCardSelection.getCard());
        GWT.log("test card: "+PawnAndCardSelection.getCard());
        moveMessage.setPawnId1(PawnAndCardSelection.getPawnId1());
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        moveMessage.setStepsPawn1(PawnAndCardSelection.getNrStepsPawn1());
        if(PawnAndCardSelection.getMoveType()==MoveType.SPLIT){
            moveMessage.setStepsPawn2(PawnAndCardSelection.getNrStepsPawn2());
        }
        moveMessage.setPawnId2(PawnAndCardSelection.getPawnId2());

        GWT.log(moveMessage.toString());

        movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
            public void onFailure(Throwable caught) {
                StepsAnimation.resetStepsAnimation();
            }
            public void onSuccess(MoveResponse result) {
                GWT.log("Test Move: "+result.toString());
                List<TileId> tileIds = new ArrayList<TileId>();
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