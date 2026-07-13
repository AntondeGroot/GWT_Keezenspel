import { test, expect } from '@playwright/test';
import { setOnlyCard, setPawn } from './support/seed';
import { openBoard, playCard, waitPawnSettled } from './support/steps';

// The team card-trade ("ask your teammate for a King/Ace") is only offerable until you play your
// first card of the round; a new deal reopens it. The rule lives in the backend (canRequestTrade)
// and the button mirrors it. Backend unit tests cover the rule; this asserts the end-to-end wiring.
test.describe('team card-trade window', () => {
  test('the ask button shows until you play your first card of the round', async ({ browser }) => {
    const { page } = await openBoard(browser, {
      players: 4,
      as: 'player0',
      gameOptions: { teamPlay: true, teamCardTrade: true },
      setup: async (api, s) => {
        await setPawn(api, s, 'player0', 0, 'player0', 5);
        await setOnlyCard(api, s, 'player0', 1); // a single Ace to play
      },
    });

    const askButton = page.locator('.ask-trade-button');
    await expect(askButton, 'trade window open before playing').toBeVisible();

    await playCard(page, { value: 1, pawns: ['player0:0'] });
    await waitPawnSettled(page, 'player0:0');

    await expect(askButton, 'trade window closed after the first play').toHaveCount(0);
    await page.context().close();
  });
});
