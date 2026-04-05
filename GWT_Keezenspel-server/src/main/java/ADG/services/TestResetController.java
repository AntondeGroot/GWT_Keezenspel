package ADG.services;

import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.PositionKey;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestResetController {

  @PostMapping("/reset")
  public void resetGameState() {
    GameSession gameSession = GameRegistry.getGame("123");
    gameSession.reset();
  }

  @PostMapping("/set-card/{playerId}/{cardValue}")
  public void setCardForPlayer(
      @PathVariable("playerId") String playerId,
      @PathVariable("cardValue") int cardValue) {
    GameSession gameSession = GameRegistry.getGame("123");
    // Use cardValue as the UUID so the client can reliably round-trip it back in MoveRequest.
    // Works for both the real and mock card decks.
    Card card = new Card().value(cardValue).suit(0).uuid(cardValue);
    gameSession.getCardsDeck().giveCardToPlayerForTesting(playerId, card);
  }

  @PostMapping("/set-card/{sessionId}/{playerId}/{cardValue}")
  public void setCardForPlayer(
      @PathVariable("sessionId") String sessionId,
      @PathVariable("playerId") String playerId,
      @PathVariable("cardValue") int cardValue) {
    GameSession gameSession = GameRegistry.getGame(sessionId);
    // Use cardValue as the UUID so the client can reliably round-trip it back in MoveRequest.
    // Works for both the real and mock card decks.
    Card card = new Card().value(cardValue).suit(0).uuid(cardValue);
    gameSession.getCardsDeck().giveCardToPlayerForTesting(playerId, card);
  }

  @PostMapping("/start-game/{sessionId}")
  public void startGameWithoutShuffle(@PathVariable("sessionId") String sessionId) {
    GameSession gameSession = GameRegistry.getGame(sessionId);
    gameSession.getGameState().start(false);
  }

  @PostMapping("/set-pawn/{sessionId}/{playerId}/{pawnNr}/{sectionOwnerId}/{tileNr}")
  public void setPawnPosition(
      @PathVariable("sessionId") String sessionId,
      @PathVariable("playerId") String playerId,
      @PathVariable("pawnNr") int pawnNr,
      @PathVariable("sectionOwnerId") String sectionOwnerId,
      @PathVariable("tileNr") int tileNr) {
    GameSession gameSession = GameRegistry.getGame(sessionId);
    gameSession.getGameState().getPawns().stream()
        .filter(pawn -> pawn.getPlayerId().equals(playerId)
                     && pawn.getPawnId().getPawnNr() == pawnNr)
        .findFirst()
        .ifPresent(pawn -> pawn.setCurrentTileId(new PositionKey(sectionOwnerId, tileNr)));
  }
}
