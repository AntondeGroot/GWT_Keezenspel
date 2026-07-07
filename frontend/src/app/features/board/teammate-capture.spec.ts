import { teammateCaptureKeys } from './teammate-capture';

// 4-player team game: seats 0 & 2 = team 0, seats 1 & 3 = team 1.
const TEAMS: Record<string, number> = { '0': 0, '2': 0, '1': 1, '3': 1 };
const teamOf = (id: string) => TEAMS[id] ?? null;

describe('teammateCaptureKeys', () => {
  it('flags a preview tile holding a teammate’s pawn', () => {
    // mover is player 0 (team 0); player 2 (team 0) sits on a previewed tile
    const preview = new Set(['track:5']);
    const occupants = [{ key: 'track:5', ownerId: '2' }];
    expect([...teammateCaptureKeys(preview, occupants, teamOf, '0')]).toEqual(['track:5']);
  });

  it('does not flag capturing an opponent', () => {
    const preview = new Set(['track:5']);
    const occupants = [{ key: 'track:5', ownerId: '1' }]; // player 1 is team 1 — opponent
    expect(teammateCaptureKeys(preview, occupants, teamOf, '0').size).toBe(0);
  });

  it('ignores the mover’s own pawn', () => {
    const preview = new Set(['track:5']);
    const occupants = [{ key: 'track:5', ownerId: '0' }]; // the mover
    expect(teammateCaptureKeys(preview, occupants, teamOf, '0').size).toBe(0);
  });

  it('ignores teammate pawns not on a previewed tile', () => {
    const preview = new Set(['track:5']);
    const occupants = [{ key: 'track:9', ownerId: '2' }]; // teammate, but elsewhere
    expect(teammateCaptureKeys(preview, occupants, teamOf, '0').size).toBe(0);
  });

  it('returns empty when the mover has no team (teams off)', () => {
    const preview = new Set(['track:5']);
    const occupants = [{ key: 'track:5', ownerId: '2' }];
    expect(teammateCaptureKeys(preview, occupants, () => null, '0').size).toBe(0);
  });

  it('flags multiple teammate tiles and leaves opponents alone', () => {
    const preview = new Set(['track:5', 'track:8', 'track:11']);
    const occupants = [
      { key: 'track:5', ownerId: '2' }, // teammate → flag
      { key: 'track:8', ownerId: '3' }, // opponent → no
      { key: 'track:11', ownerId: '2' }, // teammate → flag
    ];
    expect([...teammateCaptureKeys(preview, occupants, teamOf, '0')].sort()).toEqual([
      'track:11',
      'track:5',
    ]);
  });
});
