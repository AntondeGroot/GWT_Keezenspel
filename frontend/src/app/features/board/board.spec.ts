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
      pile: { set: (v: Array<{ uuid: number; suit: number; value: number }>) => void };
      cards: () => Array<{ uuid: number; inPile: boolean }>;
    };
    const card = (uuid: number, suit: number, value: number) => ({ uuid, suit, value });
    const push = (cards: Array<{ uuid: number; suit: number; value: number }>) => ({
      playerCards: cards,
      players: [],
      pawns: [],
      winners: [],
    });

    // Round 1: three cards dealt, then played one by one (each lands in the pile as it flies).
    c.handleGameState(push([card(1, 0, 5), card(2, 0, 6), card(3, 0, 7)]));
    c.pile.set([card(1, 0, 5)]);
    c.handleGameState(push([card(2, 0, 6), card(3, 0, 7)]));
    c.pile.set([card(1, 0, 5), card(2, 0, 6)]);
    c.handleGameState(push([card(3, 0, 7)]));
    c.pile.set([card(1, 0, 5), card(2, 0, 6), card(3, 0, 7)]);
    c.handleGameState(push([])); // hand empty — round over

    // New round: the deck reuses uuids, so cards 1 and 2 return in a fresh hand.
    c.handleGameState(push([card(1, 1, 9), card(2, 1, 10)]));

    const shown = c
      .cards()
      .filter((x) => !x.inPile)
      .map((x) => x.uuid)
      .sort();
    expect(shown).toEqual([1, 2]); // both visible — pile was cleared on the new deal
  });
});
