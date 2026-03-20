package ADG.Games.Keezen.IntegrationTests.Utils;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
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
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
              exchange.sendResponseHeaders(204, -1);
              return;
            }
            byte[] response = "[]".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
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