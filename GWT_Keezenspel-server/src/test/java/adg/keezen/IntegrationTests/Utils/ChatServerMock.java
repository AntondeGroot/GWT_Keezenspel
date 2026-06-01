package adg.keezen.IntegrationTests.Utils;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class ChatServerMock {

  private static HttpServer server;

  public static void start() {
    if (server != null) return;
    try {
      server = HttpServer.create(new InetSocketAddress(4100), 0);
      server.createContext(
          "/chat/",
          exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
              exchange.sendResponseHeaders(204, -1);
              return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path.endsWith("/stream")) {
              // SSE stream: send an initial empty message list and keep the connection alive.
              exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
              exchange.getResponseHeaders().add("Cache-Control", "no-cache");
              exchange.getResponseHeaders().add("X-Accel-Buffering", "no");
              exchange.sendResponseHeaders(200, 0);
              OutputStream out = exchange.getResponseBody();
              try {
                out.write("data: []\n\n".getBytes());
                out.flush();
                // Keep alive with periodic heartbeats until the client disconnects.
                while (!Thread.currentThread().isInterrupted()) {
                  Thread.sleep(15_000);
                  out.write(": ping\n\n".getBytes());
                  out.flush();
                }
              } catch (Exception ignored) {
                // Client disconnected — normal.
              }
            } else {
              exchange.getResponseHeaders().add("Content-Type", "application/json");
              byte[] response = "[]".getBytes();
              exchange.sendResponseHeaders(200, response.length);
              exchange.getResponseBody().write(response);
              exchange.getResponseBody().close();
            }
          });
      server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
      server.start();
    } catch (IOException e) {
      throw new RuntimeException("Failed to start ChatServerMock on port 4100", e);
    }
  }

  public static void stop() {
    if (server != null) {
      server.stop(0);
      server = null;
    }
  }
}