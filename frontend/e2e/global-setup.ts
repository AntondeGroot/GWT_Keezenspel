import { chromium } from '@playwright/test';

// Warm up the ng-serve dev server before any test runs. Its first request triggers
// a full Angular compile (tens of seconds in CI); doing it here means that cost is
// paid once in setup rather than against — and inside — the first test's timeout.
export default async function globalSetup() {
  const port = Number(process.env.E2E_NG_PORT ?? 4300);
  const baseURL = `http://localhost:${port}`;
  const browser = await chromium.launch();
  try {
    const page = await browser.newPage();
    await page.goto(baseURL, { waitUntil: 'domcontentloaded', timeout: 240_000 });
    // .title-bar is rendered by the App component, so waiting for it confirms the
    // app actually compiled and booted (not just the static index.html shell).
    await page.waitForSelector('.title-bar', { timeout: 240_000 });
  } finally {
    await browser.close();
  }
}