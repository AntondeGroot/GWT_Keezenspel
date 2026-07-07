/**
 * The path prefix the app is mounted under, read from <base href> at runtime:
 * '' when served at the root (local dev / default build), '/keezen' when the deploy
 * build sets base-href=/keezen/. Prepend it to every root-absolute API / SSE / asset URL
 * so the app works under a sub-path — a bare '/foo' from a page mounted at /keezen/ would
 * escape the prefix and 404. Assets referenced relatively in templates (e.g.
 * src="study-icon.svg") resolve against <base href> automatically and don't need this.
 */
export function basePath(): string {
  const href = document.querySelector('base')?.getAttribute('href') ?? '/';
  return href.replace(/\/+$/, ''); // '/keezen/' -> '/keezen', '/' -> ''
}
