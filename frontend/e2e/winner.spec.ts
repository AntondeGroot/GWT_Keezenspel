import { test, expect, request, Page } from '@playwright/test';
import { API_URL } from '../playwright.config';
import { createGame, setPawn, setOnlyCard, makeMove } from './support/seed';
import { viewAs, forfeit } from './support/steps';

// Rewrite of Winner_IT + Winner2Players_IT: a player who brings all four pawns home
// gets a medal (place 1/2/3) in the roster and is marked finished, and a winner
// banner announces it. Winner2Players guards a bug where the wrong colour got the medal.

type Api = Awaited<ReturnType<typeof request.newContext>>;

/** Park a player one Ace-step from home: 3 pawns in the finish (17/18/19), the 4th
 *  on the previous section's tile 15. Playing the Ace sends the last one home → win. */
async function setupWin(
  api: Api,
  s: string,
  playerId: string,
  prevPlayerId: string,
): Promise<void> {
  await setPawn(api, s, playerId, 0, playerId, 19);
  await setPawn(api, s, playerId, 1, playerId, 18);
  await setPawn(api, s, playerId, 2, playerId, 17);
  await setPawn(api, s, playerId, 3, prevPlayerId, 15);
  await setOnlyCard(api, s, playerId, 1);
}
const winMove = (api: Api, s: string, playerId: string) =>
  makeMove(api, s, playerId, { cardId: 1, pawn1Id: { playerId, pawnNr: 3 }, stepsPawn1: 1 });

/** Read a player's roster chip (medal text + finished/dimmed state) by name. */
function chip(
  page: Page,
  name: string,
): Promise<{ medal: string | null; inactive: boolean } | null> {
  return page.evaluate((n) => {
    const c = [...document.querySelectorAll('.chip')].find(
      (el) => el.querySelector('.chip__name')?.textContent?.trim() === n,
    );
    return c
      ? {
          medal: c.querySelector('.chip__medal')?.textContent?.trim() || null,
          inactive: c.classList.contains('chip--inactive'),
        }
      : null;
  }, name);
}
const medalOf = (page: Page, name: string) => chip(page, name).then((c) => c?.medal ?? null);

test.describe('winners & medals (Winner_IT / Winner2Players_IT)', () => {
  test('2 players: the winner gets gold, is marked finished, and is announced', async ({
    browser,
  }) => {
    const api = await request.newContext({ baseURL: API_URL });
    const { sessionId } = await createGame(api, 2);
    await setupWin(api, sessionId, 'player0', 'player1');
    const page = await viewAs(browser, sessionId, 'player0');

    await winMove(api, sessionId, 'player0');
    await api.dispose();

    await expect(page.getByTestId('winner-banner')).toBeVisible();
    await expect.poll(() => medalOf(page, 'player0')).toBe('🥇');
    expect((await chip(page, 'player0'))!.inactive).toBe(true);
    expect(await medalOf(page, 'player1')).toBeNull();
    await page.context().close();
  });

  test('2 players: when player 1 wins, player 1 gets the medal — not player 0', async ({
    browser,
  }) => {
    const api = await request.newContext({ baseURL: API_URL });
    const { sessionId } = await createGame(api, 2);
    const page = await viewAs(browser, sessionId, 'player0');
    await forfeit(page); // player 0 passes → player 1 has the turn

    await setupWin(api, sessionId, 'player1', 'player0');
    await winMove(api, sessionId, 'player1');
    await api.dispose();

    await expect.poll(() => medalOf(page, 'player1')).toBe('🥇');
    expect((await chip(page, 'player1'))!.inactive).toBe(true);
    expect(await medalOf(page, 'player0')).toBeNull();
    await page.context().close();
  });

  test('3 players: medals are awarded in finishing order (gold then silver)', async ({
    browser,
  }) => {
    const api = await request.newContext({ baseURL: API_URL });
    const { sessionId } = await createGame(api, 3);
    await setupWin(api, sessionId, 'player0', 'player2');
    const page = await viewAs(browser, sessionId, 'player0');

    await winMove(api, sessionId, 'player0'); // finishes 1st
    await expect.poll(() => medalOf(page, 'player0')).toBe('🥇');

    // player 0 is out; the turn passes to player 1, who now finishes 2nd.
    await setupWin(api, sessionId, 'player1', 'player0');
    await winMove(api, sessionId, 'player1');
    await api.dispose();

    await expect.poll(() => medalOf(page, 'player1')).toBe('🥈');
    expect(await medalOf(page, 'player0')).toBe('🥇');
    await page.context().close();
  });
});
