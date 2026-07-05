import { test, expect } from '@playwright/test';
import { openBoard } from './support/steps';

// Rewrite of MobileLayoutCheck_IT: on the mobile layout the board, the roster and
// the action buttons must all be fully on screen and must not overlap one another,
// for every player count from 2 to 8. (The GWT test bound GWT ids + a testLayout()
// helper; here we read the real Angular elements' geometry.)

const COUNTS = [2, 3, 4, 5, 6, 7, 8];
const MOBILE = { width: 390, height: 844 };

test.describe('mobile layout (MobileLayoutCheck_IT)', () => {
  for (const n of COUNTS) {
    test(`${n} players — board, roster & buttons on screen, no overlap`, async ({ browser }) => {
      const { page } = await openBoard(browser, { players: n, viewport: MOBILE });

      const g = await page.evaluate(() => {
        const rj = (s: string) => {
          const el = document.querySelector(s);
          return el ? (el.getBoundingClientRect().toJSON() as DOMRect) : null;
        };
        return {
          board: rj('.board'),
          buttons: rj('.button-container'),
          roster: rj('app-player-list'),
          vw: window.innerWidth,
          vh: window.innerHeight,
        };
      });

      const T = 1;
      const onScreen = (r: DOMRect | null) =>
        !!r && r.top >= -T && r.left >= -T && r.right <= g.vw + T && r.bottom <= g.vh + T;
      const overlap = (a: DOMRect, b: DOMRect) =>
        !(a.right <= b.left + T || b.right <= a.left + T || a.bottom <= b.top + T || b.bottom <= a.top + T);

      expect(onScreen(g.board), 'board on screen').toBe(true);
      expect(onScreen(g.buttons), 'buttons on screen').toBe(true);
      expect(onScreen(g.roster), 'roster on screen').toBe(true);

      expect(overlap(g.board!, g.buttons!), 'board vs buttons').toBe(false);
      expect(overlap(g.board!, g.roster!), 'board vs roster').toBe(false);
      expect(overlap(g.buttons!, g.roster!), 'buttons vs roster').toBe(false);

      await page.context().close();
    });
  }
});
