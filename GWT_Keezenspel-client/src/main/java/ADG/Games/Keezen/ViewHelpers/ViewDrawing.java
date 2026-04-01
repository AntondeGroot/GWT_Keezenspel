package ADG.Games.Keezen.ViewHelpers;

import static ADG.Games.Keezen.player.PlayerColors.*;

import ADG.Games.Keezen.PawnAndCardSelection;
import ADG.Games.Keezen.Player.PawnHighlightColors;
import ADG.Games.Keezen.TileId;
import ADG.Games.Keezen.dto.PawnClient;
import ADG.Games.Keezen.dto.PlayerClient;
import ADG.Games.Keezen.util.PawnLayout;
import ADG.Games.Keezen.util.UUID;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.*;
import java.util.List;
import java.util.stream.Collectors;

public class ViewDrawing {

  private static final String pawnSvgTemplate = PawnResources.INSTANCE.pawnSvg().getText();

  public static DivElement createPawn(PawnClient pawn, PawnAndCardSelection pawnAndCardSelection, Runnable onSelectionChanged) {
    GWT.log("A pawn was created");
    DivElement pawnElement = Document.get().createDivElement();
    pawnElement.setClassName("pawnDiv");
    pawnElement.setId(pawn.getPawnId());
    pawnElement.setAttribute("data-uuid", UUID.get());

    DivElement pawnImage = Document.get().createDivElement();
    pawnImage.addClassName("pawnImage");
    pawnImage.getStyle().setPosition(Style.Position.ABSOLUTE);
    pawnImage.getStyle().setHeight(PawnLayout.HEIGHT, Style.Unit.PX);
    pawnImage.getStyle().setWidth(PawnLayout.WIDTH, Style.Unit.PX);

    // Compute main color (sent as hex by server) and a darker shade for collar/base
    String mainColor = pawn.getUri();
    int[] darkRgb = darkenColor(hexToRgb(mainColor));
    String darkColor = rgbToHex(darkRgb);

    injectSvgAndSetColors(pawnImage, pawnSvgTemplate, mainColor, darkColor);

    pawnElement.appendChild(pawnImage);

    Event.sinkEvents(pawnElement, Event.ONCLICK);
    Event.setEventListener(
        pawnElement,
        new EventListener() {
          @Override
          public void onBrowserEvent(Event event) {
            if (DOM.eventGetType(event) == Event.ONCLICK) {
              pawnAndCardSelection.addPawnId(pawn.getPawnId());
              GWT.log("Pawn and card selection is: " + pawnAndCardSelection);

              String pawn1Id = pawnAndCardSelection.getPawn1() != null
                  ? pawnAndCardSelection.getPawn1().getPawnId() : null;
              String pawn2Id = pawnAndCardSelection.getPawn2() != null
                  ? pawnAndCardSelection.getPawn2().getPawnId() : null;
              String pawn1Color = pawnAndCardSelection.getPawn1() != null
                  ? pawnAndCardSelection.getPawn1().getUri() : null;
              String pawn2Color = pawnAndCardSelection.getPawn2() != null
                  ? pawnAndCardSelection.getPawn2().getUri() : null;
              updateAllPawnHighlights(pawn1Id, pawn2Id, pawn1Color, pawn2Color);
              if (onSelectionChanged != null) {
                onSelectionChanged.run();
              }

              GWT.log("Pawn and card selection is after validation: " + pawnAndCardSelection);
            }
          }
        });
    GWT.log("a pawn was created");
    return pawnElement;
  }

  private static native void injectSvgAndSetColors(
      Element container, String svgText, String mainColor, String darkColor) /*-{
    container.innerHTML = svgText;
    var svg = container.querySelector('svg');
    if (!svg) return;
    svg.setAttribute('width', '50');
    svg.setAttribute('height', '50');
    var highlights = svg.querySelectorAll('.highlight');
    for (var i = 0; i < highlights.length; i++) {
      highlights[i].style.opacity = '0';
    }
    var head = svg.querySelector('#headFill');
    var body = svg.querySelector('#bodyFill');
    var collar = svg.querySelector('#collarFill');
    var base = svg.querySelector('#baseFill');
    if (head) head.style.fill = mainColor;
    if (body) body.style.fill = mainColor;
    if (collar) collar.style.fill = darkColor;
    if (base) base.style.fill = darkColor;
  }-*/;

  private static void updateAllPawnHighlights(
      String pawn1Id, String pawn2Id, String pawn1Color, String pawn2Color) {
    String pawn1HighlightColor = PawnHighlightColors.forPawn1(pawn1Color);
    String pawn2HighlightColor = PawnHighlightColors.forPawn2(pawn2Color);
    renderPawnHighlights(pawn1Id, pawn2Id, pawn1HighlightColor, pawn2HighlightColor);
    updateStepBoxColors(pawn1HighlightColor, pawn2HighlightColor);
  }

  private static native void renderPawnHighlights(
      String pawn1Id, String pawn2Id, String pawn1HighlightColor, String pawn2HighlightColor) /*-{
    var pawnDivs = $doc.querySelectorAll('.pawnDiv');
    for (var i = 0; i < pawnDivs.length; i++) {
      var pawnDiv = pawnDivs[i];
      var pawnId = pawnDiv.id;

      var isPawn1 = pawn1Id && pawnId === pawn1Id;
      var isPawn2 = pawn2Id && pawnId === pawn2Id;
      var visible = isPawn1 || isPawn2;
      var svg = pawnDiv.querySelector('svg');
      if (!svg) continue;

      var highlights = svg.querySelectorAll('.highlight');

      for (var j = 0; j < highlights.length; j++) {
        var color = isPawn2 ? pawn2HighlightColor : pawn1HighlightColor;

        highlights[j].style.opacity = visible ? '1' : '0';

        if (visible) {
          highlights[j].style.fill = 'none';
          highlights[j].style.stroke = color;
          highlights[j].style.strokeWidth = '64';
          highlights[j].style.strokeLinejoin = 'round';
        } else {
          highlights[j].style.stroke = 'none';
        }
      }
    }
  }-*/;

  public static void clearPawnHighlights() {
    renderPawnHighlights(null, null, null, null);
    updateStepBoxColors(null, null);
  }

  public static void clearPawnHighlightsExceptPawn1(String pawn1Id, String pawn1Color) {
    String pawn1HighlightColor = PawnHighlightColors.forPawn1(pawn1Color);
    renderPawnHighlights(pawn1Id, null, pawn1HighlightColor, null);
    updateStepBoxColors(null, null);
  }

  private static native void updateStepBoxColors(
      String pawn1HighlightColor, String pawn2HighlightColor) /*-{
    var labels1 = $doc.querySelectorAll('.pawn1Label');
    for (var a = 0; a < labels1.length; a++) labels1[a].style.color = pawn1HighlightColor;
    var boxes1 = $doc.querySelectorAll('.TextBoxForPawnSteps1');
    for (var b = 0; b < boxes1.length; b++) boxes1[b].style.borderColor = pawn1HighlightColor;
    var labels2 = $doc.querySelectorAll('.pawn2Label');
    for (var c = 0; c < labels2.length; c++) labels2[c].style.color = pawn2HighlightColor;
    var boxes2 = $doc.querySelectorAll('.TextBoxForPawnSteps2');
    for (var d = 0; d < boxes2.length; d++) boxes2[d].style.borderColor = pawn2HighlightColor;
  }-*/;

  public static DivElement createCircle(
      TileId tileId, double x, double y, double radius, String color) {
    // Create a new <div> element
    DivElement tileElement = Document.get().createDivElement();

    DivElement tileHighlight = Document.get().createDivElement();
    tileHighlight.getStyle().setPosition(Position.ABSOLUTE);
    tileHighlight.getStyle().setWidth(100, Unit.PCT);
    tileHighlight.getStyle().setHeight(100, Unit.PCT);
    tileHighlight.addClassName("tile-highlight");
    tileHighlight.setId(tileId + "Highlight");
    DivElement tile = Document.get().createDivElement();

    // Add the 'circle' class to the element
    tile.setClassName("tile");

    // Set the position using absolute coordinates relative to the container
    tileElement.getStyle().setPosition(Position.ABSOLUTE);
    tileElement.getStyle().setLeft(x, Style.Unit.PX);
    tileElement.getStyle().setTop(y, Style.Unit.PX);

    // Set the size of the circle dynamically
    // todo: by making the circle a little smaller '-3' the indicator for possible moves is no
    // longer exactly aligned, this can be seen for larger values e.g. -5
    tile.getStyle().setWidth(radius * 2 - 3, Style.Unit.PX);
    tile.getStyle().setHeight(radius * 2 - 3, Style.Unit.PX);

    // Ensure it remains a circle by setting border-radius to 50%
    String darkColor = rgbToHex(darkenColor(hexToRgb(color)));
    String lightColor = rgbToHex(lightenColor(hexToRgb(color)));
    tile.getStyle().setProperty("backgroundColor", color);
    tile.getStyle()
        .setProperty(
            "boxShadow",
            "inset 3px 3px 4px " + darkColor + "," + "  inset -4px -4px 4px " + lightColor);

    tileElement.appendChild(tileHighlight);
    tileElement.appendChild(tile);
    return tileElement;
  }

  public static Grid createPlayerGrid(List<PlayerClient> players) {
    GWT.log("creates player grid");
    // todo: maybe check player whether they belong in column 1 or 2, so don't expand both when a
    // winner has been declared
    int colCount = 1;
    int rowCount = players.size();
    Grid grid = new Grid(rowCount, colCount);

    List<PlayerClient> winners =
        players.stream().filter(player -> player.getPlace() > -1).collect(Collectors.toList());

    int playerId = 0;
    for (PlayerClient player : players) {
      int imagePixelSize = 50;

      HorizontalPanel hp = new HorizontalPanel();
      hp.getElement().setId("player" + player.getPlayerInt());
      Label playerNameLabel = new Label("" + player.getName());
      playerNameLabel.getElement().setId(player.getPlayerInt() + "Label");

      hp.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
      hp.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

      Image profilePic = new Image("/profile-pic/" + player.getProfilePictureUrl());
      profilePic.setWidth(imagePixelSize + "px");
      profilePic.setHeight(imagePixelSize + "px");
      profilePic.setStyleName("profilepic");
      profilePic.getElement().setId(player.getPlayerInt() + "Pic");

      if (player.isPlaying()) {
        hp.asWidget().setStyleName("playerPlaying");
      }
      // add an empty medal element
      Canvas canvasMedal = Canvas.createIfSupported();
      canvasMedal.getElement().setId(player.getName() + "Medal");
      canvasMedal.setWidth("0px");
      canvasMedal.setHeight("0px");
      hp.add(canvasMedal.asWidget());

      hp.add(profilePic);
      hp.add(playerNameLabel);
      grid.setWidget(playerId, 0, hp);

      playerId++;
    }

    grid.getElement().getStyle().setMargin(50, Style.Unit.PX);
    return grid;
  }

public static void updatePlayerProfileUI(List<PlayerClient> players) {

    GWT.log("Players were updated: " + players);
    for (PlayerClient player : players) {
      // set color of border around profile pic:
      GWT.log("Player: " + player.getName());
      String playerColor = player.getColor(); // gethex color
      GWT.log("playercolor = " + playerColor);
      String INACTIVE_GREY = "#c2bfb6";
      GWT.log("trying to get document");
      Document.get()
          .getElementById(player.getPlayerInt() + "Pic")
          .getStyle()
          .setBorderColor(player.isActive() ? playerColor : INACTIVE_GREY);
      GWT.log("finished getting document");
      // set player name label : there's a strikethrough when player is not active
      Element playerLabel = Document.get().getElementById(player.getPlayerInt() + "Label");
      playerLabel.setClassName(player.isActive() ? "activePlayerName" : "inactivePlayerName");
      playerLabel.addClassName("playerName");

      // set border for Horizontal Panel
      Element hp = Document.get().getElementById("player" + player.getPlayerInt());
      hp.setClassName(player.isPlaying() ? "playerPlaying" : "playerNotPlaying");
      hp.addClassName(player.isActive() ? "playerActive" : "playerInactive");
    }

    drawMedals(players);
  }

  private static void drawMedals(List<PlayerClient> players) {
    for (PlayerClient player : players) {
      if (player.getPlace() > -1) {
        // get element
        CanvasElement canvasMedal =
            (CanvasElement) Document.get().getElementById(player.getName() + "Medal");
        canvasMedal.setClassName("Medal" + player.getPlace());

        // fill element
        int imagePixelSize = 50;
        canvasMedal.setWidth(imagePixelSize); // drawing resolution
        canvasMedal.setHeight(imagePixelSize);
        canvasMedal.getStyle().setWidth(imagePixelSize, Style.Unit.PX); // visual size
        canvasMedal.getStyle().setHeight(imagePixelSize, Style.Unit.PX); // visual size

        ImageElement imgMedals = (ImageElement) Document.get().getElementById("medals").cast();

        // image has 220px empty space on top and bottom
        // and is 2500px wide and 1668px high
        double sw1 = 2500 / 3.0;
        double sh1 = 1668 - 440;
        double sx1 = sw1 * (player.getPlace() - 1);
        double sy1 = 220;
        // destination
        double dy1 = 0;
        double dx1 = 0;
        double dh1 = imagePixelSize + 5;
        double dw1 = dh1 / sh1 * sw1;

        Context2d ctxMedals = canvasMedal.getContext2d();
        ctxMedals.drawImage(imgMedals, sx1, sy1, sw1, sh1, dx1, dy1, dw1, dh1);
      }
    }
  }
}
