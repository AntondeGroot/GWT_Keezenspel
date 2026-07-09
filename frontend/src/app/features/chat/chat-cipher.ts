/**
 * XOR "cipher" — really just obfuscation — ported byte-for-byte from the GWT client's
 * `adg.keezen.util.ChatCipher`. It must stay wire-compatible: the external chat server
 * (:4100) relays messages verbatim, so a GWT client and an Angular client sharing a
 * room only read each other if both apply the same scheme. Each source character is
 * XORed with the key (cycled) and written as 4 lowercase hex digits; the key is the
 * game's sessionId.
 */

const HEX = '0123456789abcdef';

/** A UTF-16 code unit as 4 hex digits (matches Java's 16-bit `char`). */
function toHex(v: number): string {
  return HEX[(v >> 12) & 0xf] + HEX[(v >> 8) & 0xf] + HEX[(v >> 4) & 0xf] + HEX[v & 0xf];
}

/** Value of one hex digit char code; unknown digits count as 0 (matches the GWT port). */
function hexDigit(c: number): number {
  if (c >= 48 && c <= 57) return c - 48; // '0'–'9'
  if (c >= 97 && c <= 102) return c - 97 + 10; // 'a'–'f'
  if (c >= 65 && c <= 70) return c - 65 + 10; // 'A'–'F'
  return 0;
}

/** Parse 4 hex digits back to a number (case-insensitive; unknown digits count as 0). */
function fromHex(s: string): number {
  let r = 0;
  for (let i = 0; i < s.length; i++) {
    r = r * 16 + hexDigit(s.charCodeAt(i));
  }
  return r;
}

export function encrypt(text: string, key: string): string {
  if (!key || !text) return text;
  let out = '';
  for (let i = 0; i < text.length; i++) {
    const xored = text.charCodeAt(i) ^ key.charCodeAt(i % key.length);
    out += toHex(xored);
  }
  return out;
}

export function decrypt(hex: string, key: string): string {
  if (!key || !hex) return hex;
  let out = '';
  for (let i = 0; i + 4 <= hex.length; i += 4) {
    const xored = fromHex(hex.substring(i, i + 4));
    out += String.fromCharCode(xored ^ key.charCodeAt((i / 4) % key.length));
  }
  return out;
}
