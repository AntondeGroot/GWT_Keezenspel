package adg.keezen.audio;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

public class AudioPlayer {

  public static final String TURN_CHANGE =
      "zapsplat_vehicles_car_cabin_light_buttom_press_on_002_113419.mp3";
  public static final String BUTTON_CLICK =
      "zapsplat_multimedia_button_click_bright_001_92098.mp3";
  public static final String PAWN_ON_BOARD =
      "master_of_dreams_8_bit_arcade_up_3_012.mp3";
  public static final String PAWN_KILLED =
      "zapsplat_multimedia_game_sound_classic_arcade_negative_lose_life_die_etc_113889.mp3";
  public static final String MEDAL_AWARDED =
      "zapsplat_multimedia_game_sound_fanfare_trumpets_staccato_finish_complete_109640.mp3";

  /**
   * Play a sound by filename. The URL is resolved relative to the GWT host page so
   * it works regardless of the server's context path (e.g. /keezen/ on the Pi).
   * Silently ignores browser autoplay-policy blocks.
   */
  public static void play(String filename) {
    playUrl(GWT.getHostPageBaseURL() + "assets/audio/" + filename, 1.0);
  }

  /** Play a sound at a reduced volume (0.0 = silent, 1.0 = full). */
  public static void play(String filename, double volume) {
    playUrl(GWT.getHostPageBaseURL() + "assets/audio/" + filename, volume);
  }

  /** Play a sound after {@code delayMs} milliseconds. */
  public static void playDelayed(String filename, int delayMs) {
    if (delayMs <= 0) {
      play(filename);
      return;
    }
    new Timer() {
      @Override
      public void run() {
        play(filename);
      }
    }.schedule(delayMs);
  }

  private static native void playUrl(String url, double volume) /*-{
    var audio = new Audio(url);
    audio.volume = volume;
    var promise = audio.play();
    if (promise !== undefined) {
      promise["catch"](function(e) {}); // autoplay blocked — ignore
    }
  }-*/;
}