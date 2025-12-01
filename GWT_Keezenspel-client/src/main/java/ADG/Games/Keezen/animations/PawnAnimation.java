package ADG.Games.Keezen.animations;

import ADG.Games.Keezen.Point;
import ADG.Games.Keezen.dto.MoveResponseDTO;
import ADG.Games.Keezen.dto.PawnClient;
import ADG.Games.Keezen.dto.PawnDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class PawnAnimation {

  //  private boolean isFirstSequence = true;
  //  private double totalDelay;
  //  private Map<String, DivElement> pawnElements;

  //  public PawnAnimation(Map<String, DivElement> pawnElements) {
  //    this.pawnElements = pawnElements;
  //  }

  public static void animateSequence(
      Map<String, DivElement> pawnElements, MoveResponseDTO moveResponse) {
    // first

    PawnDTO pawn1 = moveResponse.getPawn1();
    PawnDTO pawn2 = moveResponse.getPawn2();
    PawnDTO pawnKilledByPawn1 = moveResponse.getPawnKilledByPawn1();
    PawnDTO pawnKilledByPawn2 = moveResponse.getPawnKilledByPawn2();

    double delayPawnKilledByPawn1 = 0;
    double delayPawnKilledByPawn2 = 0;
    double pixelsPerMs = 100;

    if (pawn1 != null) {
      AnimatePawnPoints animatePawnPointsPawn1 =
          new AnimatePawnPoints(new PawnClient(pawn1), moveResponse.getMovePawn1());
      double distancePawn1 = animatePawnPointsPawn1.getTotalPathLength();
      pixelsPerMs = calculateSpeed(distancePawn1);
      delayPawnKilledByPawn1 = distancePawn1 / pixelsPerMs;
      DivElement pawnElement = pawnElements.get(pawn1.getPawnId());
      GWT.log("move pawn 1 distance: " + distancePawn1);
      GWT.log("move pawn 1 speed: " + pixelsPerMs);
      LinkedList<Point> points = animatePawnPointsPawn1.getPoints();
      Point current = points.pop();

      animateStep(pawnElement, current, points, pixelsPerMs, 0);
    }
    if (pawn2 != null) {
      AnimatePawnPoints animatePawnPointsPawn2 =
          new AnimatePawnPoints(new PawnClient(pawn2), moveResponse.getMovePawn2());
      double distancePawn2 = animatePawnPointsPawn2.getTotalPathLength();
      pixelsPerMs = calculateSpeed(distancePawn2);
      delayPawnKilledByPawn2 = distancePawn2 / pixelsPerMs;
      DivElement pawnElement = pawnElements.get(pawn2.getPawnId());
      animateStep(
          pawnElement,
          animatePawnPointsPawn2.getPoints().get(0),
          animatePawnPointsPawn2.getPoints(),
          pixelsPerMs,
          0);
    }
    if (pawnKilledByPawn1 != null) {
      AnimatePawnPoints animatePawnPointsPawnKilledByPawn1 =
          new AnimatePawnPoints(
              new PawnClient(moveResponse.getPawnKilledByPawn1()), moveResponse.getMovePawn1());
      double distancePawnKilledByPawn1 = animatePawnPointsPawnKilledByPawn1.getTotalPathLength();
      pixelsPerMs = calculateSpeed(distancePawnKilledByPawn1);
      DivElement pawnElement = pawnElements.get(pawnKilledByPawn1.getPawnId());
      animateStep(
          pawnElement,
          animatePawnPointsPawnKilledByPawn1.getPoints().get(0),
          animatePawnPointsPawnKilledByPawn1.getPoints(),
          pixelsPerMs,
          delayPawnKilledByPawn1);
    }
    if (pawnKilledByPawn2 != null) {
      AnimatePawnPoints animatePawnPointsPawnKilledByPawn2 =
          new AnimatePawnPoints(
              new PawnClient(moveResponse.getPawnKilledByPawn2()), moveResponse.getMovePawn2());
      double distancePawnKilledByPawn2 = animatePawnPointsPawnKilledByPawn2.getTotalPathLength();
      pixelsPerMs = calculateSpeed(distancePawnKilledByPawn2);
      DivElement pawnElement = pawnElements.get(pawnKilledByPawn2.getPawnId());
      animateStep(
          pawnElement,
          animatePawnPointsPawnKilledByPawn2.getPoints().get(0),
          animatePawnPointsPawnKilledByPawn2.getPoints(),
          pixelsPerMs,
          delayPawnKilledByPawn2);
    }

    // * get the list of all pawns that should be animated first
    // * get one pawn
    // * determine the total length that pawn has to travel
    // * determine the speed it will need to travel?
    // * get the DivElement for that Pawn
    // * animate the pawn
  }

  public void animateSequence(List<AnimatePawnPoints> sequence) {
    // * get the list of all pawns that should be animated first
    // * get one pawn
    // * determine the total length that pawn has to travel
    // * determine the speed it will need to travel?
    // * get the DivElement for that Pawn
    // * animate the pawn
    //    if (isFirstSequence) {
    //      totalDelay = 0;
    //    }
    //    for (AnimatePawnPoints p : sequence) {
    //      GWT.log("Animating pawn path: " + p);
    //
    //      PawnClient pawn = p.getPawn();
    //      LinkedList<Point> points = p.getPoints();
    //      double distance = p.getTotalPathLength();
    //
    //      double pixelsPerMs = calculateSpeed(distance);
    //
    //      DivElement pawnElement = pawnElements.get(pawn.getPawnId());
    //
    //      animateStep(pawnElement, points.get(0), points, pixelsPerMs);
    //
    //      totalDelay = max(totalDelay, distance / pixelsPerMs);
    //    }
    //    isFirstSequence = !isFirstSequence;
  }

  private static double calculateSpeed(double distance) {
    double pixelsPerMs = 0.1;
    if (distance > 200) {
      pixelsPerMs = 0.12;
    }
    ;
    if (distance > 400) {
      pixelsPerMs = 0.16;
    }
    ;
    pixelsPerMs = pixelsPerMs * AnimationSpeed.getSpeed();

    return pixelsPerMs;
  }

  private static void animateStep(
      DivElement pawn,
      Point current,
      Queue<Point> remaining,
      double speedPixelsPerMs,
      double totalDelay) {

    if (remaining.isEmpty()) {
      return;
    }

    Point next = remaining.poll();
    double dx = next.getX() - current.getX();
    double dy = next.getY() - current.getY();
    double distancePixels = Math.sqrt(dx * dx + dy * dy);

    double duration = (distancePixels / speedPixelsPerMs);
    GWT.log("current: " + current);
    GWT.log("next: " + next);
    GWT.log("duration = " + duration);
    GWT.log("distancePixels = " + distancePixels);
    GWT.log("totalDelay = " + totalDelay);

    // Apply CSS transition
    pawn.getStyle().setProperty("transition", "all " + duration + "ms linear");
    pawn.getStyle().setProperty("animationDelay", totalDelay + "ms");
    pawn.getOffsetWidth(); // force reflow

    pawn.getStyle().setLeft(next.getX() - 20, Style.Unit.PX);
    pawn.getStyle().setTop(next.getY() - 20 - 15, Style.Unit.PX);

    // Chain to next move
    new com.google.gwt.user.client.Timer() {
      @Override
      public void run() {
        animateStep(pawn, next, remaining, speedPixelsPerMs, duration);
      }
    }.schedule((int) duration);
  }
}
