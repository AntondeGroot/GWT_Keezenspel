import { createServer, type Server } from 'node:http';

// A controllable stand-in for the external chat server on :4100 (the GameRoom service),
// the Playwright equivalent of the Selenium suite's `ChatServerMock`. The keezen backend
// proxies /chat/** to :4100, so starting/stopping this toggles chat availability in the
// UI. Like the Java mock it just streams an empty message list and 200s everything else —
// the migrated Chat_IT only asserts visibility + layout, not message content.

let server: Server | null = null;

export async function startChatMock(port = 4100): Promise<void> {
  if (server) return;
  const srv = createServer((req, res) => {
    if ((req.url ?? '').endsWith('/stream')) {
      res.writeHead(200, {
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache',
        Connection: 'keep-alive',
      });
      res.write('data: []\n\n'); // one payload → the UI flips "online"
      const ping = setInterval(() => res.write(': ping\n\n'), 15_000);
      req.on('close', () => clearInterval(ping));
    } else {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end('[]');
    }
  });
  await new Promise<void>((resolve) => srv.listen(port, resolve));
  server = srv;
}

export async function stopChatMock(): Promise<void> {
  const srv = server;
  if (!srv) return;
  server = null;
  // Drop the open SSE connection (the backend's proxy) so close() can complete.
  srv.closeAllConnections?.();
  await new Promise<void>((resolve) => srv.close(() => resolve()));
}
