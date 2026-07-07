/**
 * Team play, step 3: which previewed landing tiles would capture a *teammate's* pawn.
 *
 * A move onto an occupied tile captures (sends home) whatever pawn sits there — teammates
 * included (that stays legal). Of the tiles the selected move could land on, this returns the
 * ones holding a pawn owned by another player on the mover's own team, so the board can warn
 * ("you'd send your own side home") instead of showing the normal go-ahead preview.
 *
 * Returns tile keys (`"playerId:tileNr"`). Empty when the mover has no team (teams off), since
 * then nothing is a teammate.
 */
export function teammateCaptureKeys(
  previewTiles: Set<string>,
  occupants: { key: string; ownerId: string }[],
  teamOf: (ownerId: string) => number | null | undefined,
  moverId: string,
): Set<string> {
  const out = new Set<string>();
  const moverTeam = teamOf(moverId);
  if (moverTeam == null) return out;
  for (const occ of occupants) {
    if (previewTiles.has(occ.key) && occ.ownerId !== moverId && teamOf(occ.ownerId) === moverTeam) {
      out.add(occ.key);
    }
  }
  return out;
}
