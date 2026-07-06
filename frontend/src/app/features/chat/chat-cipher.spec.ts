import { describe, expect, it } from 'vitest';
import { encrypt, decrypt } from './chat-cipher';

// Port of the GWT client's ChatCipher behaviour. The key invariant is wire-compat
// with the GWT implementation (same external chat room), so besides round-tripping we
// pin a hand-computed vector: 'A'(65) ^ 'k'(107) = 42 = 0x2a → "002a".

describe('ChatCipher', () => {
  it('round-trips text through encrypt → decrypt with the same key', () => {
    const key = 'session-123';
    for (const text of ['Hello', 'keezen! 🎲', 'a', 'meerdere woorden hier']) {
      expect(decrypt(encrypt(text, key), key)).toBe(text);
    }
  });

  it('matches the GWT byte layout: 4 lowercase hex digits per char', () => {
    expect(encrypt('A', 'k')).toBe('002a');
    expect(decrypt('002a', 'k')).toBe('A');
    expect(encrypt('Hi', 'key')).toBe('0023000c');
  });

  it('output length is exactly 4 hex digits per source character', () => {
    expect(encrypt('abcde', 'k')).toHaveLength(5 * 4);
  });

  it('is a no-op when the key is empty (returns the input unchanged)', () => {
    expect(encrypt('plaintext', '')).toBe('plaintext');
    expect(decrypt('deadbeef', '')).toBe('deadbeef');
  });

  it('is a no-op when the text/hex is empty', () => {
    expect(encrypt('', 'key')).toBe('');
    expect(decrypt('', 'key')).toBe('');
  });
});