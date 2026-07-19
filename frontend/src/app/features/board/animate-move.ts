import { MoveResponse, Pawn as ApiPawn, PositionKey } from '../../api';
import { BoardGeometry, Pt } from './board-geometry';
import { PawnAnimator } from './pawn-animator';
import { SoundService } from '../../sound.service';
import { pawnKey } from './pawn-key';

/**
 * Animate the pawns of the last move: convert each pawn's tile path to pixel waypoints and hand them
 * to the {@link PawnAnimator}, staggering a captured pawn's fling-home after its capturer's step and
 * playing the on-board / kill sounds at the right beats. Extracted from Board to keep it lean.
 */
export function animateMove(
  mr: MoveResponse,
  g: BoardGeometry | undefined,
  animator: PawnAnimator,
  sound: SoundService,
): void {
  if (!g) return;
  if (mr.moveType === 'onBoard') sound.play('pawnOnBoard');
  const d1 = walkPawn(g, animator, mr.pawn1, mr.movePawn1, 0);
  const d2 = walkPawn(g, animator, mr.pawn2, mr.movePawn2, 0);
  walkPawn(g, animator, mr.pawnKilledByPawn1, mr.movePawnKilledByPawn1, d1);
  walkPawn(g, animator, mr.pawnKilledByPawn2, mr.movePawnKilledByPawn2, d2);
  // A captured pawn "dies" as it's flung home — play the kill sound as that begins.
  if (mr.pawnKilledByPawn1) sound.play('pawnKilled', d1);
  if (mr.pawnKilledByPawn2) sound.play('pawnKilled', d2);
}

/** Convert a pawn's tile path to pixel waypoints (board geometry) and hand it to the animator. */
function walkPawn(
  g: BoardGeometry,
  animator: PawnAnimator,
  pawn: ApiPawn | undefined,
  move: PositionKey[] | undefined,
  delayMs: number,
): number {
  if (!pawn || !move) return 0;
  const points: Pt[] = [];
  for (const t of move) {
    const p = g.position(t.playerId, t.tileNr);
    if (p) points.push(p);
  }
  return animator.walk(pawnKey(pawn.pawnId), points, delayMs);
}
