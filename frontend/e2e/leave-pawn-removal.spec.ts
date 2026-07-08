import { test, expect, request } from '@playwright/test';
import { API_URL } from '../playwright.config';
import { openBoard } from './support/steps';
import { setPawn } from './support/seed';

// A player leaving mid-game removes their pawns from the server push. The board must drop those
// pawns cleanly (they're rendered straight from `state.pawns`) without a runtime error.
test('a player leaving mid-game removes their pawns from the board, no error', async ({ browser }) => {
  const errors: string[] = [];

  const { page, sessionId } = await openBoard(browser, {
    players: 3,
    as: 'player0',
    setup: async (api, s) => {
      await setPawn(api, s, 'player0', 0, 'player0', 3);
      await setPawn(api, s, 'player1', 0, 'player1', 5);
      await setPawn(api, s, 'player1', 1, 'player1', 7);
    },
  });
  page.on('pageerror', (e) => errors.push(`pageerror: ${e}`));
  page.on('console', (m) => {
    if (m.type() === 'error') errors.push(`console: ${m.text()}`);
  });

  // Both players' pawns render to start with.
  await expect(page.getByTestId('pawn-player0:0')).toBeVisible();
  await expect(page.getByTestId('pawn-player1:0')).toBeVisible();
  await expect(page.getByTestId('pawn-player1:1')).toBeVisible();

  // Player 1 leaves the game (DELETE /games/{session}/players/{player}).
  const api = await request.newContext({ baseURL: API_URL });
  const resp = await api.delete(`/games/${sessionId}/players/player1`);
  expect(resp.ok(), `leave request failed: ${resp.status()}`).toBeTruthy();
  await api.dispose();

  // Their pawns are removed via the SSE push; player 0's pawn stays; nothing crashed.
  await expect(page.getByTestId('pawn-player1:0')).toHaveCount(0);
  await expect(page.getByTestId('pawn-player1:1')).toHaveCount(0);
  await expect(page.getByTestId('pawn-player0:0')).toBeVisible();
  expect(errors, `runtime errors during pawn removal:\n${errors.join('\n')}`).toEqual([]);
});
