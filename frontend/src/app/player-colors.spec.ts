import { seatColor, teamAccent } from './player-colors';

describe('seatColor', () => {
  it('maps a seat index to its palette colour', () => {
    expect(seatColor(0)).toBe('#A52A2A');
    expect(seatColor(1)).toBe('#1a6fbd');
  });

  it('falls back to grey when the seat is missing or out of range', () => {
    expect(seatColor(null)).toBe('#8a8a8a');
    expect(seatColor(undefined)).toBe('#8a8a8a');
    expect(seatColor(99)).toBe('#8a8a8a');
  });
});

describe('teamAccent', () => {
  it('maps teamId 0..3 to the roster accents (orange, blue, green, pink)', () => {
    expect(teamAccent(0)).toBe('#f0932a');
    expect(teamAccent(1)).toBe('#4da3e8');
    expect(teamAccent(2)).toBe('#35be83');
    expect(teamAccent(3)).toBe('#ee7ab6');
  });

  it('returns undefined when the player has no team', () => {
    expect(teamAccent(null)).toBeUndefined();
    expect(teamAccent(undefined)).toBeUndefined();
  });

  it('returns undefined for an out-of-range team', () => {
    expect(teamAccent(4)).toBeUndefined();
  });
});
