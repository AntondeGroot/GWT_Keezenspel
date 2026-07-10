package adg.services.preview;

import java.util.List;

/** Renders the dev preview pages (the scenario list and a single scenario) as HTML strings. */
final class PreviewHtml {

  private static final String PAGE_TEMPLATE =
      """
      <!DOCTYPE html>
      <html lang="en">
      <head>
        <meta charset="UTF-8">
        <title>{{title}}</title>
        <style>
          *, *::before, *::after { box-sizing: border-box; }
          body    { margin: 0; padding: 2rem 1.5rem; background: #12122a; color: #e0e0e0;
                    font-family: system-ui, sans-serif; }
          h1      { margin: 0 0 .4rem; color: #fff; font-size: 1.5rem; }
          .hint   { margin: 0 0 1.5rem; color: #888; font-size: .9rem; }
          table   { border-collapse: collapse; width: 100%; max-width: 820px; }
          th      { padding: .45rem 1rem; text-align: left; color: #aaa; font-size: .75rem;
                    text-transform: uppercase; letter-spacing: .06em;
                    border-bottom: 2px solid rgba(255,255,255,.12); }
          td      { padding: .6rem 1rem; border-bottom: 1px solid rgba(255,255,255,.06);
                    vertical-align: middle; }
          tr:hover td { background: rgba(255,255,255,.04); }
          .url    { font-family: monospace; font-size: .85rem; color: gold; white-space: nowrap; }
          .btn    { display: inline-block; padding: .4rem 1.1rem; border-radius: 5px;
                    font-size: .88rem; font-weight: 700; text-decoration: none;
                    border: none; white-space: nowrap; cursor: pointer; }
          .btn:hover { opacity: .82; }
          .primary { background: gold; color: #111; }
          .ghost   { background: rgba(255,255,255,.07); color: #ccc;
                     border: 1px solid rgba(255,255,255,.18); }
          .desc   { color: #bbb; margin: 0 0 1.75rem; }
          .actions { display: flex; gap: .75rem; flex-wrap: wrap; margin-bottom: 2rem; }
          .back   { color: #888; font-size: .85rem; text-decoration: none; }
          .back:hover { color: #ccc; }
        </style>
      </head>
      <body>{{body}}</body>
      </html>
      """;

  private PreviewHtml() {}

  /** The scenario list: a table of paths, descriptions, and "Open" buttons. */
  static String listPage(List<PreviewScenarios.Scenario> scenarios) {
    var rows = new StringBuilder();
    for (PreviewScenarios.Scenario s : scenarios) {
      rows.append("<tr>")
          .append("<td class='url'>").append(esc("/preview/" + s.path())).append("</td>")
          .append("<td>").append(esc(s.label())).append("</td>")
          .append("<td><a href='preview/").append(esc(s.path())).append("' class='btn primary'>Open</a></td>")
          .append("</tr>");
    }
    String body =
        "<h1>Keezen Scenario Previews</h1>"
            + "<p class='hint'>Each button creates a fresh game session &mdash; "
            + "open multiple browser tabs for multi-player testing.</p>"
            + "<table>"
            + "<thead><tr><th>Path</th><th>Scenario</th><th></th></tr></thead>"
            + "<tbody>" + rows + "</tbody>"
            + "</table>";
    return page("Keezen Previews", body);
  }

  /** A single scenario: its description plus one "open as" button per player. */
  static String scenarioPage(String scenario, String desc, List<PreviewScenarios.PlayerLink> links) {
    var actions = new StringBuilder();
    for (PreviewScenarios.PlayerLink l : links) {
      String cls = l.isPrimary() ? "btn primary" : "btn ghost";
      actions.append("<a href='").append(esc(l.url())).append("' class='").append(cls).append("'>")
          .append(esc(l.label())).append("</a>");
    }
    String body =
        "<h1>Scenario: " + esc(scenario) + "</h1>"
            + "<p class='desc'>" + esc(desc) + "</p>"
            + "<div class='actions'>" + actions + "</div>"
            + "<a href='../preview' class='back'>&#8592; back to list</a>";
    return page("Preview: " + scenario, body);
  }

  private static String page(String title, String body) {
    return PAGE_TEMPLATE.replace("{{title}}", esc(title)).replace("{{body}}", body);
  }

  private static String esc(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;");
  }
}
