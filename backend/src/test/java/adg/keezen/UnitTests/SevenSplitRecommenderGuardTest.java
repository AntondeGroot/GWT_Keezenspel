package adg.keezen.UnitTests;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import adg.keezen.GameState;
import adg.processing.SevenSplitRecommender;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import org.junit.jupiter.api.Test;

/**
 * The early-exit guards of {@link SevenSplitRecommender#recommend}, driven with a mocked GameState
 * so each rejection branch is reached in isolation (the happy-path allocation logic is covered by
 * the scenario-based {@link SevenSplitRecommenderTest}).
 */
class SevenSplitRecommenderGuardTest {

  private static final PawnId P1 = new PawnId("0", 1);
  private static final PawnId P2 = new PawnId("0", 2);
  private static final Card SEVEN = new Card().suit(0).value(7).uuid(507);

  private static MoveRequest req(PawnId p1, PawnId p2, Integer cardId) {
    MoveRequest r = new MoveRequest();
    r.setPlayerId("0");
    r.setPawn1Id(p1);
    r.setPawn2Id(p2);
    r.setCardId(cardId);
    return r;
  }

  private static Pawn pawnAt(int tileNr) {
    PositionKey tile = new PositionKey("0", tileNr);
    return new Pawn("0", P1, tile, tile);
  }

  @Test
  void noRecommendationWhenPawn1IsMissing() {
    assertNull(SevenSplitRecommender.recommend(mock(GameState.class), req(null, P2, 507)));
  }

  @Test
  void noRecommendationWhenPawn2IsMissing() {
    assertNull(SevenSplitRecommender.recommend(mock(GameState.class), req(P1, null, 507)));
  }

  @Test
  void noRecommendationWhenCardIdIsMissing() {
    assertNull(SevenSplitRecommender.recommend(mock(GameState.class), req(P1, P2, null)));
  }

  @Test
  void noRecommendationWhenPlayerDoesNotHoldTheCard() {
    GameState gs = mock(GameState.class);
    when(gs.getCard(507, "0")).thenReturn(null);
    assertNull(SevenSplitRecommender.recommend(gs, req(P1, P2, 507)));
  }

  @Test
  void noRecommendationWhenTheCardIsNotASeven() {
    GameState gs = mock(GameState.class);
    when(gs.getCard(509, "0")).thenReturn(new Card().suit(0).value(9).uuid(509));
    assertNull(SevenSplitRecommender.recommend(gs, req(P1, P2, 509)));
  }

  @Test
  void noRecommendationWhenNeitherPawnIsNearAWall() {
    GameState gs = mock(GameState.class);
    when(gs.getCard(507, "0")).thenReturn(SEVEN);
    when(gs.getPawn(P1)).thenReturn(pawnAt(3));
    when(gs.getPawn(P2)).thenReturn(pawnAt(4));
    // isPawnOnLastSection / tileIsABlockade default to false, so no wall is within reach.
    assertNull(SevenSplitRecommender.recommend(gs, req(P1, P2, 507)));
  }
}
