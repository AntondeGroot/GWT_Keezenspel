import { describe, expect, it, vi } from 'vitest';
import { reconcilePile, HandReconciler } from './card-reconcile';

const card = (uuid: number) => ({ uuid });

// A tiny signal-like stand-in matching the callable `{ (): T[]; set(v) }` the reconciler writes to.
const pileSignal = (initial: { uuid: number }[]) => {
  let value = initial;
  const sig = () => value;
  sig.set = (v: { uuid: number }[]) => {
    value = v;
  };
  return sig;
};

describe('HandReconciler', () => {
  it('removes a stale shadow from the pile signal on reconcile', () => {
    const r = new HandReconciler();
    const pile = pileSignal([card(5)]);

    r.reconcile([card(5), card(6)], pile, 2, vi.fn());

    expect(pile().map((c) => c.uuid)).toEqual([]); // card 5 un-shadowed
  });

  it('protects a just-played card until the server drops it, then lets it settle', () => {
    const r = new HandReconciler();
    r.markPlayed(5);
    const pile = pileSignal([card(5)]);

    r.reconcile([card(5), card(6)], pile, 2, vi.fn()); // server still lists 5
    expect(pile().map((c) => c.uuid)).toEqual([5]); // kept

    r.reconcile([card(6)], pile, 1, vi.fn()); // server dropped 5
    expect(pile().map((c) => c.uuid)).toEqual([5]); // stays on pile as a genuine discard
  });

  it('resyncs at most once per short-push episode and recovers when the hand is whole again', () => {
    const r = new HandReconciler();
    const pile = pileSignal([]);
    const resync = vi.fn();

    r.reconcile([card(6)], pile, 4, resync); // short: 1 of 4
    r.reconcile([card(6)], pile, 4, resync); // still short → must NOT resync again
    expect(resync).toHaveBeenCalledTimes(1);

    r.reconcile([card(6), card(7), card(8), card(9)], pile, 4, resync); // whole → clears the guard
    r.reconcile([card(6)], pile, 4, resync); // short again → resyncs once more
    expect(resync).toHaveBeenCalledTimes(2);
  });

  it('does not resync again after reset() (the reconnect a resync triggers must not storm)', () => {
    const r = new HandReconciler();
    const pile = pileSignal([]);
    const resync = vi.fn();

    r.reconcile([card(6)], pile, 4, resync); // short → resync #1 sets the guard
    r.reset(); // the resync reconnected the stream, whose onReconnect calls reset()
    r.reconcile([card(6)], pile, 4, resync); // still short — guard must survive the reset

    expect(resync).toHaveBeenCalledTimes(1);
  });

  it('reset() drops just-played protection so a reused uuid is treated as a fresh shadow', () => {
    const r = new HandReconciler();
    r.markPlayed(5);
    r.reset();
    const pile = pileSignal([card(5)]);

    r.reconcile([card(5), card(6)], pile, 2, vi.fn());
    expect(pile().map((c) => c.uuid)).toEqual([]); // no longer protected → removed
  });
});

describe('reconcilePile', () => {
  it('drops a stale pile entry that shadows a card back in the hand', () => {
    // uuid 5 was played last round and lingers in the pile; this round it was redealt.
    const result = reconcilePile({
      hand: [card(5), card(6)],
      pile: [card(5)],
      serverCount: 2,
      justPlayed: new Set<number>(),
    });

    expect(result.pile.map((c) => c.uuid)).toEqual([]); // shadow removed → card 5 becomes visible
    expect(result.needsResync).toBe(false);
  });

  it('keeps a just-played card in the pile while the server still lists it in the hand', () => {
    // You played 5 this beat; the next push has not dropped it from playerCards yet.
    const result = reconcilePile({
      hand: [card(5), card(6)],
      pile: [card(5)],
      serverCount: 2,
      justPlayed: new Set([5]),
    });

    expect(result.pile.map((c) => c.uuid)).toEqual([5]); // protected → stays on the pile
    expect(result.justPlayed.has(5)).toBe(true); // still awaiting the server to drop it
  });

  it('prunes justPlayed once the server drops the card from the hand', () => {
    const result = reconcilePile({
      hand: [card(6)], // 5 is gone from the hand now
      pile: [card(5)],
      serverCount: 1,
      justPlayed: new Set([5]),
    });

    expect(result.pile.map((c) => c.uuid)).toEqual([5]); // genuinely played → remains on the pile
    expect(result.justPlayed.has(5)).toBe(false); // settled → no longer protected
  });

  it('leaves genuine discards (pile cards not in the hand) untouched', () => {
    const result = reconcilePile({
      hand: [card(6)],
      pile: [card(1), card(2)], // opponents' / earlier plays this round
      serverCount: 1,
      justPlayed: new Set<number>(),
    });

    expect(result.pile.map((c) => c.uuid)).toEqual([1, 2]);
    expect(result.needsResync).toBe(false);
  });

  it('flags needsResync when the hand arrived shorter than the server count', () => {
    // Server says the viewer holds 4, but the push only carried 2 (a lost/partial update).
    const result = reconcilePile({
      hand: [card(6), card(7)],
      pile: [],
      serverCount: 4,
      justPlayed: new Set<number>(),
    });

    expect(result.needsResync).toBe(true);
  });

  it('does not flag resync when the count is absent', () => {
    const result = reconcilePile({
      hand: [card(6)],
      pile: [],
      serverCount: undefined,
      justPlayed: new Set<number>(),
    });

    expect(result.needsResync).toBe(false);
  });

  it('does not flag resync when a just-played card explains the count gap', () => {
    // hand still lists the just-played card, so hand.length matches the count — no real shortfall.
    const result = reconcilePile({
      hand: [card(5), card(6)],
      pile: [card(5)],
      serverCount: 2,
      justPlayed: new Set([5]),
    });

    expect(result.needsResync).toBe(false);
  });
});
