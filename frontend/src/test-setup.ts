// Global test setup, executed before every spec file.
//
// jsdom does not implement HTMLMediaElement playback, so any code that calls
// audio.play()/pause()/load() (e.g. SoundService) floods the test output with
// "Error: Not implemented: HTMLMediaElement.prototype.play" noise. Stub the
// playback methods so the tests exercise the surrounding logic quietly.
const noop = (): void => {};
Object.defineProperties(HTMLMediaElement.prototype, {
  play: { configurable: true, value: () => Promise.resolve() },
  pause: { configurable: true, value: noop },
  load: { configurable: true, value: noop },
});
