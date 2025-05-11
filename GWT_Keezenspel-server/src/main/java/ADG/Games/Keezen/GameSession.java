package ADG.Games.Keezen;

import java.util.UUID;

public class GameSession {
  private final GameState gameState;
  private final CardsDeckInterface cardsDeck;
  private final String sessionId;

  public GameSession() {
    this.sessionId = UUID.randomUUID().toString();
    this.cardsDeck = new CardsDeck();
    this.gameState = new GameState(cardsDeck);
    this.cardsDeck.setGameState(this.gameState);
  }

  public GameSession(CardsDeckInterface cardsDeck){
    this.sessionId = UUID.randomUUID().toString();
    this.cardsDeck = cardsDeck;
    this.gameState = new GameState(cardsDeck);
    this.cardsDeck.setGameState(this.gameState);
  }

  public GameSession(CardsDeckInterface cardsDeck, int animationSpeed){
    this.sessionId = UUID.randomUUID().toString();
    this.cardsDeck = cardsDeck;
    this.gameState = new GameState(cardsDeck);
    this.gameState.setAnimationSpeed(animationSpeed);
    this.cardsDeck.setGameState(this.gameState);
  }

  public GameState getGameState() { return gameState; }
  public CardsDeckInterface getCardsDeck() { return cardsDeck; }
  public String getSessionId() { return sessionId; }

  public void reset(){
    this.gameState.reset();
  }
}
