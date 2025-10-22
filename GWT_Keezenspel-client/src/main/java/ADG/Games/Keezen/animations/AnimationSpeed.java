package ADG.Games.Keezen.animations;

public class AnimationSpeed {

  private static int speed = 1;

  public static void setSpeed(int speed) {
    if (speed <= 0) {
      speed = 1;
    }
    AnimationSpeed.speed = speed;
  }

  public static int getSpeed() {
    return speed;
  }
}
