import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { Board } from './board';

// jsdom has no EventSource, so drive the board's REAL game-state stream with a controllable fake
// (same shape as game-state-stream.spec). This lets us reproduce an SSE drop + reconnect end-to-end
// through the board — the timing-dependent path a mocked-browser e2e can't exercise reliably — and
// assert the card desync heals. The board resolves its session from cookies (see session.ts), so we
// set those before creating the component so ngOnInit actually opens the stream.
class FakeEventSource {
  static readonly CLOSED = 2;
  static readonly instances: FakeEventSource[] = [];
  readyState = 0; // CONNECTING
  onerror: (() => void) | null = null;
  private readonly listeners: Record<string, ((e: MessageEvent) => void)[]> = {};

  constructor(readonly url: string) {
    FakeEventSource.instances.push(this);
  }
  addEventListener(type: string, cb: (e: MessageEvent) => void): void {
    this.listeners[type] ??= [];
    this.listeners[type].push(cb);
  }
  close(): void {
    this.readyState = FakeEventSource.CLOSED;
  }
  /** Deliver a server event. */
  emit(type: string, data: unknown): void {
    (this.listeners[type] ?? []).forEach((cb) =>
      cb({ data: JSON.stringify(data) } as MessageEvent),
    );
  }
  /** Simulate a transport error at a given readyState. */
  fail(readyState: number): void {
    this.readyState = readyState;
    this.onerror?.();
  }
}

interface CardFixture {
  uuid: number;
  suit: number;
  value: number;
}

describe('Board — SSE reconnect card sync', () => {
  let fixture: ComponentFixture<Board>;
  let component: Board;
  const env = globalThis as Record<string, unknown>;
  let originalES: unknown;

  // A valid 4-player roster (contiguous playerInts) so the board geometry renders without crashing.
  const PLAYERS = [0, 1, 2, 3].map((i) => ({ id: `p${i}`, name: `P${i}`, playerInt: i }));
  const card = (uuid: number): CardFixture => ({ uuid, suit: 0, value: 5 });
  const push = (cards: CardFixture[], nrOfCardsPerPlayer?: Record<string, number>) => ({
    currentPlayerId: 'p0',
    players: PLAYERS,
    pawns: [],
    winners: [],
    version: 1,
    playerCards: cards,
    ...(nrOfCardsPerPlayer ? { nrOfCardsPerPlayer } : {}),
  });

  const es = () => FakeEventSource.instances.at(-1)!;
  const visible = () => {
    const table = (
      component as unknown as { cardTable: { cards: () => { uuid: number; inPile: boolean }[] } }
    ).cardTable;
    return table
      .cards()
      .filter((c) => !c.inPile)
      .map((c) => c.uuid)
      .sort((a, b) => a - b);
  };
  const setPile = (cards: CardFixture[]) =>
    (
      component as unknown as { cardTable: { pile: { set: (v: CardFixture[]) => void } } }
    ).cardTable.pile.set(cards);

  beforeEach(async () => {
    document.cookie = 'sessionid=s1';
    document.cookie = 'playerid=p0';
    FakeEventSource.instances.length = 0;
    originalES = env['EventSource'];
    env['EventSource'] = FakeEventSource;
    vi.useFakeTimers();

    await TestBed.configureTestingModule({ imports: [Board] }).compileComponents();
    fixture = TestBed.createComponent(Board);
    component = fixture.componentInstance;
    fixture.detectChanges(); // runs ngOnInit → opens the stream (first FakeEventSource)
  });

  afterEach(() => {
    vi.useRealTimers();
    env['EventSource'] = originalES;
    document.cookie = 'sessionid=; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    document.cookie = 'playerid=; expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  it('clears a stale pile on reconnect so a colliding hand snapshot stays fully visible', () => {
    expect(FakeEventSource.instances).toHaveLength(1); // stream opened on init

    // Round 1: the viewer holds cards 1, 2, 3.
    es().emit('gamestate', push([card(1), card(2), card(3)]));
    expect(visible()).toEqual([1, 2, 3]);

    // Card 1 lands on the discard pile (played), leaving 2 and 3 in hand.
    setPile([card(1)]);
    expect(visible()).toEqual([2, 3]);

    // The stream drops (CLOSED) and our code reconnects after the backoff.
    es().fail(FakeEventSource.CLOSED);
    vi.advanceTimersByTime(2000);
    expect(FakeEventSource.instances).toHaveLength(2); // reopened

    // The reconnect delivers a fresh snapshot whose hand REUSES uuid 1 (a new round). Before the fix
    // the surviving pile shadowed it (card 1 invisible); now onReconnect cleared the pile, so all
    // three cards are visible.
    es().emit('gamestate', push([card(1), card(9), card(10)]));
    expect(visible()).toEqual([1, 9, 10]);
  });

  it('re-pulls a fresh snapshot when the hand arrives shorter than the server card count', () => {
    expect(FakeEventSource.instances).toHaveLength(1);

    // Server says the viewer holds 4 cards, but the push only carried 2 (a lost/partial update):
    // the board re-pulls, opening a new stream connection.
    es().emit('gamestate', push([card(1), card(2)], { p0: 4 }));
    expect(FakeEventSource.instances).toHaveLength(2);

    // A second still-short push must NOT re-pull again — the guard survives the reconnect it caused,
    // so there's no reconnect storm.
    es().emit('gamestate', push([card(1), card(2)], { p0: 4 }));
    expect(FakeEventSource.instances).toHaveLength(2);

    // A whole push clears the guard, so a later shortfall can re-pull once more.
    es().emit('gamestate', push([card(1), card(2), card(3), card(4)], { p0: 4 }));
    es().emit('gamestate', push([card(1), card(2)], { p0: 4 }));
    expect(FakeEventSource.instances).toHaveLength(3);
  });
});
