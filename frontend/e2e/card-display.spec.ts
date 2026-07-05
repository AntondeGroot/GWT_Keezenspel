import { test, expect } from '@playwright/test';
import { openBoard, viewAs, forfeit, handCards, pileCards } from './support/steps';

// Rewrite of CardDisplay_IT: the viewer's hand renders, forfeiting empties it and
// moves the cards to the pile, and the next player still sees their own hand.

test.describe('card display (CardDisplay_IT)', () => {
  test("the viewer's hand cards render", async ({ browser }) => {
    const { page } = await openBoard(browser);
    await expect(handCards(page)).toHaveCount(5);
    await page.context().close();
  });

  test('forfeiting moves the whole hand to the pile', async ({ browser }) => {
    const { page } = await openBoard(browser);
    const before = await handCards(page).count();
    expect(before).toBeGreaterThan(0);

    await forfeit(page);

    await expect(handCards(page)).toHaveCount(0);
    await expect(pileCards(page)).toHaveCount(before);
    await page.context().close();
  });

  test('after a forfeit the next player still sees their own hand', async ({ browser }) => {
    const { page, sessionId } = await openBoard(browser);
    await forfeit(page);
    await page.context().close();

    const p1 = await viewAs(browser, sessionId, 'player1');
    await expect(handCards(p1)).toHaveCount(5);
    await p1.context().close();
  });
});
