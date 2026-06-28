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

function urlParam(name: string): string | null {
  const value = new URLSearchParams(window.location.search).get(name);
  return value && value.length > 0 ? value : null;
}

function cookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
  return match ? decodeURIComponent(match[1]) : null;
}

export function resolveGameSession(): GameSessionRef {
  return {
    sessionId: urlParam('sessionid') ?? cookie('sessionid'),
    playerId: urlParam('playerid') ?? cookie('playerid'),
  };
}