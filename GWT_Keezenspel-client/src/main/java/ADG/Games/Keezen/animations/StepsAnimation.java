package ADG.Games.Keezen.animations;

import ADG.Games.Keezen.TileId;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import java.util.ArrayList;
import java.util.List;

public class StepsAnimation {

  private static ArrayList<String> ids = new ArrayList<>();
  private static final String CLASS_ANIMATION = "tile-highlight-pulse";

  public static void updateStepsAnimation(List<TileId> tileIds) {
    resetStepsAnimation();

    for (TileId tileId : tileIds) {
      String id = tileId + "Highlight";
      ids.add(id);
      if(Document.get().getElementById(id) == null){
        GWT.log("StepsAnimation: No tile found with id :" + id);
      }
      Document.get().getElementById(id).addClassName(CLASS_ANIMATION);
    }
  }

  public static void resetStepsAnimation() {
    for (String id : ids) {
      Document.get().getElementById(id).removeClassName(CLASS_ANIMATION);
    }
    ids.clear();
  }
}
