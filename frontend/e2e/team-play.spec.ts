import { test, expect, Browser, APIRequestContext } from '@playwright/test';
import { setOnlyCard, setPawn } from './support/seed';
import {
  openBoard,
  clickPawn,
  isPawnSelected,
  playCard,
  pawnCentre,
  waitPawnSettled,
  dist,
} from './support/steps';

// Team play (steps 4/5): once ALL your own pawns are home you take over your teammate's pawns.
// Teams are opposite seats and /test/start-game does NOT shuffle, so player0's teammate is
// player2. These drive the real Angular board to confirm you can select, move and switch a
// teammate's pawns — including the edge positions (its start tile, and the finish).

const FINISH = [16, 17, 18, 19];

/** A team game where player0's own four pawns are all home, so they may play player2's pawns. */
async function teamGame(
  browser: Browser,
  scenario: (api: APIRequestContext, sessionId: string) => Promise<void>,
) {
  return openBoard(browser, {
    players: 4,
    as: 'player0',
    gameOptions: { teamPlay: true },
    setup: async (api, s) => {
      for (let n = 0; n < 4; n++) await setPawn(api, s, 'player0', n, 'player0', FINISH[n]);
      await scenario(api, s);
    },
  });
}

test.describe('team play — controlling your teammate’s pawns', () => {
  test('can select a teammate pawn on its start tile and in the finish', async ({ browser }) => {
    const { page } = await teamGame(browser, async (api, s) => {
      await setPawn(api, s, 'player2', 0, 'player2', 0); // teammate pawn on its start tile
      await setPawn(api, s, 'player2', 1, 'player2', 16); // teammate pawn in the finish
    });

    await clickPawn(page, 'player2:0');
    await expect.poll(() => isPawnSelected(page, 'player2:0')).toBe(true);

    await clickPawn(page, 'player2:1');
    await expect.poll(() => isPawnSelected(page, 'player2:1')).toBe(true);

    await page.context().close();
  });

  test('can move a teammate pawn from its start tile', async ({ browser }) => {
    const { page } = await teamGame(browser, async (api, s) => {
      await setPawn(api, s, 'player2', 0, 'player2', 0); // start tile
      await setOnlyCard(api, s, 'player0', 5); // your card moves it 5 steps
    });

    const before = await pawnCentre(page, 'player2:0');
    await playCard(page, { value: 5, pawns: ['player2:0'] });
    const after = await waitPawnSettled(page, 'player2:0');

    expect(dist(before, after), 'teammate pawn advanced from its start tile').toBeGreaterThan(5);
    await page.context().close();
  });

  test('can switch a teammate pawn on its start tile with a Jack', async ({ browser }) => {
    const { page } = await teamGame(browser, async (api, s) => {
      await setPawn(api, s, 'player2', 0, 'player2', 0); // teammate pawn on its start tile
      await setPawn(api, s, 'player1', 0, 'player1', 5); // an opponent pawn on the board
      await setOnlyCard(api, s, 'player0', 11); // Jack
    });

    const mateBefore = await pawnCentre(page, 'player2:0');
    const oppBefore = await pawnCentre(page, 'player1:0');

    await playCard(page, { value: 11, pawns: ['player2:0', 'player1:0'] });
    const mateAfter = await waitPawnSettled(page, 'player2:0');
    const oppAfter = await waitPawnSettled(page, 'player1:0');

    // The Jack swapped the two pawns' positions.
    expect(dist(mateBefore, mateAfter), 'teammate pawn switched off its start').toBeGreaterThan(5);
    expect(dist(oppBefore, oppAfter), 'opponent pawn switched').toBeGreaterThan(5);
    await page.context().close();
  });
});
