import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { PawnAnimator } from './pawn-animator';
import { Pt } from './board-geometry';

const pt = (x: number, y: number): Pt => ({ x, y });

describe('PawnAnimator', () => {
  let anim: PawnAnimator;

  beforeEach(() => {
    anim = new PawnAnimator();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.runOnlyPendingTimers();
    vi.useRealTimers();
  });

  it('ignores a degenerate path (fewer than two waypoints)', () => {
    expect(anim.walk('p', [pt(0, 0)])).toBe(0);
    expect(anim.positions().size).toBe(0);
  });

  it('holds the pawn at its start immediately, then walks and settles (clears) at the end', () => {
    const dur = anim.walk('p', [pt(0, 0), pt(100, 0)]);
    expect(dur).toBe(1000); // 100px at 0.1px/ms

    // Held at the start this frame so the server's final tile can't snap it there.
    expect(anim.positions().get('p')).toEqual({ x: 0, y: 0, ms: 0 });

    // After the release frames it moves to the waypoint with that step's duration.
    vi.advanceTimersByTime(50);
    expect(anim.positions().get('p')).toEqual({ x: 100, y: 0, ms: 1000 });

    // Once the step completes it settles onto the server tile (drops out of the map).
    vi.advanceTimersByTime(1000);
    expect(anim.positions().has('p')).toBe(false);
  });

  it('delays the start of the walk by delayMs (holding at the start meanwhile)', () => {
    anim.walk('p', [pt(0, 0), pt(100, 0)], 500);
    expect(anim.positions().get('p')).toEqual({ x: 0, y: 0, ms: 0 });

    vi.advanceTimersByTime(50); // before the delay elapses — still held at the start
    expect(anim.positions().get('p')).toEqual({ x: 0, y: 0, ms: 0 });

    vi.advanceTimersByTime(500); // delay elapsed → it starts moving
    expect(anim.positions().get('p')).toEqual({ x: 100, y: 0, ms: 1000 });
  });

  it('walks multi-segment paths and scales speed to the total distance', () => {
    // 300px total → 0.12px/ms tier → 2500ms; 500px → 0.16px/ms → ~3125ms.
    expect(anim.walk('a', [pt(0, 0), pt(300, 0)])).toBe(2500);
    expect(anim.walk('b', [pt(0, 0), pt(500, 0)])).toBe(3125);
  });
});
