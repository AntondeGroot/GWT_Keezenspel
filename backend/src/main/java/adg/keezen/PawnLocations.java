package adg.keezen;

import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import java.util.List;
import java.util.function.Supplier;

/**
 * Lookups over the pawns currently on the board: find a pawn by id or by the tile it sits on, read a
 * pawn's tile, and move a pawn. Extracted from GameState as queries over the (live) pawns list —
 * supplied lazily because GameState replaces the list on reset. GameState keeps thin delegating
 * methods.
 */
class PawnLocations {

  private final Supplier<List<Pawn>> pawns;

  PawnLocations(Supplier<List<Pawn>> pawns) {
    this.pawns = pawns;
  }

  /** The pawn with this id, or null if it isn't on the board. */
  Pawn withId(PawnId pawnId) {
    for (Pawn pawn : pawns.get()) {
      if (pawn.getPawnId().equals(pawnId)) {
        return pawn;
      }
    }
    return null;
  }

  /** The pawn sitting on this tile, or null if the tile is empty. */
  Pawn atTile(PositionKey tile) {
    for (Pawn pawn : pawns.get()) {
      if (pawn.getCurrentTileId().equals(tile)) {
        return pawn;
      }
    }
    return null;
  }

  /** Set a pawn's location without any validation (matched by pawn id). */
  void moveTo(Pawn selectedPawn) {
    for (Pawn pawn : pawns.get()) {
      if (pawn.getPawnId().equals(selectedPawn.getPawnId())) {
        pawn.setCurrentTileId(selectedPawn.getCurrentTileId());
        return;
      }
    }
  }
}
