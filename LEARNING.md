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

---

## Session 2 — Reading the session from URL params and cookies

===PAGE 1===
How do you read a query parameter from the current URL in plain TypeScript?
?
Use the browser's `URLSearchParams` on `window.location.search`:
```typescript
const value = new URLSearchParams(window.location.search).get('sessionid');
```
`window.location.search` is the `?a=1&b=2` part of the URL; `.get(name)` returns the value or `null` if it is absent.

---

How do you read a cookie value by name in plain TypeScript?
?
There is no built-in getter, so read `document.cookie` (a single `"a=1; b=2"` string) and pull the value out:
```typescript
const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
const value = match ? decodeURIComponent(match[1]) : null;
```
`decodeURIComponent` reverses the encoding the browser applies when the cookie was set.

---

When a value can come from either the URL or a cookie, which should win, and how do you express it?
?
The URL parameter wins, with the cookie as a fallback — the URL is the explicit hand-off for *this* visit, the cookie is what persists across refreshes. Express it with `??` (nullish coalescing):
```typescript
const sessionId = urlParam('sessionid') ?? cookie('sessionid');
```
`a ?? b` evaluates to `a` unless `a` is `null`/`undefined`, in which case it falls back to `b`. (This mirrors the GWT client's `Cookie.pickValue`.)

===END PAGE===

---

## Session 3 — Rendering lists: `@for`, `$index`, and self-closing tags

===PAGE 1===
Inside an Angular `@for`, how do you reference the current item's position in the list — what is the variable called?
?
`$index` — note the leading `$`, which marks all of `@for`'s built-in variables (the others are `$first`, `$last`, `$even`, `$odd`, `$count`). It starts at `0`.

A common use is `track`, which is required and tells Angular how to identify each item across re-renders. Use `track $index` only when items have **no stable unique id**:
```html
@for (p of pawns(); track $index) { ... }
```
Prefer a real id when one exists (e.g. `track item.id`) — index-based tracking re-renders more than necessary if the list reorders.

---

When can an element be self-closed in an Angular template, and when not?
?
Only three kinds of elements may use `<… />`:
- **void** elements (`<br/>`, `<img/>`, `<input/>`),
- **custom** elements — a tag with a hyphen, including your components (`<app-board />`),
- **foreign** elements (SVG / MathML).

A normal element like `<div/>` is **not** allowed and fails the build with **NG5002: "Only void, custom and foreign elements can be self closed"**. Close it explicitly instead: `<div ...></div>`.

===END PAGE===

---

## Session 4 — Class fields assigned later, and component cleanup

===PAGE 1===
A class field is assigned later (e.g. in `ngOnInit`), not at its declaration or in the constructor. How do you declare it so strict TypeScript accepts it, and what error appears if you don't?
?
Mark it **optional** with `?`:
```typescript
private eventSource?: EventSource;
```
The `?` makes the type `EventSource | undefined`, which satisfies strict mode (it's allowed to be unset). Without the `?` you get **TS2564: "Property … has no initializer and is not definitely assigned in the constructor."** Because it may be undefined, you then read it with optional chaining: `this.eventSource?.close()`.

(Remember the distinction: class fields use `private`/`protected`/`public` (+ optional `readonly`) — never `const`/`let`, which are only for local variables inside a method.)

---

How do you run cleanup when an Angular component is destroyed, and why declare the interface instead of just writing the method?
?
Add an `ngOnDestroy()` method and declare `implements OnDestroy` (imported from `@angular/core`):
```typescript
export class Board implements OnInit, OnDestroy {
  ngOnDestroy(): void {
    this.eventSource?.close();
  }
}
```
Angular calls `ngOnDestroy` whenever the method exists, so it works without the interface — but `implements OnDestroy` makes TypeScript verify the method's name and signature, catching a typo like `ngOnDestory`. Typical use: closing an `EventSource`, unsubscribing, clearing timers.

===END PAGE===

===END PAGE===