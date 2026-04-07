package adg.keezen.animations;

import adg.keezen.Point;
import adg.keezen.audio.AudioPlayer;
import adg.keezen.util.PawnLayout;
import adg.keezen.dto.MoveResponseDTO;
import adg.keezen.dto.PawnClient;
import adg.keezen.dto.PawnDTO;
import adg.keezen.dto.PositionKeyDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.Timer;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public final class PawnAnimation {

  private static final double BASE_SPEED_PIXELS_PER_MS = 0.1;
  private static final double MEDIUM_DISTANCE_SPEED_PIXELS_PER_MS = 0.12;
  private static final double LONG_DISTANCE_SPEED_PIXELS_PER_MS = 0.16;

  private static final double MEDIUM_DISTANCE_THRESHOLD = 200;
  private static final double LONG_DISTANCE_THRESHOLD = 400;

  private static int runningAnimations = 0;
  private static Runnable onAllAnimationsComplete;

  private PawnAnimation() {
  }

  public static boolean isAnimating() {
    return runningAnimations > 0;
  }

  public static void animateSequence(
      Map<String, DivElement> pawnElements, MoveResponseDTO moveResponse, Runnable onComplete) {

    onAllAnimationsComplete = onComplete;

    int delayForPawn1Kill = schedulePawnMove(
        pawnElements,
        moveResponse.getPawn1(),
        moveResponse.getMovePawn1(),
        false,
        0);

    int delayForPawn2Kill = schedulePawnMove(
        pawnElements,
        moveResponse.getPawn2(),
        moveResponse.getMovePawn2(),
        false,
        0);

    schedulePawnMove(
        pawnElements,
        moveResponse.getPawnKilledByPawn1(),
        moveResponse.getMovePawnKilledByPawn1(),
        true,
        delayForPawn1Kill);

    schedulePawnMove(
        pawnElements,
        moveResponse.getPawnKilledByPawn2(),
        moveResponse.getMovePawnKilledByPawn2(),
        true,
        delayForPawn2Kill);
  }

  private static int schedulePawnMove(
      Map<String, DivElement> pawnElements,
      PawnDTO pawn,
      JsArray<PositionKeyDTO> move,
      boolean playKilledSound,
      int delayMs) {

    if (pawn == null || move == null) {
      return 0;
    }

    PawnClient pawnClient = new PawnClient(pawn);
    DivElement element = pawnElements.get(pawnClient.getPawnId());

    if (element == null) {
      GWT.log("No DOM element found for pawn: " + pawnClient.getPawnId());
      return 0;
    }

    AnimatePawnPoints animationPoints = new AnimatePawnPoints(pawnClient, move);
    LinkedList<Point> points = animationPoints.getPoints();

    if (points == null || points.isEmpty()) {
      GWT.log("No animation points found for pawn: " + pawnClient.getPawnId());
      return 0;
    }

    double totalDistance = animationPoints.getTotalPathLength();
    double speedPixelsPerMs = calculateSpeed(totalDistance);
    int totalDurationMs = (int) Math.round(totalDistance / speedPixelsPerMs);

    Point start = points.poll();
    if (start == null) {
      GWT.log("Animation path has no start point for pawn: " + pawnClient.getPawnId());
      return 0;
    }

    runningAnimations++;

    if (delayMs <= 0) {
      if (playKilledSound) {
        AudioPlayer.play(AudioPlayer.PAWN_KILLED);
      }
      animateStep(element, start, points, speedPixelsPerMs);
    } else {
      new Timer() {
        @Override
        public void run() {
          if (playKilledSound) {
            AudioPlayer.play(AudioPlayer.PAWN_KILLED);
          }
          animateStep(element, start, points, speedPixelsPerMs);
        }
      }.schedule(delayMs);
    }

    return totalDurationMs;
  }

  private static void onAnimationStepComplete() {
    runningAnimations--;
    if (runningAnimations == 0 && onAllAnimationsComplete != null) {
      Runnable cb = onAllAnimationsComplete;
      onAllAnimationsComplete = null;
      cb.run();
    }
  }

  private static double calculateSpeed(double distance) {
    double pixelsPerMs = BASE_SPEED_PIXELS_PER_MS;

    if (distance > LONG_DISTANCE_THRESHOLD) {
      pixelsPerMs = LONG_DISTANCE_SPEED_PIXELS_PER_MS;
    } else if (distance > MEDIUM_DISTANCE_THRESHOLD) {
      pixelsPerMs = MEDIUM_DISTANCE_SPEED_PIXELS_PER_MS;
    }

    return pixelsPerMs * AnimationSpeed.getSpeed();
  }

  private static void animateStep(
      DivElement pawn,
      Point current,
      Queue<Point> remaining,
      double speedPixelsPerMs) {

    if (pawn == null || remaining == null || remaining.isEmpty()) {
      onAnimationStepComplete();
      return;
    }

    Point next = remaining.poll();
    if (next == null) {
      onAnimationStepComplete();
      return;
    }

    double dx = next.getX() - current.getX();
    double dy = next.getY() - current.getY();
    double distancePixels = Math.sqrt(dx * dx + dy * dy);
    int durationMs = (int) Math.round(distancePixels / speedPixelsPerMs);

    GWT.log("animateStep: " + current + " -> " + next + " duration=" + durationMs + "ms");

    pawn.getStyle().setProperty(
        "transition",
        "left " + durationMs + "ms linear, top " + durationMs + "ms linear");

    pawn.getOffsetWidth(); // force reflow

    PawnLayout.applyPosition(pawn, next);

    new Timer() {
      @Override
      public void run() {
        animateStep(pawn, next, remaining, speedPixelsPerMs);
      }
    }.schedule(durationMs);
  }
}