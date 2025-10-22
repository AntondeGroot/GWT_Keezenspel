package ADG.Games.Keezen.moving;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Move.MovingService;
import ADG.Games.Keezen.Move.MovingServiceAsync;
import ADG.Games.Keezen.animations.StepsAnimation;
import ADG.Games.Keezen.util.Cookie;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

public class Move {

  private static final MovingServiceAsync movingService = GWT.create(MovingService.class);

  public static void testMove(MoveMessage moveMessage) {
    GWT.log(moveMessage.toString());

    movingService.makeMove(Cookie.getSessionID(), moveMessage, new AsyncCallback<MoveResponse>() {
      public void onFailure(Throwable caught) {
        StepsAnimation.resetStepsAnimation();
      }

      public void onSuccess(MoveResponse result) {
        GWT.log("Test Move: " + result.toString());
        List<TileId> tileIds = new ArrayList<>();
        if (result.getMovePawn1() != null) {
          tileIds.add(result.getMovePawn1().getLast());
          if (result.getMovePawn2() != null) {
            tileIds.add(result.getMovePawn2().getLast());
          }
          StepsAnimation.updateStepsAnimation(tileIds);
        } else {
          StepsAnimation.resetStepsAnimation();
        }
      }
    });
  }

  public static void makeMove(MoveMessage moveMessage) {
    GWT.log("Sending MoveMessage" + moveMessage);

    movingService.makeMove(Cookie.getSessionID(), moveMessage, new AsyncCallback<MoveResponse>() {
      public void onFailure(Throwable caught) {
        StepsAnimation.resetStepsAnimation();
      }

      public void onSuccess(MoveResponse result) {
        StepsAnimation.resetStepsAnimation();
      }
    });
  }
}