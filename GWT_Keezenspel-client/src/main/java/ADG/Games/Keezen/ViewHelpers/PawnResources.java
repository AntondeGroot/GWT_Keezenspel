package ADG.Games.Keezen.ViewHelpers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface PawnResources extends ClientBundle {

  PawnResources INSTANCE = GWT.create(PawnResources.class);

  @Source("pawn.svg")
  TextResource pawnSvg();
}
