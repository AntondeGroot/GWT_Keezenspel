package adg.keezen.util;

import adg.keezen.i18n.AppConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

/**
 * A small modal that explains why a move could not be made. It is a compact, opaque box centred
 * over the board. It is rendered with a native &lt;dialog&gt; in the browser's top layer so it sits
 * above the card deck — the mobile layout zoom-scales the cards, and zoomed content otherwise
 * paints over a normal fixed/positioned overlay regardless of z-index. The backdrop is transparent
 * (not a full-screen dark cover). Clicking anywhere closes it.
 */
public final class MoveRejectedPopup {

  private static final AppConstants CONSTANTS = GWT.create(AppConstants.class);

  private MoveRejectedPopup() {}

  public static void show(String title, String message) {
    Element dialog = Document.get().createElement("dialog");
    dialog.setClassName("move-rejected-dialog");

    Element titleEl = Document.get().createDivElement().cast();
    titleEl.setClassName("move-rejected-title");
    titleEl.setInnerText(title);
    dialog.appendChild(titleEl);

    Element bodyEl = Document.get().createDivElement().cast();
    bodyEl.setClassName("move-rejected-text");
    bodyEl.setInnerText(message);
    dialog.appendChild(bodyEl);

    Element hintEl = Document.get().createDivElement().cast();
    hintEl.setClassName("move-rejected-close-hint");
    hintEl.setInnerText(CONSTANTS.rulesClickToClose());
    dialog.appendChild(hintEl);

    Document.get().getBody().appendChild(dialog);
    showModalAndBindClose(dialog);
  }

  /**
   * Opens the dialog in the top layer and anchors it over the board (the .canvasWrapper area),
   * in its lower half so it sits just above the card row. The board has no zoom, unlike the card
   * row whose mobile zoom would otherwise composite over the popup. Any click dismisses it.
   */
  private static native void showModalAndBindClose(Element dialog) /*-{
    if (dialog.showModal) {
      dialog.showModal();
    } else {
      dialog.setAttribute('open', '');
    }
    var board = document.querySelector('.canvasWrapper');
    if (board) {
      var r = board.getBoundingClientRect();
      if (r.width > 0 && r.height > 0) {
        dialog.style.top = (r.top + r.height * 0.55) + 'px';
        dialog.style.left = (r.left + r.width / 2) + 'px';
      }
    }
    dialog.addEventListener('click', function() {
      if (dialog.close) dialog.close();
      if (dialog.parentNode) dialog.parentNode.removeChild(dialog);
    });
  }-*/;
}
