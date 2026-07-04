import { defineConfig } from '@playwright/test';

// Proof-D harness (bucket D — true end-to-end tests against the real backend).
//
// Orchestration:
//   - The Spring backend runs on E2E_API_URL (default http://localhost:4200) and
//     exposes the same /test/* seeding hooks the GWT Selenium ITs used.
//   - Playwright serves the Angular app via `ng serve` on NG_PORT, with
//     proxy.conf.json forwarding /games, /moves, /gamestates (SSE), /cards and
//     /game-options to the backend. proxy.conf.json must target E2E_API_URL.
//   - Tests seed state by hitting the backend directly (an APIRequestContext with
//     baseURL = E2E_API_URL), then drive the served Angular UI at baseURL.
//
// NOTE: ng serve defaults to port 4200, which the backend occupies — so the UI is
// served on a separate port (NG_PORT) to avoid the clash.

const NG_PORT = Number(process.env.E2E_NG_PORT ?? 4300);
export const API_URL = process.env.E2E_API_URL ?? 'http://localhost:4200';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1, // shared backend state — keep tests serial
  reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : 'list',
  // Warm up ng serve (first compile is slow in CI) before the tests run.
  globalSetup: './e2e/global-setup.ts',
  use: {
    baseURL: `http://localhost:${NG_PORT}`,
    trace: 'on-first-retry',
  },
  webServer: {
    command: `npm run start -- --port ${NG_PORT} --proxy-config proxy.conf.json`,
    url: `http://localhost:${NG_PORT}`,
    reuseExistingServer: true,
    timeout: 240_000,
  },
});