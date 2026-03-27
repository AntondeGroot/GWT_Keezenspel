package ADG.Games.Keezen.animations;

import ADG.Games.Keezen.Point;
import ADG.Games.Keezen.audio.AudioPlayer;
import ADG.Games.Keezen.dto.MoveResponseDTO;
import ADG.Games.Keezen.dto.PawnClient;
import ADG.Games.Keezen.dto.PawnDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class PawnAnimation {

  private static int runningAnimations = 0;

  public static boolean isAnimating() {
    return runningAnimations > 0;
  }

  public static void animateSequence(
      Map<String, DivElement> pawnElements, MoveResponseDTO moveResponse) {

    PawnDTO pawn1 = moveResponse.getPawn1();
    PawnDTO pawn2 = moveResponse.getPawn2();
    PawnDTO pawnKilledByPawn1 = moveResponse.getPawnKilledByPawn1();
    PawnDTO pawnKilledByPawn2 = moveResponse.getPawnKilledByPawn2();

    double delayKilledByPawn1 = 0;
    double delayKilledByPawn2 = 0;

    if (pawn1 != null) {
      PawnClient pawn1Client = new PawnClient(pawn1);
      AnimatePawnPoints animPoints = new AnimatePawnPoints(pawn1Client, moveResponse.getMovePawn1());
      double distance = animPoints.getTotalPathLength();
      double speed = calculateSpeed(distance);
      delayKilledByPawn1 = distance / speed;
      DivElement element = pawnElements.get(pawn1Client.getPawnId());
      LinkedList<Point> points = animPoints.getPoints();
      Point current = points.pop();
      runningAnimations++;
      animateStep(element, current, points, speed);
    }

    if (pawn2 != null) {
      PawnClient pawn2Client = new PawnClient(pawn2);
      AnimatePawnPoints animPoints = new AnimatePawnPoints(pawn2Client, moveResponse.getMovePawn2());
      double distance = animPoints.getTotalPathLength();
      double speed = calculateSpeed(distance);
      delayKilledByPawn2 = distance / speed;
      DivElement element = pawnElements.get(pawn2Client.getPawnId());
      LinkedList<Point> points = animPoints.getPoints();
      Point current = points.pop();
      runningAnimations++;
      animateStep(element, current, points, speed);
    }

    if (pawnKilledByPawn1 != null) {
      PawnClient killedClient = new PawnClient(pawnKilledByPawn1);
      AnimatePawnPoints animPoints = new AnimatePawnPoints(killedClient, moveResponse.getMovePawnKilledByPawn1());
      double speed = calculateSpeed(animPoints.getTotalPathLength());
      DivElement element = pawnElements.get(killedClient.getPawnId());
      LinkedList<Point> points = animPoints.getPoints();
      Point current = points.pop();
      runningAnimations++;
      int delay = (int) delayKilledByPawn1;
      new Timer() {
        @Override public void run() {
          AudioPlayer.play(AudioPlayer.PAWN_KILLED);
          animateStep(element, current, points, speed);
        }
      }.schedule(delay);
    }

    if (pawnKilledByPawn2 != null) {
      PawnClient killedClient = new PawnClient(pawnKilledByPawn2);
      AnimatePawnPoints animPoints = new AnimatePawnPoints(killedClient, moveResponse.getMovePawnKilledByPawn2());
      double speed = calculateSpeed(animPoints.getTotalPathLength());
      DivElement element = pawnElements.get(killedClient.getPawnId());
      LinkedList<Point> points = animPoints.getPoints();
      Point current = points.pop();
      runningAnimations++;
      int delay = (int) delayKilledByPawn2;
      new Timer() {
        @Override public void run() {
          AudioPlayer.play(AudioPlayer.PAWN_KILLED);
          animateStep(element, current, points, speed);
        }
      }.schedule(delay);
    }
  }

  private static double calculateSpeed(double distance) {
    double pixelsPerMs = 0.1;
    if (distance > 200) pixelsPerMs = 0.12;
    if (distance > 400) pixelsPerMs = 0.16;
    return pixelsPerMs * AnimationSpeed.getSpeed();
  }

  private static void animateStep(
      DivElement pawn, Point current, Queue<Point> remaining, double speedPixelsPerMs) {

    if (remaining.isEmpty()) {
      runningAnimations--;
      return;
    }

    Point next = remaining.poll();
    double dx = next.getX() - current.getX();
    double dy = next.getY() - current.getY();
    double distancePixels = Math.sqrt(dx * dx + dy * dy);
    double duration = distancePixels / speedPixelsPerMs;

    GWT.log("animateStep: " + current + " -> " + next + " duration=" + duration + "ms");

    pawn.getStyle().setProperty(
        "transition", "left " + duration + "ms linear, top " + duration + "ms linear");
    pawn.getOffsetWidth(); // force reflow so transition applies

    pawn.getStyle().setLeft(next.getX() - 20, Style.Unit.PX);
    pawn.getStyle().setTop(next.getY() - 20 - 15, Style.Unit.PX);

    new Timer() {
      @Override
      public void run() {
        animateStep(pawn, next, remaining, speedPixelsPerMs);
      }
    }.schedule((int) duration);
  }
}