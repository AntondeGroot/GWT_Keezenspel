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
- `frontend/src/app/features/board/board.spec.ts`
- `frontend/src/app/features/board/pawn-and-card-selection.spec.ts`

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
| [ ] | `BoardTest.java` | 2 | `board.spec.ts` (only 1 `it` present) | 🟡 Partial — confirm both cases covered |
| [ ] | `PawnAnimationKeyTest.java` | 4 | _none_ | ❌ Not migrated |
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

> These are **Selenium browser tests** of the GWT UI. There is currently **no** e2e/component
> browser-test framework wired into `frontend/` (only Vitest unit tests). Decide the target
> (Vitest component test, Playwright, etc.) before migrating — none are done yet.

| ✔ | Java IT file | Cases | What it covers | Status |
| --- | --- | --- | --- | --- |
| [ ] | `AutoSelectCardBorder_IT.java` | 2 | Auto-selected card border highlight | ❌ Not migrated |
| [ ] | `Board_IT.java` | 5 | Board rendering/layout | ❌ Not migrated |
| [ ] | `CardAnimation_IT.java` | 1 | Card animation | ❌ Not migrated |
| [ ] | `CardDisplay_IT.java` | 6 | Card display | ❌ Not migrated |
| [ ] | `CardSevenSplit_IT.java` | 1 | Seven-card split move | ❌ Not migrated |
| [ ] | `Card_IT.java` | 4 | Card behaviour | ❌ Not migrated |
| [ ] | `Chat_IT.java` | 5 | In-game chat | ❌ Not migrated |
| [ ] | `GameStateLastMoveResponse_IT.java` | 2 | Last-move response rendering | ❌ Not migrated |
| [ ] | `JackAnimationFromOpponentPerspective_IT.java` | 1 | Jack animation (opponent view) | ❌ Not migrated |
| [ ] | `LeaveGame_IT.java` | 7 | Leaving a game | ❌ Not migrated |
| [ ] | `MobileLayoutCheck_IT.java` | 1 | Mobile layout | ❌ Not migrated |
| [ ] | `MobileLocale_IT.java` | 5 | Mobile locale/i18n | ❌ Not migrated |
| [ ] | `MovingOnBoard_IT.java` | 5 | Moving pawns on board | ❌ Not migrated |
| [ ] | `PawnHighlightColors_IT.java` | 7 | Pawn highlight colours | ❌ Not migrated |
| [ ] | `Pawn_IT.java` | 5 | Pawn behaviour | ❌ Not migrated |
| [ ] | `PlayerStatusMock_IT.java` | 5 | Player status (mocked) | ❌ Not migrated |
| [ ] | `PlayerStatusReal_IT.java` | 3 | Player status (real) | ❌ Not migrated |
| [ ] | `SendButtonSpinner_IT.java` | 1 | Send-button spinner | ❌ Not migrated |
| [ ] | `Winner2Players_IT.java` | 2 | Winner with 2 players | ❌ Not migrated |
| [ ] | `Winner_IT.java` | 1 | Winner detection | ❌ Not migrated |

> IT support files (`Utils/*.java`: `Steps`, `Player`, `TestUtils`, `TestTemplate`,
> `SpringAppTestHelper`, `ScreenshotOnFailure`, etc.) are shared infrastructure, not tests —
> migrate only the behaviours they support.

**Integration total: 0 / 20 `_IT` files migrated.**

---

## Summary

| Category | Migrated | Remaining |
| --- | --- | --- |
| GWT client unit tests | 9.5 / 12 files | `Board` (partial), `PawnAnimationKey`, `Cookie` (6/18, rest N/A) |
| GWT integration tests (`_IT`) | 0 / 20 files | all 20 |

**Not yet migrated:** `BoardTest` (finish), `PawnAnimationKeyTest`, `CookieTest` `pickValue` done
(12 remaining tests are GWT-only, not applicable to Angular), and all 20 `*_IT` integration tests.