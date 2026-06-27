# TypeScript & Angular — Keezen sessions

---

## Session 1 — First Angular view: the game-state component

===PAGE 1===
How do you scaffold a new Angular component with the CLI?
?
Run `ng generate component features/game-state` (shorthand: `ng g component features/game-state`). The path before the name becomes the folder, so this creates a standalone component under `src/app/features/game-state/` with its `.ts`, `.html`, and `.scss` files wired together.

---

How do you get a reference to a service (like the generated API client) inside a component?
?
Call `inject()` from `@angular/core` as a field initializer:
```typescript
private readonly gamestates = inject(GamestatesService);
```
This is the modern alternative to constructor injection. It works because the component is created inside Angular's injection context.

---

The generated API method returns an `Observable` — how do you get the value out?
?
Subscribe to it; the callback runs when the HTTP response arrives:
```typescript
this.gamestates.getGameStateForGame(sessionId).subscribe(s => this.state.set(s));
```
Angular's `HttpClient` observables are lazy (nothing happens until you subscribe) and complete after a single emission.

---

How do you store an async result so the template re-renders when it arrives?
?
Put it in a signal and `.set()` it in the subscribe callback:
```typescript
protected readonly state = signal<GameState | undefined>(undefined);
// ...
.subscribe(s => this.state.set(s));
```
Reading `state()` in the template makes Angular re-render that part when the signal changes.

---

Why is the signal typed `GameState | undefined`, and how do you guard the template?
?
The value isn't there until the HTTP call resolves, so it starts as `undefined`. Guard and narrow in one step with `@if (... ; as ...)`:
```html
@if (state(); as s) { ... {{ s.currentPlayerId }} ... }
```
Inside the block, `s` is a non-null `GameState`, so no `!` is needed.

---

How do you turn a player id into a player name?
?
Look it up in the players array — a `computed` is a good home for it:
```typescript
protected readonly currentPlayerName = computed(() =>
  this.state()?.players?.find(p => p.id === this.state()!.currentPlayerId)?.name
);
```

---

Where do `GameState` and `GamestatesService` come from, and why aren't they in git?
?
They're generated from `keezenspel_openapi.yml` into `src/app/api/` by `npm run generate:api`. That folder is gitignored and regenerated on `postinstall`/`prebuild` — the OpenAPI spec is the source of truth, so the client is rebuilt rather than committed.

===END PAGE===