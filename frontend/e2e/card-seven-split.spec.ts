import { test, expect } from '@playwright/test';
import { setOnlyCard, setPawn } from './support/seed';
import { openBoard, clickPawn } from './support/steps';

// Rewrite of CardSevenSplit_IT: the step-box/model desync regression. After the
// user edits the split steps, re-selecting the 7 must snap the boxes back to the
// model defaults — not leave the stale user values on screen while the model has
// reset (which caused the send button to play the wrong split).

test.describe('7-split step boxes (CardSevenSplit_IT)', () => {
  test('re-selecting the 7 resets the split step boxes to their defaults', async ({ browser }) => {
    const { page } = await openBoard(browser, {
      setup: async (api, s) => {
        await setPawn(api, s, 'player0', 0, 'player0', 1);
        await setPawn(api, s, 'player0', 1, 'player0', 2);
        await setOnlyCard(api, s, 'player0', 7);
      },
    });

    const card7 = page.getByTestId('card-7');
    const box1 = page.locator('.pawn-steps__input').first();
    const box2 = page.locator('.pawn-steps__input').nth(1);

    // Select 7 + both pawns → the split step boxes appear at the model default.
    await card7.dispatchEvent('click');
    await clickPawn(page, 'player0:0');
    await clickPawn(page, 'player0:1');
    await expect(box1).toBeVisible();

    const d1 = await box1.inputValue();
    const d2 = await box2.inputValue();
    expect(Number(d1) + Number(d2)).toBe(7); // a 7-split always totals 7

    // The user changes step 1 → step 2 recomputes; the boxes now differ from default.
    await box1.fill('3');
    await box1.blur();
    await expect(box1).toHaveValue('3');
    await expect(box2).toHaveValue('4');

    // Re-select the 7 (deselect, then select) → the boxes snap back to the default.
    await card7.dispatchEvent('click');
    await card7.dispatchEvent('click');
    await expect(box1).toHaveValue(d1);
    await expect(box2).toHaveValue(d2);
    await page.context().close();
  });
});
