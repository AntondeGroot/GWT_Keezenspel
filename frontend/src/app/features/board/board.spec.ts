import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Board } from './board';

describe('Board', () => {
  let component: Board;
  let fixture: ComponentFixture<Board>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Board],
    }).compileComponents();

    fixture = TestBed.createComponent(Board);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Card uuids are reused across rounds; the client pile must be cleared on a new deal or a
  // redealt card (whose uuid lingered in the pile) gets filtered out of the hand and vanishes.
  it('clears the pile on a new round so a redealt card is not filtered out of the hand', () => {
    const c = component as unknown as {
      handleGameState: (push: unknown) => void;
      cardTable: {
        pile: { set: (v: { uuid: number; suit: number; value: number }[]) => void };
        cards: () => { uuid: number; inPile: boolean }[];
      };
    };
    const card = (uuid: number, suit: number, value: number) => ({ uuid, suit, value });
    const push = (cards: { uuid: number; suit: number; value: number }[]) => ({
      playerCards: cards,
      players: [],
      pawns: [],
      winners: [],
    });

    // Round 1: three cards dealt, then played one by one (each lands in the pile as it flies).
    c.handleGameState(push([card(1, 0, 5), card(2, 0, 6), card(3, 0, 7)]));
    c.cardTable.pile.set([card(1, 0, 5)]);
    c.handleGameState(push([card(2, 0, 6), card(3, 0, 7)]));
    c.cardTable.pile.set([card(1, 0, 5), card(2, 0, 6)]);
    c.handleGameState(push([card(3, 0, 7)]));
    c.cardTable.pile.set([card(1, 0, 5), card(2, 0, 6), card(3, 0, 7)]);
    c.handleGameState(push([])); // hand empty — round over

    // New round: the deck reuses uuids, so cards 1 and 2 return in a fresh hand.
    c.handleGameState(push([card(1, 1, 9), card(2, 1, 10)]));

    const shown = c.cardTable
      .cards()
      .filter((x) => !x.inPile)
      .map((x) => x.uuid)
      .sort();
    expect(shown).toEqual([1, 2]); // both visible — pile was cleared on the new deal
  });

  // Reproduces the SSE drop/reconnect desync: a stale pile entry (uuid reused across rounds) shadows
  // a card the server says you still hold, so it goes invisible/unplayable. On reconnect the deal
  // detection is skipped, so the new-round clearPile does NOT run — the reconciler must heal it on
  // the next authoritative push instead.
  it('heals a stale pile entry that shadows a held card without a new deal', () => {
    const c = component as unknown as {
      handleGameState: (push: unknown) => void;
      cardTable: {
        pile: { set: (v: { uuid: number; suit: number; value: number }[]) => void };
        cards: () => { uuid: number; inPile: boolean }[];
      };
    };
    const card = (uuid: number, suit: number, value: number) => ({ uuid, suit, value });
    const push = (cards: { uuid: number; suit: number; value: number }[]) => ({
      playerCards: cards,
      players: [],
      pawns: [],
      winners: [],
      nrOfCardsPerPlayer: {}, // no viewer id resolved in the test → count is not consulted
    });
    const visible = () =>
      c.cardTable
        .cards()
        .filter((x) => !x.inPile)
        .map((x) => x.uuid)
        .sort();

    // Steady state: the viewer holds cards 1 and 3.
    c.handleGameState(push([card(1, 0, 5), card(3, 0, 7)]));

    // The desync a reconnect can leave behind: card 1 wrongly lingers in the local pile, so it's
    // filtered out of the displayed hand and disappears — the reported bug.
    c.cardTable.pile.set([card(1, 0, 5)]);
    expect(visible()).toEqual([3]); // bug state: card 1 invisible though the server says you hold it

    // The next push carries the SAME hand (no growth), so the new-round reset does not fire; the
    // reconciler alone must drop the stale pile entry and bring card 1 back.
    c.handleGameState(push([card(1, 0, 5), card(3, 0, 7)]));
    expect(visible()).toEqual([1, 3]); // healed — the held card is visible again
  });
});
