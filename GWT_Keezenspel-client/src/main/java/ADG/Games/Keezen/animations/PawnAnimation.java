package ADG.Games.Keezen.animations;

import static java.lang.Math.max;

import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Point;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PawnAnimation {
  boolean isFirstSequence = true;
  double totalDelay;

  public void animateSequence(List<AnimatePawnPoints> sequence) {
    // * get the list of all pawns that should be animated first
    // * get one pawn
    // * determine the total length that pawn has to travel
    // * determine the speed it will need to travel?
    // * get the DivElement for that Pawn
    // * animate the pawn
    if(isFirstSequence){
      totalDelay = 0;
    }
    for (AnimatePawnPoints p : sequence) {
      GWT.log("Animating pawn path: " + p);

      Pawn pawn = p.getPawn();
      LinkedList<Point> points = p.getPoints();
      double distance = p.getTotalPathLength();

      double pixelsPerMs = calculateSpeed(distance);

      DivElement pawnElement = (DivElement) Document.get().getElementById(pawn.getPawnId().toString());

      animateStep(pawnElement, points.get(0), points, pixelsPerMs);

      totalDelay = max(totalDelay, distance/pixelsPerMs);
    }
    isFirstSequence = !isFirstSequence;
  }


  private double calculateSpeed(double distance) {
    double pixelsPerMs = 0.1;
    if(distance > 200){ pixelsPerMs = 0.12;};
    if(distance > 400){ pixelsPerMs = 0.16;};
    return pixelsPerMs;
  }

  private void animateStep(DivElement pawn, Point current, Queue<Point> remaining, double speedPixelsPerMs) {
    if (remaining.isEmpty()) return;

    Point next = remaining.poll();
    double dx = next.getX() - current.getX();
    double dy = next.getY() - current.getY();
    double distancePixels = Math.sqrt(dx * dx + dy * dy);

    int duration = (int)(distancePixels / speedPixelsPerMs);
    GWT.log("duration = "+duration);

    // Apply CSS transition
    pawn.getStyle().setProperty("transition","all " + duration + "ms linear");// setTransition("all " + duration + "ms linear");
    if(!isFirstSequence){
      pawn.getStyle().setProperty("animationDelay", totalDelay+"ms");
    }
    pawn.getOffsetWidth(); // force reflow

    pawn.getStyle().setLeft(next.getX()-20, Style.Unit.PX);
    pawn.getStyle().setTop(next.getY()-20-15, Style.Unit.PX);

    // Chain to next move
    new com.google.gwt.user.client.Timer() {
      @Override
      public void run() {
        animateStep(pawn, next, remaining, speedPixelsPerMs);
      }
    }.schedule(duration);
  }

  /***
   * remove "transition" and "animation-delay" from showing after it was finished
   */
  public native void clearPawnDivStyles() /*-{
    var elements = $doc.getElementsByClassName('pawnDiv');
    for (var i = 0; i < elements.length; i++) {
      elements[i].style.transition = '';
      elements[i].style.animationDelay = '';
    }
  }-*/;
}
