package ADG.Games.Keezen.dto;

import java.util.Objects;

public class PlayerClient {
  private String id;
  private String name;
  private String profilePictureUrl;
  private String color;
  private boolean isActive;
  private boolean isPlaying;
  private int place = -1;
  private int playerInt; // todo is this necessary?

  public PlayerClient(PlayerDTO playerDTO) {
    this.id = playerDTO.getId();
    this.name = playerDTO.getName();
    this.profilePictureUrl = playerDTO.getProfilePic();
    this.color = playerDTO.getColor();
    this.isActive = playerDTO.isActive();
    this.isPlaying = playerDTO.isPlaying();
    this.place = playerDTO.getPlace();
    this.playerInt = playerDTO.getPlayerInt();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getProfilePictureUrl() {
    return profilePictureUrl;
  }

  public String getColor() {
    return color;
  }

  public boolean isActive() {
    return isActive;
  }

  public boolean isPlaying() {
    return isPlaying;
  }

  public int getPlace() {
    return place;
  }

  public int getPlayerInt() {
    return playerInt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlayerClient player = (PlayerClient) o;
    return Objects.equals(id, player.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
