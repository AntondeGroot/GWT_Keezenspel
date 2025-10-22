package ADG.Games.Keezen.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import java.util.HashMap;

/**
 * Utility functions for converting JS objects into Java collections.
 */
public final class JsInteropUtil {

  private JsInteropUtil() {
  }

  /**
   * Converts a plain JavaScript object (with string keys and numeric values) into a Java
   * HashMap<String, Integer>.
   */
  public static HashMap<String, Integer> toHashMap(JavaScriptObject jsObj) {
    HashMap<String, Integer> map = new HashMap<>();
    if (jsObj == null) {
      return map;
    }

    JsArrayString keys = getObjectKeys(jsObj);
    for (int i = 0; i < keys.length(); i++) {
      String key = keys.get(i);
      int value = getIntProperty(jsObj, key);
      map.put(key, value);
    }
    return map;
  }

  // Native JSNI helpers
  private static native JsArrayString getObjectKeys(JavaScriptObject obj) /*-{
        return Object.keys(obj);
    }-*/;

  private static native int getIntProperty(JavaScriptObject obj, String key) /*-{
        return obj[key] || 0;
    }-*/;
}
