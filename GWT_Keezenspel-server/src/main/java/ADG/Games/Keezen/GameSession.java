package ADG.Games.Keezen;

import java.util.UUID;

public class GameSession {
  private final GameState gameState;
  private final CardsDeckInterface cardsDeck;
  private final String sessionId;
  private final String roomName;
  private final int maxPlayers;

  public GameSession(String sessionId, String roomName, Integer maxPlayers) {
    this.sessionId = sessionId;
    this.cardsDeck = new CardsDeck();
    this.gameState = new GameState(cardsDeck);
    this.cardsDeck.setGameState(this.gameState);
    this.roomName = roomName;
    this.maxPlayers = maxPlayers;
  }

  public GameSession() {
    this.sessionId = UUID.randomUUID().toString();
    this.cardsDeck = new CardsDeck();
    this.gameState = new GameState(cardsDeck);
    this.cardsDeck.setGameState(this.gameState);
    this.roomName = "";
    this.maxPlayers = 3;
  }

  public GameSession(CardsDeckInterface cardsDeck){
    this.sessionId = UUID.randomUUID().toString();
    this.cardsDeck = cardsDeck;
    this.gameState = new GameState(cardsDeck);
    this.cardsDeck.setGameState(this.gameState);
    this.roomName = "";
    this.maxPlayers = 3;
  }

  public GameSession(CardsDeckInterface cardsDeck, int animationSpeed){
    this.sessionId = UUID.randomUUID().toString();
    this.cardsDeck = cardsDeck;
    this.gameState = new GameState(cardsDeck);
    this.gameState.setAnimationSpeed(animationSpeed);
    this.cardsDeck.setGameState(this.gameState);
    this.roomName = "";
    this.maxPlayers = 3;
  }

  public GameState getGameState() { return gameState; }
  public CardsDeckInterface getCardsDeck() { return cardsDeck; }
  public String getSessionId() { return sessionId; }
  public String getRoomName() { return roomName; }
  public int getMaxPlayers() { return maxPlayers; }

  public void reset(){
    this.gameState.reset();
  }
}
