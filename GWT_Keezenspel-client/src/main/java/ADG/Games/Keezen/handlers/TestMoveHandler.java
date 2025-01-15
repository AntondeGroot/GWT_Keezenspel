package ADG.Games.Keezen.handlers;

import ADG.Games.Keezen.PawnAndCardSelection;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ADG.Games.Keezen.*;
import ADG.Games.Keezen.animations.StepsAnimation;

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
        moveMessage.setStepsPawn1(PawnAndCardSelection.getNrSteps());
        moveMessage.setPawnId2(PawnAndCardSelection.getPawnId2());

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
}