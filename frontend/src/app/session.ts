/**
 * Resolves the current game session + player the same way the GWT client does
 * (see Cookie.pickValue): a URL query parameter is authoritative — GameRoom puts
 * it there when handing off to the game — and the cookie is the fallback for a
 * plain page refresh where the URL has no value. Same names the rest of the app
 * and GameRoom use: `sessionid` and `playerid`.
 */
export interface GameSessionRef {
  sessionId: string | null;
  playerId: string | null;
}

/**
 * URL parameter is authoritative (GameRoom places it there for this session);
 * the cookie is only a fallback for a page refresh where the URL has no value.
 * A faithful port of the GWT Cookie.pickValue — an empty URL value counts as
 * absent, so it too falls back to the cookie.
 */
export function pickValue(urlVal: string | null, cookieVal: string | null): string | null {
  return urlVal ? urlVal : cookieVal;
}

function urlParam(name: string): string | null {
  return new URLSearchParams(window.location.search).get(name);
}

function cookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
  return match ? decodeURIComponent(match[1]) : null;
}

export function resolveGameSession(): GameSessionRef {
  return {
    sessionId: pickValue(urlParam('sessionid'), cookie('sessionid')),
    playerId: pickValue(urlParam('playerid'), cookie('playerid')),
  };
}