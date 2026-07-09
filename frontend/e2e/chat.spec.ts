import { test, expect } from '@playwright/test';
import { openBoard } from './support/steps';
import { startChatMock, stopChatMock } from './support/chat-mock';

// Rewrite of Chat_IT. The GWT test bound to `chatSendButton` / `chatInputField` /
// `chatDisplayField` and toggled a mock chat server on :4100; here we drive the Angular
// chat panel (data-testid selectors) and toggle the same-role mock. The keezen backend
// proxies /chat/** to :4100, so the chat UI is present only while the mock is up —
// exactly the availability behaviour Chat_IT asserted. Content isn't tested (the mock
// streams an empty list); the assertions are visibility + "just above the footer".

const send = (p: import('@playwright/test').Page) => p.getByTestId('chat-send');
const input = (p: import('@playwright/test').Page) => p.getByTestId('chat-input');
const display = (p: import('@playwright/test').Page) => p.getByTestId('chat-display');

test.afterEach(async () => {
  await stopChatMock(); // leave :4100 down so other specs see chat hidden
});

test.describe('in-game chat (Chat_IT)', () => {
  test('chat UI is hidden when the chat server is down', async ({ browser }) => {
    // No mock started → the /chat stream errors → the panel renders nothing.
    const { page } = await openBoard(browser, { players: 3, as: 'player0' });
    await expect(send(page)).toHaveCount(0);
    await expect(input(page)).toHaveCount(0);
    await expect(display(page)).toHaveCount(0);
    await page.context().close();
  });

  test('chat UI (input, send, display) becomes visible when the chat server is up', async ({
    browser,
  }) => {
    await startChatMock();
    const { page } = await openBoard(browser, { players: 3, as: 'player0' });
    await expect(send(page)).toBeVisible();
    await expect(input(page)).toBeVisible();
    await expect(display(page)).toBeVisible();
    await page.context().close();
  });

  test('the send button sits just above the footer', async ({ browser }) => {
    await startChatMock();
    const { page } = await openBoard(browser, { players: 3, as: 'player0' });
    await expect(send(page)).toBeVisible();

    const btn = await send(page).boundingBox();
    const footer = await page.locator('footer').boundingBox();
    if (!btn || !footer) throw new Error('send button or footer not laid out');

    const buttonBottom = btn.y + btn.height;
    expect(buttonBottom, 'send button bottom should be above the footer top').toBeLessThan(
      footer.y,
    );
    expect(
      footer.y - buttonBottom,
      'send button should be just above the footer',
    ).toBeLessThanOrEqual(60);
    await page.context().close();
  });
});
