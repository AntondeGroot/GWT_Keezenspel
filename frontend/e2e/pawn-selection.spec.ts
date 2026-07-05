import { test, expect, request, Browser, Page } from '@playwright/test';
import { API_URL } from '../playwright.config';
import { createGame } from './support/seed';
import { clickPawn, isPawnSelected } from './support/steps';

// Rewrite of Pawn_IT's selection cases: clicking a pawn highlights it, a second
// own pawn steals the selection, and an opponent's pawn cannot be selected.

const UI = 'http://localhost:4300';

async function openGame(browser: Browser): Promise<Page> {
  const api = await request.newContext({ baseURL: API_URL });
  const { sessionId } = await createGame(api, 3);
  await api.dispose();

  const ctx = await browser.newContext();
  await ctx.addCookies([{ name: 'playerid', value: 'player0', url: UI }]);
  const page = await ctx.newPage();
  await page.goto(`/?sessionid=${sessionId}&playerid=player0`);
  await page.waitForSelector('app-card.card', { timeout: 30000 });
  return page;
}

test.describe('pawn selection (Pawn_IT)', () => {
  test('clicking your own pawn selects it', async ({ browser }) => {
    const page = await openGame(browser);
    await clickPawn(page, 'player0:0');
    expect(await isPawnSelected(page, 'player0:0')).toBe(true);
    await page.context().close();
  });

  test('selecting a second own pawn deselects the first', async ({ browser }) => {
    const page = await openGame(browser);
    await clickPawn(page, 'player0:1');
    await clickPawn(page, 'player0:2');
    expect(await isPawnSelected(page, 'player0:1')).toBe(false);
    expect(await isPawnSelected(page, 'player0:2')).toBe(true);
    await page.context().close();
  });

  test("an opponent's pawn cannot be selected", async ({ browser }) => {
    const page = await openGame(browser);
    await clickPawn(page, 'player1:1');
    expect(await isPawnSelected(page, 'player1:1')).toBe(false);
    await page.context().close();
  });
});
