package ADG.Games.Keezen.utils;

import com.adg.openapi.model.Player;
import java.util.Random;
import java.util.UUID;

public class ApiModelHelpers {

  private static final Random rnd = new Random();

  private static String randomAlphaNum(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(chars.charAt(rnd.nextInt(chars.length())));
    }
    return sb.toString();
  }

  public static String getRandomRoomName(){
    return "TestRoomName-"+ randomAlphaNum(8);
  }

  public static Player getRandomPlayer(){
    return new Player()
        .id(UUID.randomUUID().toString())
        .name("TestPlayer_"+randomAlphaNum(4))
        .color(randomAlphaNum(8))
        .profilePic("url_"+randomAlphaNum(8));
  }
}
