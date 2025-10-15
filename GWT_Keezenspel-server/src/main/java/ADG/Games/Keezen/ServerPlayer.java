package ADG.Games.Keezen;

import com.adg.openapi.model.Player;

public class ServerPlayer extends Player {

  // just for readability
  public boolean hasFinished(){
    if(getPlace() == null) {
      return false;
    }
    return getPlace() > 0;
  }

  // needs extra validation
  public void setActive() {
    if(getPlace() == -1){
      // only allow players who haven't finished yet to be set to active when a new round starts
      setIsActive(true);
    }
  }
}
