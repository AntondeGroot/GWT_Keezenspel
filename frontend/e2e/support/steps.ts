import { Page, expect } from '@playwright/test';

// A small Steps DSL over the Angular board — the Playwright equivalent of the GWT
// IntegrationTests `Steps`/`TestUtils` helpers, driving moves through the real UI
// (select card → select pawn(s) → Play card) and reading pawn positions.

export interface Pt {
  x: number;
  y: number;
}

/** Centre of a pawn on screen, by its `{playerId}:{pawnNr}` id. */
export async function pawnCentre(page: Page, id: string): Promise<Pt> {
  const box = await page.getByTestId(`pawn-${id}`).boundingBox();
  if (!box) throw new Error(`pawn ${id} not found`);
  return { x: box.x + box.width / 2, y: box.y + box.height / 2 };
}

export function dist(a: Pt, b: Pt): number {
  return Math.hypot(a.x - b.x, a.y - b.y);
}

/** Click a pawn (dispatchEvent so clustered nest pawns still get the exact one). */
export async function clickPawn(page: Page, id: string): Promise<void> {
  await page.getByTestId(`pawn-${id}`).dispatchEvent('click');
}

/** A pawn is "selected" when the board sets its --pawn-highlight (pawn1/pawn2). */
export async function isPawnSelected(page: Page, id: string): Promise<boolean> {
  return page
    .getByTestId(`pawn-${id}`)
    .evaluate((el) => getComputedStyle(el).getPropertyValue('--pawn-highlight').trim() !== '');
}

/**
 * Wait until a pawn stops moving (its centre is unchanged across two polls) —
 * the equivalent of the GWT `waitUntilPawnStopsMoving`.
 */
export async function waitPawnSettled(page: Page, id: string, timeout = 8000): Promise<Pt> {
  let prev: Pt | null = null;
  let stableReads = 0;
  const started = Date.now();
  while (Date.now() - started < timeout) {
    const now = await pawnCentre(page, id);
    if (prev && dist(prev, now) < 1) {
      if (++stableReads >= 2) return now;
    } else {
      stableReads = 0;
    }
    prev = now;
    await page.waitForTimeout(120);
  }
  return prev ?? (await pawnCentre(page, id));
}

/**
 * Play the hand card of `value` on the given pawn(s), then click Play card.
 * Uses dispatchEvent so overlapping nest pawns still receive the click reliably.
 */
export async function playCard(
  page: Page,
  opts: { value: number; pawns: string[] },
): Promise<void> {
  await page.getByTestId(`card-${opts.value}`).dispatchEvent('click');
  for (const id of opts.pawns) {
    await page.getByTestId(`pawn-${id}`).dispatchEvent('click');
  }
  const play = page.locator('.send-button');
  await expect(play).toBeEnabled();
  await play.click();
}
