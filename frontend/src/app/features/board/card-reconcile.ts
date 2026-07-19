/**
 * Self-healing reconciliation between the server's authoritative hand and the local discard pile.
 *
 * The displayed hand is `playerCards` MINUS the local pile (see card-table): a played card sits in
 * the pile so it doesn't also show in the fan. But card uuids are REUSED across rounds, so a pile
 * entry left over from an earlier round (e.g. one that survived an SSE drop/reconnect landing
 * mid-round) can share a uuid with a card you genuinely hold NOW — shadowing it, making it INVISIBLE
 * and unplayable. That's the "I only saw 2 of my 4 cards" / "invisible queen I could still play" bug.
 *
 * This runs on every push and enforces the invariant: a card the server still lists in your hand may
 * NOT sit in your pile — EXCEPT one you just played that the server hasn't dropped from the hand yet
 * (`justPlayed`), which is the legitimate one-beat transient the pile-filter exists for.
 *
 * Separately, `serverCount` (nrOfCardsPerPlayer[viewer]) is authoritative: if the push's hand list is
 * shorter than that count, the hand data itself arrived short (a lost/partial update) and no pile
 * edit can recover it — the caller should re-pull a fresh snapshot (`needsResync`).
 */
export interface HasUuid {
  uuid: number;
}

export interface PileReconcileResult<P extends HasUuid> {
  /** The pile with stale shadows removed (identity-preserving filter of the input pile). */
  pile: P[];
  /** `justPlayed` pruned to cards the server still lists in the hand (the rest are settled). */
  justPlayed: Set<number>;
  /** True when the push's hand is shorter than the server's count — re-pull a fresh snapshot. */
  needsResync: boolean;
}

export function reconcilePile<P extends HasUuid>(input: {
  hand: readonly HasUuid[];
  pile: readonly P[];
  serverCount: number | undefined;
  justPlayed: ReadonlySet<number>;
}): PileReconcileResult<P> {
  const handUuids = new Set(input.hand.map((c) => c.uuid));

  // Drop any pile entry whose uuid is back in the authoritative hand (a stale shadow), unless it's
  // a card you just played and the server hasn't caught up on yet.
  const pile = input.pile.filter((c) => !handUuids.has(c.uuid) || input.justPlayed.has(c.uuid));

  // Once the server drops a just-played card from your hand, its transient protection is done.
  const justPlayed = new Set([...input.justPlayed].filter((u) => handUuids.has(u)));

  const needsResync = input.serverCount !== undefined && input.hand.length < input.serverCount;

  return { pile, justPlayed, needsResync };
}

/**
 * Stateful wrapper around {@link reconcilePile} that owns the just-played set and the resync guard,
 * so the board keeps the hand in sync in a single call per push. Writes the shortened pile back to
 * the given signal (only when it actually shrank) and invokes `resync` at most once per short-push
 * episode. `markPlayed` protects a card the viewer just played; `reset` clears that protection on a
 * new round or reconnect (last round's plays must not protect this round's reused uuids).
 */
export class HandReconciler {
  private justPlayed = new Set<number>();
  private resyncing = false;

  markPlayed(uuid: number): void {
    this.justPlayed.add(uuid);
  }

  // Only the just-played protection resets here. The resync guard deliberately survives: a resync
  // itself reconnects the stream (which calls reset), so clearing it here would let the next short
  // push resync again — a reconnect storm. It is cleared instead when a whole push arrives (below).
  reset(): void {
    this.justPlayed.clear();
  }

  reconcile<P extends HasUuid>(
    hand: readonly HasUuid[],
    pile: { (): P[]; set(value: P[]): void },
    serverCount: number | undefined,
    resync: () => void,
  ): void {
    const current = pile();
    const result = reconcilePile({ hand, pile: current, serverCount, justPlayed: this.justPlayed });
    this.justPlayed = result.justPlayed;
    if (result.pile.length !== current.length) pile.set(result.pile);
    if (!result.needsResync) {
      this.resyncing = false;
    } else if (!this.resyncing) {
      this.resyncing = true;
      resync();
    }
  }
}
