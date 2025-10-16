package ADG.services;

import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameRegistry;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveType;
import com.adg.openapi.api.CardsApiDelegate;
import com.adg.openapi.model.Card;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardsApiDelegateImpl implements CardsApiDelegate {

  @Override
  public ResponseEntity<List<Card>> getPlayerCards(String gameId, String playerId) {
    // Input validation
    if (gameId == null || playerId == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of());
    }

    // Get the session
    GameSession session = GameRegistry.getGame(gameId);
    if (session == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
    }

    // Get the deck
    CardsDeckInterface cardsDeck = session.getCardsDeck();
    if (cardsDeck == null) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
    }

    // Domain cards (your internal Card class) //todo: when Card is replaced by openapi.model.Card This has to be redone
    List<ADG.Games.Keezen.Cards.Card> domainCards = cardsDeck.getCardsForPlayer(playerId);
    if (domainCards == null) {
      return ResponseEntity.ok(List.of());
    }

    // Map to API model //todo: see above
    List<Card> responseCards = domainCards.stream()
        .map(c -> new Card()
            .suit(c.getSuit())
            .value(c.getCardValue())
            .uuid(c.getUniqueCardNumber()))
        .collect(Collectors.toList());

    return ResponseEntity.ok(responseCards);
  }

  @Override
  public ResponseEntity<Void> playerForfeits(String sessionId,
      String playerId) {

    GameSession session = GameRegistry.getGame(sessionId);
    if (session == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    GameState gameState = session.getGameState();

    if(gameState.hasStarted() && gameState.getPlayerIdTurn().equals(playerId)) {

      // todo improve when switching to openapi
      MoveMessage moveMessage = new MoveMessage();
      moveMessage.setPlayerId(playerId);
      moveMessage.setMoveType(MoveType.FORFEIT);
      gameState.processOnForfeit(moveMessage);
      return ResponseEntity.status(HttpStatus.OK).body(null);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }
}