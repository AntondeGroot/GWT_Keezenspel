import { test, expect } from '@playwright/test';
import { setOnlyCard, setPawn } from './support/seed';
import { openBoard, pawnCentre, waitPawnSettled, playCard, dist } from './support/steps';

// New E2E coverage (NOT a migration) for card plays the GWT browser suite never drove
// through the UI — see TEST_MIGRATION_CHECKLIST.md "Coverage gaps (beyond migration)".
// The game logic is also covered by backend Java unit tests (MovingAndKillTest etc.);
// these assert the same behaviours end-to-end through the Angular board.

test.describe('card plays (coverage gaps)', () => {
  test('capture: landing on an opponent pawn sends it home', async ({ browser }) => {
    // player0's pawn at its own section tile 9; player1's pawn one tile ahead (tile 10).
    // An Ace advances player0 one step onto player1 → player1 is sent back to its nest.
    // (Mirrors MovingAndKillTest.KillPawnOnNormalTile_Forward.)
    const { page } = await openBoard(browser, {
      players: 3,
      as: 'player0',
      setup: async (api, s) => {
        await setPawn(api, s, 'player0', 0, 'player0', 9);
        await setPawn(api, s, 'player1', 0, 'player0', 10);
        await setOnlyCard(api, s, 'player0', 1);
      },
    });

    const ownBefore = await pawnCentre(page, 'player0:0');
    const foeBefore = await pawnCentre(page, 'player1:0');

    await playCard(page, { value: 1, pawns: ['player0:0'] });
    const ownAfter = await waitPawnSettled(page, 'player0:0');
    const foeAfter = await waitPawnSettled(page, 'player1:0');

    expect(dist(ownBefore, ownAfter), 'own pawn advanced onto the opponent').toBeGreaterThan(5);
    // The captured pawn is flung back to its nest — a far larger move than one tile.
    expect(dist(foeBefore, foeAfter), 'captured pawn was sent home').toBeGreaterThan(5);
    await page.context().close();
  });

  test('a Four moves a pawn backward', async ({ browser }) => {
    // A Four moves -4 (see MoveAvailabilityChecker). Placed at tile 5 → lands on tile 1.
    const { page } = await openBoard(browser, {
      players: 3,
      as: 'player0',
      setup: async (api, s) => {
        await setPawn(api, s, 'player0', 0, 'player0', 5);
        await setOnlyCard(api, s, 'player0', 4);
      },
    });

    const before = await pawnCentre(page, 'player0:0');
    await playCard(page, { value: 4, pawns: ['player0:0'] });
    const after = await waitPawnSettled(page, 'player0:0');

    expect(dist(before, after), 'the Four moved the pawn (backward 4)').toBeGreaterThan(5);
    await page.context().close();
  });

  test('a Queen moves a pawn 12 forward', async ({ browser }) => {
    // A Queen moves 12 forward. From the start tile → tile 12, well clear of the finish.
    const { page } = await openBoard(browser, {
      players: 3,
      as: 'player0',
      setup: async (api, s) => {
        await setPawn(api, s, 'player0', 0, 'player0', 0);
        await setOnlyCard(api, s, 'player0', 12);
      },
    });

    const before = await pawnCentre(page, 'player0:0');
    await playCard(page, { value: 12, pawns: ['player0:0'] });
    const after = await waitPawnSettled(page, 'player0:0');

    expect(dist(before, after), 'the Queen moved the pawn 12 forward').toBeGreaterThan(5);
    await page.context().close();
  });
});
