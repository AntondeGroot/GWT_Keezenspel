# GWT → Angular/Vitest Test Migration Checklist

Tracks whether every GWT **client unit test** and GWT **integration test** has been
rewritten as an Angular/Vitest spec under `frontend/`.

## Scope

| In scope (must be migrated to Angular/Vitest) | Out of scope (stays as Java/backend tests) |
| --- | --- |
| `GWT_Keezenspel-client/src/test` — GWT UI unit tests | `GWT_Keezenspel-server/.../UnitTests` — backend game logic |
| `GWT_Keezenspel-server/.../IntegrationTests/*_IT.java` — Selenium UI tests | `GWT_Keezenspel-server/.../ApiTests` — backend API tests |
| | `GWT_Keezenspel-server/.../services`, `logic`, `GWT_Keezenspel-shared` — backend/shared |

> The server `UnitTests`/`ApiTests` exercise the **Spring backend** (game rules, REST API),
> not the GWT frontend. They remain Java tests and are **not** part of the Angular rewrite.

## Migration principle: the two test suites run side by side

The goal is **coexistence**, not replacement. Each GWT test gets an Angular/Vitest equivalent, and
**both keep running** — this lets us confirm the Angular version asserts the same behaviour and
compare their run times (a key reason for the migration). The GWT tests are the reference oracle
until Angular fully replaces GWT.

**Do not delete a GWT test just because it has been migrated.** GWT tests are removed **only when
GWT itself is removed** from the project — at which point the whole GWT test suite goes with it.

> Exception already applied: `pawnOnBoard_SelectAce_Mov` was deleted, but that was a byte-for-byte
> duplicate of `pawnOnBoard_SelectAce_Move` (which still exists) — dead-code cleanup, not a loss of
> GWT coverage.

## How to verify each item

1. Find the matching `*.spec.ts` under `frontend/src/app/`.
2. Confirm **each** `@Test` method in the Java file has an equivalent `it(...)` case.
3. Run both suites — `cd frontend && npm test` for Angular, the Maven test run for GWT — and confirm
   they pass and assert the same behaviour. Optionally record the run times to compare.
4. Tick the box. **Leave the GWT test in place** — it is retired only when GWT is removed.

Current Angular specs:
- `frontend/src/app/app.spec.ts`
- `frontend/src/app/session.spec.ts` — `CookieTest` (pickValue)
- `frontend/src/app/features/board/board.spec.ts` — component smoke test (not part of a GWT port)
- `frontend/src/app/features/board/board-geometry.spec.ts` — `BoardTest`
- `frontend/src/app/features/board/pawn-and-card-selection.spec.ts` — `PawnAndCardSelection*Test`
- `frontend/src/app/features/board/pawn-highlight.spec.ts` — `PawnHighlightColorsTest`
- `frontend/src/app/features/board/pawn-key.spec.ts` — `PawnAnimationKeyTest`
- `frontend/src/app/features/board/card-selection.spec.ts` — `Card_IT` (component)
- `frontend/src/app/features/board/board-render.spec.ts` — `Board_IT` counts + `MobileLocale_IT` i18n
- `frontend/src/app/features/board/auto-select-card.spec.ts` — `AutoSelectCardBorder_IT`
- `frontend/src/app/features/board/pawn-highlight-render.spec.ts` — `PawnHighlightColors_IT` (wiring)

---

## 1. GWT client unit tests → Angular/Vitest

Source root: `GWT_Keezenspel-client/src/test/java/adg/`

| ✔ | Java test file | Cases | Target Angular spec | Status |
| --- | --- | --- | --- | --- |
| [x] | `PawnAndCardSelectionTest.java` | 10 | `pawn-and-card-selection.spec.ts` › `PawnAndCardSelection` | Migrated — verify case-by-case |
| [x] | `PawnAndCardSelectionAceTest.java` | 10 | `pawn-and-card-selection.spec.ts` › `- Ace` | ✅ Migrated & verified |
| [x] | `PawnAndCardSelectionSevenTest.java` | 13 | `pawn-and-card-selection.spec.ts` › `- Seven` | Migrated — verify case-by-case |
| [x] | `PawnAndCardSelectionJackTest.java` | 18 | `pawn-and-card-selection.spec.ts` › `- Jack` | Migrated — verify case-by-case |
| [x] | `PawnAndCardSelectionKingTest.java` | 6 | `pawn-and-card-selection.spec.ts` › `- King` | Migrated — verify case-by-case |
| [x] | `PawnAndCardSelectionAutoSelectTest.java` | 10 | `pawn-and-card-selection.spec.ts` › `- AutoSelect` | Migrated — verify case-by-case |
| [x] | `PawnAndCardSelectionValidationTest.java` | 5 | `pawn-and-card-selection.spec.ts` › `- Validation` | Migrated — verify case-by-case |
| [x] | `PawnAndCardSelectionForfeitTest.java` | 1 | `pawn-and-card-selection.spec.ts` › `- Forfeit` | Migrated — verify case-by-case |
| [x] | `BoardTest.java` | 2 | `board-geometry.spec.ts` | ✅ Migrated & verified (2/2) |
| [x] | `PawnAnimationKeyTest.java` | 4 | `pawn-key.spec.ts` | ✅ Migrated & verified (4/4) |
| [x] | `PawnHighlightColorsTest.java` | 22 | `pawn-highlight.spec.ts` | ✅ Migrated & verified (22/22) |
| [~] | `keezen/util/CookieTest.java` | 18 | `session.spec.ts` (6) | 🟡 Partial (6/18) — see note |

> `CardEnum.java` is a test helper/fixture (0 `@Test`), not a test — migrate only if the Angular
> specs still need the equivalent card enum data.

> **`CookieTest` (6/18 migrated).** Only the `pickValue` group (6) has an Angular counterpart —
> ported to `session.spec.ts` against the extracted `pickValue` in `session.ts`. The other 12
> (`needsLocaleRedirect` ×4, `buildRedirectUrl` ×8) are **not applicable to Angular**: they model
> GWT reloading the page with a `?locale=` param (GWT compiles one permutation per locale), whereas
> Angular switches language at runtime via the `Translations` service with no reload. Porting them
> would add unused production code, so they stay GWT-only and retire when GWT is removed. Revisit
> only if a locale-based redirect is ever introduced in Angular.

**Client unit total: 8 / 12 files migrated** (the whole `PawnAndCardSelection*` family).

Per-test diff resolved the 73-vs-74 gap: the only Java case absent from the Angular spec was
`pawnOnBoard_SelectAce_Mov` in `PawnAndCardSelectionAceTest.java` — a **byte-for-byte duplicate**
of `pawnOnBoard_SelectAce_Move` (a truncated-name copy-paste) that added no coverage. It has been
**deleted from the Java file**, so both sides now sit at 73 cases. Every other case (including the
two identically named `selectPawnTwice_Deselects` tests, in the Ace and Jack files) has a matching
`it(...)`. → All 8 `PawnAndCardSelection*` files are fully migrated.

---

## 2. GWT integration tests (`*_IT`) → Angular

Source root: `GWT_Keezenspel-server/src/test/java/adg/keezen/IntegrationTests/`

These are **Selenium browser tests** that boot the real Spring backend on port 4200 (with test-only
seeding hooks: `/test/reset`, `setPawnPosition`, `setCardForPlayer`, `createNPlayerGame`) and drive
headless Chrome against the GWT DOM. They bind to **GWT-specific CSS classes** (`cardDiv`,
`TextBoxForPawnSteps1`, `pawnIntegerBoxes`, …) that the Angular app does not use — so each is a
**rewrite** against the Angular DOM, not a port.

**Chosen strategy (hybrid) — see Part 3 below.** The 20 files split into four buckets by target:

| ✔ | Java IT file | Cases | Bucket | Target |
| --- | --- | --- | --- | --- |
| [x] | `AutoSelectCardBorder_IT.java` | 2 | C | ✅ `auto-select-card.spec.ts` (2/2) |
| [~] | `Board_IT.java` | 5 | C | ✅ `board-render.spec.ts` counts (3); title = static `index.html`; centering → D |
| [x] | `Card_IT.java` | 4 | C | ✅ `card-selection.spec.ts` (4/4) — Proof C |
| [x] | `PawnHighlightColors_IT.java` | 7 | C | ✅ `pawn-highlight-render.spec.ts` (wiring) + `pawn-highlight.spec.ts` (colour maths) |
| [ ] | `MobileLocale_IT.java` | 5 | C | ✅ `board-render.spec.ts` i18n text (2); `mobile.html` redirect + reload = N/A (SPA) |
| [x] | `MobileLayoutCheck_IT.java` | 2 | D | ✅ `mobile-layout.spec.ts` (2–8 players: board/roster/buttons on screen, no overlap) |
| [ ] | `Chat_IT.java` | 5 | ~~C~~ **blocked** | No Angular chat feature yet — nothing to test until it's built |
| [x] | `CardAnimation_IT.java` | 1 | D | ✅ `card-animation.spec.ts` (1/1) — no-pawn move keeps the card |
| [x] | `CardDisplay_IT.java` | 5 | D | ✅ `card-display.spec.ts` — hand renders, forfeit→pile, next player has hand |
| [x] | `CardSevenSplit_IT.java` | 1 | D | ✅ `card-seven-split.spec.ts` (1/1) — re-select-7 reset regression |
| [x] | `JackAnimationFromOpponentPerspective_IT.java` | 1 | D | ✅ `jack-animation.spec.ts` — observer sees the swap (move via API) |
| [x] | `MovingOnBoard_IT.java` | 4 | D | ✅ `moving-on-board.spec.ts` (4/4) — first real bucket-D |
| [x] | `Pawn_IT.java` | 4 | D | ✅ `pawn-selection.spec.ts` (3 selection + case 4: forfeit ×2 → next player moves) |
| [ ] | `PlayerStatusMock_IT.java` | 5 | D | Playwright E2E |
| [ ] | `PlayerStatusReal_IT.java` | 3 | D | Playwright E2E |
| [x] | `Winner2Players_IT.java` | 2 | D | ✅ `winner.spec.ts` — medal to the right player + finished state |
| [x] | `Winner_IT.java` | 1 | D | ✅ `winner.spec.ts` — medals in finishing order (gold, silver) |
| [ ] | `GameStateLastMoveResponse_IT.java` | 2 | A | Stay Java (backend API) — not a frontend migration |
| [ ] | `LeaveGame_IT.java` | 7 | A | Stay Java (backend API + raw SSE) |
| — | `SendButtonSpinner_IT.java` | 0 | B | N/A — disabled stub (empty body); revisit if re-enabled |

Buckets: **A** = pure backend (no browser), **B** = disabled stub, **C** = frontend-only
(render/CSS/layout/i18n, faked API), **D** = true E2E (real game logic, SSE, animation, geometry).

> IT support files (`Utils/*.java`: `Steps`, `Player`, `TestUtils`, `SpringAppTestHelper`,
> `ScreenshotOnFailure`, etc.) are shared infrastructure, not tests. For bucket D they get rewritten
> once as a Playwright helper layer (a `Steps`-equivalent DSL + API-seeding via Playwright's
> `request` fixture). `ScreenshotOnFailure` and `TestTemplate` are already unused/disabled today.

**Bucket C: component-testing pass complete.** Five files landed as Angular component specs
(`card-selection`, `auto-select-card`, `pawn-highlight-render`, `board-render`) — 16 new `it`s, all
green under `ng test`. On inspection bucket C was not uniformly jsdom-testable, so it reshaped:
- **Fully migrated (component tests):** `Card_IT`, `AutoSelectCardBorder_IT`, `PawnHighlightColors_IT`.
- **Partially migrated (rest → D/N/A):** `Board_IT` (counts done; vertical-centering → D; title is
  static `index.html`), `MobileLocale_IT` (i18n button text done; `mobile.html` redirect + reload = N/A
  for a responsive SPA).
- **Reclassified C → D:** `MobileLayoutCheck_IT` (viewport/overlap geometry — needs a real browser).
- **Blocked:** `Chat_IT` — the Angular app has **no chat feature yet**; can't test UI that doesn't exist.

**Winner/medal feature built + `Winner_IT`/`Winner2Players_IT` migrated.** The backend already
populates `place` (1/2/3) and `isActive` on a finisher; the roster (built earlier) renders the medal
+ dimmed/struck "finished" state, and a new **`app-winner-banner`** (reads `GameStore.winners`)
announces each finisher with a celebratory plaque. Covered by `winner-banner.spec.ts` (4 component
tests) and `e2e/winner.spec.ts` (3): 2-player winner gets gold + finished + banner; the 2-player
bug guard (the winner, not the other colour, gets the medal); 3-player medals in finishing order.

**Integration total: ~14 / 20 covered on the frontend — every readily-migratable IT is now done.**
C bucket's testable assertions plus seven real bucket-D E2E files (`MovingOnBoard_IT`, `Pawn_IT`,
`CardDisplay_IT`, `CardAnimation_IT`, `CardSevenSplit_IT`, `JackAnimationFromOpponentPerspective_IT`,
`MobileLayoutCheck_IT`). The remaining eight are **not** currently migratable:
- **Blocked on unbuilt Angular features (3):** `Chat_IT`, `PlayerStatusMock_IT`,
  `PlayerStatusReal_IT` — no chat or player-status UI yet. (Winner/medal is now built — see below.)
- **Stay Java (2, bucket A):** `GameStateLastMoveResponse_IT`, `LeaveGame_IT` (backend JSON/SSE).
- **N/A stub (1, bucket B):** `SendButtonSpinner_IT` (disabled).

---

## Summary

| Category | Migrated | Remaining |
| --- | --- | --- |
| GWT client unit tests | 11.5 / 12 files | `Cookie` only (6/18 migrated, other 12 GWT-only / N/A) |
| GWT integration tests (`_IT`) | ~14 / 20 files (C bucket + 9 D files incl. Winner/Winner2Players) | 3 blocked (Chat/PlayerStatus, need features), 2 stay Java (A), 1 N/A stub (B) |

**Client unit tests: effectively complete.** Every migratable case is ported; the only unported
GWT cases are `CookieTest`'s 12 locale-redirect tests, which are GWT-only (not applicable to
Angular). **Remaining work is all integration:** the 20 `*_IT` tests.

---

## Part 3: Integration test strategy (hybrid)

Decision (chosen): a **hybrid** — route each `*_IT` to the cheapest home that can still make its
assertion faithfully. See the bucket column in section 2.

**A — Backend, stays Java (2).** `GameStateLastMoveResponse_IT`, `LeaveGame_IT` never open a browser;
they assert backend JSON/HTTP + raw SSE. Not a frontend migration. Best relocated into the server
test module; out of scope for `frontend/`.

**B — Disabled (1).** `SendButtonSpinner_IT` is an empty stub (logic commented out as flaky). Nothing
to migrate; leave N/A until/if re-enabled.

**C — Angular component tests (7).** Assertions are genuinely client-side (border toggling, highlight
colours, layout geometry, i18n text). Write as Angular TestBed/Vitest component tests with a **faked
API** — runs in the existing `ng test`/Vitest suite, no browser, no backend. Fast.

**Proof C — done (`Card_IT` → `card-selection.spec.ts`, 4/4).** Established the reusable harness for
rendering `Board` in a component test without a backend:
- Providers: `[provideHttpClient(), provideApi('')]`. Import `Board` directly (standalone).
- Set `document.cookie = 'playerid=0'` **before** `createComponent` (that's the viewer seat, read by
  `resolveGameSession()` at construction). Do **not** set `sessionid` → `ngOnInit` opens no
  `EventSource` (jsdom has none). No initial HTTP fetch happens; state arrives only via SSE normally.
- Seed the private state signal directly:
  `(fixture.componentInstance as any).state.set({ players:[{id,name,playerInt,color}…], pawns:[],
  winners:[], version:1, currentPlayerId, playerCards:[…] })`. The card layer only renders once
  `tiles().length > 0`, so **players with `playerInt` are required** (geometry needs them), even for a
  card test.
- Run with `CI=1 npx ng test --watch=false` (component specs need the `@angular/build:unit-test`
  builder's environment; they do **not** run under raw `npx vitest run`).
- **jsdom caveat:** component stylesheets are not applied to `getComputedStyle`, so assert the
  state that drives styling (e.g. the `selected` class), not computed px/colour. Exact border
  px/colour is a real-browser check → bucket D (Playwright).

**D — Playwright E2E (10).** Assertions *are* backend behaviour (winner/medal order, move legality,
7-split math, SSE-driven animation). Rewrite against a **real backend** using the existing test-only
seeding hooks.

**Bucket-D real migrations landed — the harness is in place (8 files, 21 cases).**
- `MovingOnBoard_IT` → `e2e/moving-on-board.spec.ts` (4/4): nest→board with Ace, nest→board with
  King, on-board move with Ace, Jack switch — real UI moves, positions asserted via SSE.
- `Pawn_IT` (selection) → `e2e/pawn-selection.spec.ts` (3): click own pawn selects it, second own
  pawn steals the selection, opponent pawn can't be selected. Case 4 (forfeit ×2 → move) is TODO.
- `CardDisplay_IT` → `e2e/card-display.spec.ts` (3): hand renders (5 cards), forfeit clears the hand
  and moves all of them to the pile, and the next player still sees their own hand. (Case 5's
  `canvasCards2 data-played-count` = the pile-card count assertion.)
- `CardAnimation_IT` → `e2e/card-animation.spec.ts` (1): selecting a card with no pawn keeps it in
  hand — Angular disables Play for the illegal move rather than sending + server-rejecting.
- `CardSevenSplit_IT` → `e2e/card-seven-split.spec.ts` (1): the step-box desync regression — editing
  a split then re-selecting the 7 snaps the boxes back to the model default. Angular passes (no bug).
- `Pawn_IT` case 4 → in `e2e/pawn-selection.spec.ts` (1): after two players forfeit (no legal move),
  the third player still moves onto the board over SSE.
- `JackAnimationFromOpponentPerspective_IT` → `e2e/jack-animation.spec.ts` (1): player0 plays a jack
  switch via the **API** (not the UI); the observer (player1) receives it over SSE and both pawns
  animate to each other's tiles. Uses the new `makeMove` seed helper.
- `MobileLayoutCheck_IT` → `e2e/mobile-layout.spec.ts` (7): for 2–8 players the board, roster and
  buttons are all on screen and never overlap. (Largely overlaps the existing `controls-layout` /
  `board-layout` suites, now made explicit per player count.)
- Helpers grew: `handCards`/`pileCards`/`forfeit`, `openBoard`/`viewAs` (now with `viewport` +
  `gameOptions`), and `makeMove` in `seed.ts`.

**New feature (not a migration): the must-play rule.** With the `mustPlayIfPossible` game option on,
the backend sends `canForfeit=false` while a legal move exists. The board now wires the **Forfeit**
button to it (`[disabled]="!canForfeit()"`, computed from `state().canForfeit ?? true`), so you can't
forfeit when you must play — Play stays the green action. Covered by `e2e/forfeit-rule.spec.ts` (2).
- **Selector layer:** added `data-testid="pawn-{playerId}:{pawnNr}"` and `data-testid="card-{value}"`
  to `board.html` (the checklist's stated stable-selector strategy).
- **Steps DSL:** `e2e/support/steps.ts` — `playCard({value, pawns})`, `clickPawn`, `pawnCentre`,
  `waitPawnSettled` (polls the box until still, = GWT `waitUntilPawnStopsMoving`), `isPawnSelected`
  (reads `--pawn-highlight`), `dist`. Reuse for the remaining D targets.
- **Perspective/turns:** unlike the GWT ordered shared session (which relied on natural turn order),
  each case seeds a fresh game and plays as **player0** (the starting player). `setPlayerIdPlaying`
  was only a viewer cookie-swap + refresh, never a backend "set current player", so nothing is lost.

**Proof D — done (smoke green: seed real game → Angular renders it over SSE).** Harness established:
- `frontend/playwright.config.ts` (`npm run e2e`). Serves Angular via `ng serve` on **:4300** (the
  backend occupies ng's default 4200), `proxy.conf.json` forwards API + SSE to the backend
  (`E2E_API_URL`, default `http://localhost:4200`). `workers: 1` (shared backend state).
- `frontend/e2e/support/seed.ts` — the `ApiUtil`/`ApiCallsHelper` seeding layer over HTTP
  (`createGame`, `setPawn`, `setCard`). **Gotcha:** `POST /games/{s}/players` needs `{id, name}`
  (both required by the `Player` schema) — `{name}` alone 400s.
- Tests seed via an `APIRequestContext` at `E2E_API_URL`, then drive the UI at `baseURL` (:4300).
- Setup cost: `npm i -D @playwright/test` + `npx playwright install chromium` (Playwright 1.61.1
  wanted the 1228 browser build).
- **Backend note:** the running :4200 backend serves the *GWT* app, not Angular — Angular is served
  separately by the harness. For CI, boot an isolated backend (realCardDeck profile) rather than a
  developer's running instance.

> **Bucket-D reality check — several targets are blocked by unbuilt Angular features.**
> `Winner2Players_IT` / `Winner_IT` assert on a **winner/medal + player-status UI that Angular does
> not have yet** (`board.ts` ignores `winners`; nothing renders a medal or `playerNotPlaying`).
> `PlayerStatus*_IT` likewise. These are **blocked** (like `Chat_IT`) until those features exist.
> Viable D targets whose UI exists: `MovingOnBoard_IT`, `Pawn_IT`, `CardAnimation_IT`,
> `CardSevenSplit_IT`, `CardDisplay_IT`.

### Cross-cutting decisions
- **Selectors:** add `data-testid` attributes to Angular components as the stable selector layer.
  Do *not* recreate the GWT suite's brittle CSS-class coupling.
- **Multiplayer:** the GWT suite fakes it with one browser + cookie-swap + refresh. Playwright can
  reuse that, or use multiple browser contexts (cleaner) for observer/SSE tests.
- **Orchestration (the real infra cost, shared by all of D):** boot Spring backend on 4200 → serve
  the Angular build (or `ng serve` + `proxy.conf.json`) → run Playwright → `/test/reset` between
  tests. New CI job (the repo already splits CI into angular / gwt-unit / gwt-integration / backend).

### Suggested sequencing
1. ~~**Proof C:** migrate one bucket-C test as an Angular component test — no new tooling.~~
   ✅ **Done** — `Card_IT` → `card-selection.spec.ts` (4/4). Harness recipe captured under bucket C above.
2. ~~**Proof D:** stand up Playwright + the serve/seed harness, migrate one bucket-D test.~~
   ✅ **Done** — `frontend/e2e/proof-d.spec.ts` smoke green (seed → SSE → render). Recipe under
   bucket D above. (`Winner2Players_IT` turned out blocked — no Angular winner UI — so the proof used
   a render smoke; real D tests target `MovingOnBoard_IT` / `Pawn_IT` / etc.)
3. ~~Fan out the rest of C~~ ✅ **Done** — bucket C component-testing pass complete (see section 2).
   Then D, reusing the helper layer.
4. Relocate bucket A into the backend test module; retire bucket B note.
5. Build the Angular chat feature, then unblock `Chat_IT`.