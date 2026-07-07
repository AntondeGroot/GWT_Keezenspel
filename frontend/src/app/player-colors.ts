/**
 * Player colours are a *frontend* concern. The backend only distinguishes players by a
 * stable `playerInt` — the seat index, identical for every viewer — so everyone sees the
 * same player as, say, red ("it's red's turn"). This maps that index to a colour; a future
 * colour-blind mode can swap the palette with no backend change.
 *
 * The palette matches the GWT client's PlayerColors, so colours are unchanged from before.
 */
const PALETTE = [
  '#A52A2A', '#1a6fbd', '#008000', '#A5A500',
  '#6A5ACD', '#FF8C00', '#008B8B', '#8B008B',
] as const;

const FALLBACK = '#8a8a8a';

/** Colour for a player's seat index (`playerInt`); a neutral grey if it's missing. */
export function seatColor(playerInt: number | null | undefined): string {
  if (playerInt == null) return FALLBACK;
  return PALETTE[playerInt] ?? FALLBACK;
}

/**
 * Team accents for team play, keyed by 0-based `teamId`. Bold, well-spaced hues (with the
 * pennant's shape as a second, colour-independent cue) so teams stay distinguishable for
 * low-vision players. Kept light enough that the roster's dark tag text stays legible, and
 * mirrored in player-list.scss `--team-a…d` so a pawn's pennant and its team's scoreline
 * match. Undefined when the player has no team (teams off), so no pennant shows.
 */
const TEAM_PALETTE = ['#f0932a', '#4da3e8', '#35be83', '#ee7ab6'] as const; // orange, blue, green, pink

export function teamAccent(teamId: number | null | undefined): string | undefined {
  if (teamId == null) return undefined;
  return TEAM_PALETTE[teamId];
}
