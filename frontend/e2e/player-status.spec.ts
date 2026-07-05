import { test, expect, request, Page } from '@playwright/test';
import { API_URL } from '../playwright.config';
import { createGame, setOnlyCard } from './support/seed';
import {
  openBoard,
  viewAs,
  forfeit,
  playCard,
  chipState,
  pawnCentre,
  waitPawnSettled,
  dist,
} from './support/steps';

// Rewrite of PlayerStatusReal_IT + PlayerStatusMock_IT. Both Selenium files asserted
// the two turn/active CSS classes on the player list — GWT `playerPlaying playerActive`
// (it's your turn) vs `playerNotPlaying playerInactive` (turn has passed on, e.g. after
// a forfeit). The Angular roster renders the same state (`chip--turn` ← isPlaying,
// `chip--inactive` ← !isActive), so here we watch the turn move around the roster over
// SSE against the real backend. The GWT tests used a viewer cookie-swap to fake the
// turn; we instead drive each forfeit/move from that player's own seat, exactly like
// the other bucket-D specs (a real backend turn, not a viewer swap).

const ON_TURN = { onTurn: true, inactive: false };
const PASSED = { onTurn: false, inactive: true };

// ── PlayerStatusReal_IT (2): forfeit clicked through the real UI ──────────────
test.describe('player status — real forfeit (PlayerStatusReal_IT)', () => {
  test('player0 is playing when the game starts', async ({ browser }) => {
    const { page } = await openBoard(browser, { players: 3, as: 'player0' });
    await expect.poll(() => chipState(page, 'player0')).toEqual(ON_TURN);
    await page.context().close();
  });

  test('player1 is playing when player0 forfeits', async ({ browser }) => {
    const { page } = await openBoard(browser, { players: 3, as: 'player0' });
    await forfeit(page);
    // The forfeiter drops out of the turn and goes inactive; the next seat lights up.
    await expect.poll(() => chipState(page, 'player0')).toEqual(PASSED);
    await expect.poll(() => chipState(page, 'player1')).toEqual(ON_TURN);
    await page.context().close();
  });
});

// ── PlayerStatusMock_IT (4): the turn walks around a 3-player game ─────────────
// Serial + a shared observer page mirrors the GWT static WebDriver + @Order flow:
// each step builds on the last as the turn is handed on by successive forfeits.
test.describe.serial('player status — turn walk (PlayerStatusMock_IT)', () => {
  let observer: Page;
  let sessionId: string;

  test.beforeAll(async ({ browser }) => {
    const api = await request.newContext({ baseURL: API_URL });
    ({ sessionId } = await createGame(api, 3));
    // Give player2 a lone Ace for the final step — a legal nest → board move, the way
    // the GWT test played card value 1 on pawn 2:0.
    await setOnlyCard(api, sessionId, 'player2', 1);
    await api.dispose();
    // Observe the whole roster from player0's seat; forfeits/moves are driven from the
    // acting player's own seat below and reach the observer over SSE.
    observer = await viewAs(browser, sessionId, 'player0');
  });

  test.afterAll(async () => {
    await observer.context().close();
  });

  test('player0 is playing when the game starts', async () => {
    await expect.poll(() => chipState(observer, 'player0')).toEqual(ON_TURN);
  });

  test('player0 is inactive after forfeiting, player1 is playing', async () => {
    await forfeit(observer); // observer's own seat is player0
    await expect.poll(() => chipState(observer, 'player1')).toEqual(ON_TURN);
    await expect.poll(() => chipState(observer, 'player0')).toEqual(PASSED);
  });

  test('player2 is playing once player0 and player1 have forfeited', async ({ browser }) => {
    const p1 = await viewAs(browser, sessionId, 'player1');
    await forfeit(p1);
    await p1.context().close();
    await expect.poll(() => chipState(observer, 'player2')).toEqual(ON_TURN);
  });

  test('player2 stays active after playing a card', async ({ browser }) => {
    const p2 = await viewAs(browser, sessionId, 'player2');
    const before = await pawnCentre(p2, 'player2:0');
    await playCard(p2, { value: 1, pawns: ['player2:0'] });
    // Wait for the move to land (pawn leaves the nest) so we assert the *post*-play state.
    const after = await waitPawnSettled(p2, 'player2:0');
    expect(dist(before, after)).toBeGreaterThan(5);
    await p2.context().close();
    // The crux of this case: unlike forfeiting (which deactivated player0 above), playing
    // keeps player2 an active participant. That Ace was their only card, so the round rolls
    // over and the current turn legitimately moves off player2 — but they stay active.
    await expect.poll(async () => (await chipState(observer, 'player2')).inactive).toBe(false);
  });
});