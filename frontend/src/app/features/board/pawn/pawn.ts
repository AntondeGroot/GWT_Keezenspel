import { Component, Input } from '@angular/core';
import { teamAccent } from '../../../player-colors';

/**
 * One distinct flag silhouette per team, indexed by teamId, so teams are told apart by
 * shape as well as colour (redundant coding for low-vision players). The staff and flag
 * live in a 657×874 viewBox; the flag hangs off the staff at x≈488.
 */
const TEAM_FLAG_PATHS = [
  'M 488,122 L 628,156 L 488,190 Z', // A · triangle (single point)
  'M 488,122 L 628,128 L 566,156 L 628,184 L 488,190 Z', // B · swallowtail (forked)
  'M 488,122 L 618,122 L 618,190 L 488,190 Z', // C · square banner (flat)
  'M 488,122 L 600,139 L 600,173 L 488,190 Z', // D · guidon (blunt taper)
];

/**
 * A single pawn, rendered as the (ported) pawn.svg. The owner's colour is passed
 * in and exposed to the SVG as the CSS variable `--pawn-color`; the template uses
 * it directly for head/body and a `color-mix`-darkened shade for collar/base
 * (mirrors the GWT client's main colour + 0.6 darken).
 *
 * In team play a heraldic pennant rides the pawn's shoulder — the team's accent colour
 * (`--team-accent`) in a per-team shape. It renders only when `teamId` is set (teams on).
 */
@Component({
  selector: 'app-pawn',
  templateUrl: './pawn.html',
  styleUrl: './pawn.scss',
  host: {
    '[style.--pawn-color]': 'color',
    '[style.--pawn-highlight]': 'highlight',
    '[style.--team-accent]': 'teamColor',
  },
})
export class Pawn {
  @Input() color = '#888888';
  /** Selection-highlight stroke colour, or undefined when not selected. */
  @Input() highlight?: string;
  /** 0-based team index in team play, or null/undefined when teams are off. */
  @Input() teamId?: number | null;

  /** The team's accent colour, or undefined when there's no team (no pennant). */
  get teamColor(): string | undefined {
    return teamAccent(this.teamId);
  }

  /** The flag outline for this team's shape. */
  get flagPath(): string {
    return TEAM_FLAG_PATHS[this.teamId ?? 0] ?? TEAM_FLAG_PATHS[0];
  }
}