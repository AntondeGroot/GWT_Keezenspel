package ADG.Games.Keezen.ViewHelpers;

import ADG.Games.Keezen.PawnAndCardSelection;
import ADG.Games.Keezen.TileId;
import ADG.Games.Keezen.dto.PawnClient;
import ADG.Games.Keezen.dto.PlayerClient;
import ADG.Games.Keezen.dto.PlayerClient;
import ADG.Games.Keezen.moving.Move;
import ADG.Games.Keezen.util.UUID;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ADG.Games.Keezen.Player.PlayerColors.*;

public class ViewDrawing {

  public static DivElement createPawn(PawnClient pawn, PawnAndCardSelection pawnAndCardSelection) {
    // Create new <div> Element
    GWT.log("A pawn was created");
    DivElement pawnElement = Document.get().createDivElement();
    pawnElement.setClassName("pawnDiv");
    String pawnId = null;

    pawnElement.setId(pawn.getPawnId());
    String uuid = UUID.get();
    pawnElement.setAttribute("data-uuid", uuid); // for debugging to see when the pawns are replaced, which happens after refreshing the page

    // Create new <div> Element
    DivElement pawnImage = Document.get().createDivElement();
    pawnImage.addClassName("pawnImage");

    // set image
    pawnImage.getStyle().setProperty("backgroundImage", "url(" + pawn.getUri() + ")");

    // set overlay for when you want to select the pawn
    ImageElement overlayImage = Document.get().createImageElement();
    overlayImage.setSrc("/pawn_outline.png");
    overlayImage.setClassName(pawn.getPawnId() + "Overlay");
    overlayImage.getStyle().setPosition(Style.Position.ABSOLUTE);
    overlayImage.getStyle().setTop(0, Style.Unit.PX);
    overlayImage.getStyle().setLeft(0, Style.Unit.PX);
    overlayImage.getStyle().setWidth(100, Style.Unit.PCT);
    overlayImage.getStyle().setHeight(100, Style.Unit.PCT);
    overlayImage.getStyle().setVisibility(Style.Visibility.HIDDEN);

    // set position
    pawnImage.getStyle().setPosition(Style.Position.ABSOLUTE);
    pawnImage.appendChild(overlayImage);

    // set width
    pawnImage.getStyle().setHeight(40, Style.Unit.PX);
    pawnImage.getStyle().setWidth(40, Style.Unit.PX);

    //combine
    pawnImage.appendChild(overlayImage);  // add overlay inside pawnImage
    pawnElement.appendChild(pawnImage);  // add pawnImage to outer container
    pawnElement.getStyle().setZIndex(10); // put it on top of any canvas elements

    Event.sinkEvents(pawnElement, Event.ONCLICK);
    Event.setEventListener(pawnElement, new EventListener() {
      @Override
      public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONCLICK) {
          // select the pawnId when you click on the div
          // we do not want to add the pawn itself as it will contain an outdated currentposition
          // let pawnandcardselection keep track of where pawns are based on polling the server.
          pawnAndCardSelection.addPawnId(pawn.getPawnId());
          GWT.log("Pawn and card selection is: " + pawnAndCardSelection);

          // each time you click on a pawn, you need to check all other pawns
          // whether they should be selected or not.
          // there is logic in pawnAndCardSelection so it might have unselected a pawn
          // in the model.
          NodeList<Element> overlayImages = Document.get().getElementsByTagName("img");
          for (int i = 0; i < overlayImages.getLength(); i++) {
            Element element = overlayImages.getItem(i);
            String className = element.getClassName(); // e.g., "PawnId{0,0}Overlay"

            // Extract pawnId by removing "Overlay" suffix
            if (className.endsWith("Overlay")) {
              String pawnId = className.substring(0, className.length() - "Overlay".length());
              // compare with selected pawns
              boolean isSelected = false;
              if (pawnAndCardSelection.getPawn1() != null && pawnAndCardSelection.getPawn1()
                  .getPawnId().equals(pawnId)) {
                isSelected = true;
              } else if (pawnAndCardSelection.getPawn2() != null && pawnAndCardSelection.getPawn2()
                  .getPawnId().equals(pawnId)) {
                isSelected = true;
              }

              element.getStyle()
                  .setVisibility(isSelected ? Style.Visibility.VISIBLE : Style.Visibility.HIDDEN);
            }
          }
          GWT.log("Pawn and card selection is after validation: " + pawnAndCardSelection);
          // after you have clicked on a pawn you will test whether it can move
//          Move.testMove(pawnAndCardSelection.createTestMoveMessage());
        }
      }
    });
    GWT.log("a pawn was created");
    return pawnElement;
  }

  public static DivElement createCircle(TileId tileId, double x, double y, double radius,
      String color) {
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
    //todo: by making the circle a little smaller '-3' the indicator for possible moves is no longer exactly aligned, this can be seen for larger values e.g. -5
    tile.getStyle().setWidth(radius * 2 - 3, Style.Unit.PX);
    tile.getStyle().setHeight(radius * 2 - 3, Style.Unit.PX);

    // Ensure it remains a circle by setting border-radius to 50%
    String darkColor = rgbToHex(darkenColor(hexToRgb(color)));
    String lightColor = rgbToHex(lightenColor(hexToRgb(color)));
    tile.getStyle().setProperty("backgroundColor", color);
    tile.getStyle().setProperty("boxShadow", "inset 3px 3px 4px " + darkColor + "," +
        "  inset -4px -4px 4px " + lightColor);

    tileElement.appendChild(tileHighlight);
    tileElement.appendChild(tile);
    return tileElement;
  }

  public static Grid createPlayerGrid(List<PlayerClient> players) {
    GWT.log("creates player grid");
    // todo: maybe check player whether they belong in column 1 or 2, so don't expand both when a winner has been declared
    List<Integer> column1 = Arrays.asList(0, 1, 2, 3);
    List<Integer> column2 = Arrays.asList(4, 5, 6, 7);
    int colCount = 2;
    // for a maximum of 8 players, there are either 4 rows and 2 columns
    // or there are between 1 and 4 rows
    int rowCount = Math.min(players.size(), 4);
    Grid grid = new Grid(rowCount, colCount);

    List<PlayerClient> winners = players.stream()
        .filter(player -> player.getPlace() > -1)
        .collect(Collectors.toList());

    int playerId = 0;
    for (PlayerClient player : players) {
      int imagePixelSize = 50;

      ImageElement img = Document.get().createImageElement();
      img.setSrc("/profilepics.png");

      HorizontalPanel hp = new HorizontalPanel();
      hp.getElement().setId(""+player.getPlayerInt());
      Label playerNameLabel = new Label(""+player.getName());
      playerNameLabel.getElement().setId(player.getPlayerInt() + "Label");

      hp.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
      hp.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

      Canvas canvas = Canvas.createIfSupported();
      canvas.setWidth(imagePixelSize + "px");
      canvas.setHeight(imagePixelSize + "px");
      canvas.setCoordinateSpaceWidth(imagePixelSize);
      canvas.setCoordinateSpaceHeight(imagePixelSize);
      canvas.setStyleName("profilepic");
      canvas.getElement().setId(player.getPlayerInt() + "Pic");

      Context2d ctx = canvas.getContext2d();
      // source image
      //todo: use img.width or something instead
      // move this to the server, users should have chosen their profile pic
      double sw = 1024 / 4.0;
      double sh = 1024 / 4.0;
      double sx = sw * (playerId % 4);
      double sy = sh * (playerId > 3 ? 1 : 0);
      // destination
      double dy = -5 / 2.0;
      double dx = -5 / 2.0;
      double dw = imagePixelSize + 5;
      double dh = imagePixelSize + 5;

      // for spritesheets dx dy
      ctx.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
      canvas.asWidget().setStyleName("profilepic");
      if (player.isPlaying()) {
        hp.asWidget().setStyleName("playerPlaying");
      }

      // todo: the following text is not implemented
      // winners can be in either column 1 or 2: only add a medal to the winner,
      // but add an empty canvas to the other players in that column so that they are aligned
      // todo: medals are now not implemented
      Canvas canvasMedal = Canvas.createIfSupported();
      canvasMedal.getElement().setId(player.getName() + "Medal");
      canvasMedal.setWidth("0px");
      canvasMedal.setHeight("0px");

      hp.add(canvasMedal.asWidget());
      hp.add(canvas.asWidget());
      hp.add(playerNameLabel);
      int row = playerId % 4;
      int col = playerId > 3 ? 1 : 0;
      grid.setWidget(row, col, hp);

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
      String playerColor = player.getColor();//gethex color
      GWT.log("playercolor = "+playerColor);
      String INACTIVE_GREY = "#c2bfb6";
      GWT.log("trying to get document");
      Document.get()
          .getElementById(player.getPlayerInt() + "Pic")
          .getStyle()
          .setBorderColor(
              player.isActive() ? playerColor : INACTIVE_GREY);
      GWT.log("finished getting document");
      // set player name label : there's a strikethrough when player is not active
      Element playerLabel = Document.get().getElementById(player.getPlayerInt() + "Label");
      playerLabel.setClassName(player.isActive() ? "activePlayerName" : "inactivePlayerName");
      playerLabel.addClassName("playerName");

      // set border for Horizontal Panel
      Element hp = Document.get().getElementById(""+player.getPlayerInt());
      hp.setClassName(player.isPlaying() ? "playerPlaying" : "playerNotPlaying");
      hp.addClassName(player.isActive() ? "playerActive" : "playerInactive");
    }

    drawMedals(players);
  }

  private static void drawMedals(List<PlayerClient> players) {
    for (PlayerClient player : players) {
      if (player.getPlace() > -1) {
        // get element
        CanvasElement canvasMedal = (CanvasElement) Document.get()
            .getElementById(player.getName() + "Medal");
        canvasMedal.setClassName("Medal" + player.getPlace());

        // fill element
        int imagePixelSize = 50;
        canvasMedal.setWidth(imagePixelSize); // drawing resolution
        canvasMedal.setHeight(imagePixelSize);
        canvasMedal.getStyle().setWidth(imagePixelSize, Style.Unit.PX); // visual size
        canvasMedal.getStyle().setHeight(imagePixelSize, Style.Unit.PX); // visual size

        ImageElement imgMedals = Document.get().createImageElement();
        imgMedals.setSrc("/medals.png");

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
