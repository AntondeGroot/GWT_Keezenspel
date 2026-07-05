import { test, expect, request } from '@playwright/test';
import { API_URL } from '../playwright.config';
import { createGame, setPawn, setCard, makeMove } from './support/seed';
import { viewAs, pawnCentre, dist } from './support/steps';

// Rewrite of JackAnimationFromOpponentPerspective_IT: player0 plays a jack switch
// via a direct API call (not the UI); the *observer* (player1) must receive it over
// SSE and animate both pawns until they have traded screen positions.

test.describe('jack switch animation from the observer (JackAnimation_IT)', () => {
  test('the observer sees both pawns swap after a jack played via API', async ({ browser }) => {
    const api = await request.newContext({ baseURL: API_URL });
    const { sessionId } = await createGame(api, 3);
    // Both pawns on player1's board section so the switch is valid; player0 holds the jack.
    await setPawn(api, sessionId, 'player1', 0, 'player1', 4);
    await setPawn(api, sessionId, 'player0', 0, 'player1', 2);
    await setCard(api, sessionId, 'player0', 11);

    // Observe as player1 (the non-acting player).
    const page = await viewAs(browser, sessionId, 'player1');
    const p0Before = await pawnCentre(page, 'player0:0');
    const p1Before = await pawnCentre(page, 'player1:0');

    // player0 plays the jack switch through the API; the browser only sees the SSE push.
    await makeMove(api, sessionId, 'player0', {
      cardId: 11,
      pawn1Id: { playerId: 'player0', pawnNr: 0 },
      pawn2Id: { playerId: 'player1', pawnNr: 0 },
    });
    await api.dispose();

    // Each pawn animates to the other's original tile.
    await expect
      .poll(async () => dist(await pawnCentre(page, 'player0:0'), p1Before), { timeout: 9000 })
      .toBeLessThan(12);
    await expect
      .poll(async () => dist(await pawnCentre(page, 'player1:0'), p0Before), { timeout: 9000 })
      .toBeLessThan(12);

    await page.context().close();
  });
});
