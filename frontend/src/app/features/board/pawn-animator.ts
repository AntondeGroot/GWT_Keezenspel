import { signal } from '@angular/core';
import { Pt } from './board-geometry';

/** A pawn's live animated position: pixel coordinates + the transition duration of the step
 *  currently underway (ms). Present in {@link PawnAnimator.positions} only while it is moving. */
export interface PawnPos {
  x: number;
  y: number;
  ms: number;
}

/** px/ms — faster over longer paths, so a lap doesn't crawl (ported from calculateSpeed). */
function moveSpeed(distance: number): number {
  if (distance > 400) return 0.16;
  if (distance > 200) return 0.12;
  return 0.1;
}

/**
 * A small, framework-light engine that walks pawns along pixel waypoints. It owns only the live
 * position overrides (a signal keyed by pawn id); the view reads {@link positions} to place a pawn
 * mid-move instead of at the server's already-final tile, and a moving pawn drops out of the map
 * when it settles. Geometry, sounds and move sequencing stay with the game — it just supplies the
 * waypoints and calls {@link walk} — so this is unit-testable like a plain state machine.
 */
export class PawnAnimator {
  readonly positions = signal<Map<string, PawnPos>>(new Map());

  /**
   * Walk pawn `id` along `points` (pixel waypoints). Holds it at the start now (so the server's
   * final tile doesn't snap it there), then walks the waypoints after `delayMs`. Returns the total
   * walk time (ms) so a caller can sequence a follow-on move (e.g. a captured pawn flung home
   * after its killer arrives).
   */
  walk(id: string, points: Pt[], delayMs = 0): number {
    if (points.length < 2) return 0;

    let total = 0;
    for (let i = 1; i < points.length; i++) {
      total += Math.hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y);
    }
    const speed = moveSpeed(total); // px/ms
    this.set(id, points[0].x, points[0].y, 0); // hold at the start now

    const go = () =>
      requestAnimationFrame(() => requestAnimationFrame(() => this.step(id, points, 1, speed)));
    if (delayMs > 0) setTimeout(go, delayMs);
    else go();
    return Math.round(total / speed);
  }

  private step(id: string, points: Pt[], i: number, speed: number): void {
    if (i >= points.length) {
      this.clear(id); // done — settle onto the server's final tile
      return;
    }
    const d = Math.hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y);
    const ms = Math.max(16, Math.round(d / speed));
    this.set(id, points[i].x, points[i].y, ms);
    setTimeout(() => this.step(id, points, i + 1, speed), ms);
  }

  private set(id: string, x: number, y: number, ms: number): void {
    this.positions.update((m) => new Map(m).set(id, { x, y, ms }));
  }

  private clear(id: string): void {
    this.positions.update((m) => {
      const n = new Map(m);
      n.delete(id);
      return n;
    });
  }
}
