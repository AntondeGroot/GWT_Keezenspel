package adg.services;

import adg.keezen.CardsDeckInterface;
import adg.keezen.GameRegistry;
import adg.keezen.GameSession;
import adg.keezen.GameState;
import com.adg.openapi.api.CardsApiDelegate;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.CardResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    List<Card> playerCards = cardsDeck.getCardsForPlayer(playerId);

    return ResponseEntity.ok(playerCards);
  }

  @Override
  public ResponseEntity<Void> playerForfeits(String sessionId, String playerId) {

    GameSession session = GameRegistry.getGame(sessionId);
    if (session == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    GameState gameState = session.getGameState();

    if (gameState.hasStarted() && gameState.getPlayerIdTurn().equals(playerId)) {
      gameState.processOnForfeit(playerId);
      session.setLastMoveResponse(null);
      return ResponseEntity.status(HttpStatus.OK).body(null);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }

  @Override
  public ResponseEntity<CardResponse> getPubliclyAvailableCardInformation(String sessionId) {
    // 1️⃣ Validate input
    if (sessionId == null || sessionId.isBlank()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    // 2️⃣ Get the game session
    GameSession session = GameRegistry.getGame(sessionId);
    if (session == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // 3️⃣ Get the deck
    CardsDeckInterface cardsDeck = session.getCardsDeck();
    if (cardsDeck == null) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // 4️⃣ Collect played cards as card IDs ("suit_value") for client-side resource lookup
    List<String> playedCardIds =
        cardsDeck.getPlayedCards().stream()
            .map(card -> card.getSuit() + "_" + card.getValue())
            .toList();

    // 5️⃣ Collect number of cards per player
    var nrOfCardsPerPlayer = cardsDeck.getNrOfCardsPerPlayer(); // Map<String, Integer>

    // 6️⃣ Construct response DTO
    CardResponse response =
        new CardResponse()
            .playedCards(playedCardIds)
            .nrOfCardsPerPlayer(nrOfCardsPerPlayer);

    // 7️⃣ Return 200 OK
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
  }
}
