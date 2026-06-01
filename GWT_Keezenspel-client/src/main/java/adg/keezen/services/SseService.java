package adg.keezen.services;

import com.google.gwt.core.client.JavaScriptObject;

public class SseService {

  public interface Callback {
    void onMessage(String data);
    void onError();
  }

  private JavaScriptObject eventSource;

  /** Connect listening for a named SSE event (e.g. "gamestate"). */
  public void connect(String url, String eventName, Callback callback) {
    disconnect();
    eventSource = openEventSource(url, eventName, callback);
  }

  /** Connect listening for the default unnamed SSE message event. */
  public void connect(String url, Callback callback) {
    connect(url, "", callback);
  }

  public void disconnect() {
    if (eventSource != null) {
      closeEventSource(eventSource);
      eventSource = null;
    }
  }

  private native JavaScriptObject openEventSource(String url, String eventName, Callback callback) /*-{
    var es = new EventSource(url);
    if (eventName) {
      es.addEventListener(eventName, $entry(function(e) {
        callback.@adg.keezen.services.SseService.Callback::onMessage(Ljava/lang/String;)(e.data);
      }));
    } else {
      es.onmessage = $entry(function(e) {
        callback.@adg.keezen.services.SseService.Callback::onMessage(Ljava/lang/String;)(e.data);
      });
    }
    es.onerror = $entry(function(e) {
      callback.@adg.keezen.services.SseService.Callback::onError()();
    });
    return es;
  }-*/;

  private static native void closeEventSource(JavaScriptObject es) /*-{
    es.close();
  }-*/;
}