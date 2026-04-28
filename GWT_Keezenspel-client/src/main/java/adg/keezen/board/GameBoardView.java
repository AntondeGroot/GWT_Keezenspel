package adg.keezen.board;

import static adg.keezen.viewhelpers.ViewDrawing.clearPawnHighlightsExceptPawn1;
import static adg.keezen.viewhelpers.ViewDrawing.createCircle;
import static adg.keezen.viewhelpers.ViewDrawing.createPawn;
import static adg.keezen.viewhelpers.ViewDrawing.createPlayerGrid;
import static adg.keezen.util.PlayerUtil.getPlayerById;

import adg.keezen.CardsDeck;
import adg.keezen.PawnAndCardSelection;
import adg.keezen.i18n.AppConstants;
import adg.keezen.Point;
import adg.keezen.TileId;
import adg.keezen.TileMapping;
import adg.keezen.animations.PawnAnimation;
import adg.keezen.animations.StepsAnimation;
import adg.keezen.dto.CardClient;
import adg.keezen.dto.MoveResponseDTO;
import adg.keezen.dto.PawnClient;
import adg.keezen.dto.PlayerClient;
import adg.keezen.dto.TestMoveResponseDTO;
import adg.keezen.services.ApiClient;
import adg.keezen.services.ApiClient.ApiCallback;
import adg.keezen.util.Cookie;
import adg.keezen.util.PawnLayout;
import adg.keezen.util.MoveRequestJsonBuilder;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import java.util.*;

public class GameBoardView extends Composite {

  private static final AppConstants CONSTANTS = GWT.create(AppConstants.class);

  private final Document document;

  interface Binder extends UiBinder<Widget, GameBoardView> {}

  private static final Binder uiBinder = GWT.create(Binder.class);

  @UiField HTMLPanel sendButtonWrapper;

  @UiField Button sendButton;

  @UiField Button forfeitButton;

  @UiField Button leaveGameButton;

  @UiField Label errorLabel;

  @UiField VerticalPanel playerListContainer2;

  //    needed to add click listeners to the Canvas. GWT Canvas does not have
  //    a dom element which you could find by .findElementById()
  @UiField HTMLPanel canvasWrapper;

  @UiField HTMLPanel tileBoard;

  @UiField HTMLPanel pawnBoard;

  @UiField FlowPanel cardsContainer;

  @UiField Label cardHintLabel;

  @UiField HorizontalPanel pawnIntegerBoxes;

  @UiField Label pawn1Label;

  @UiField Label pawn2Label;

  @UiField TextBox stepsPawn1;

  @UiField TextBox stepsPawn2;

  @UiField HorizontalPanel chatInputRow;

  @UiField TextArea chatDisplayField;

  @UiField TextBox chatInputField;

  @UiField Button chatSendButton;

  @Override
  public void onLoad() {
    super.onLoad();
  }

  private final Map<String, DivElement> pawnElements = new HashMap<>();

  public GameBoardView() {
    initWidget(uiBinder.createAndBindUi(this));
    document = Document.get();
    stepsPawn1.setValue("7");
    stepsPawn2.setValue("0");
    pawnIntegerBoxes.getElement().setId("pawnIntegerBoxes");

    sendButton.setText(CONSTANTS.playCard());
    forfeitButton.setText(CONSTANTS.forfeit());
    leaveGameButton.setText(CONSTANTS.leaveGame());
    chatSendButton.setText(CONSTANTS.send());
    pawn1Label.setText(CONSTANTS.pawn1());
    pawn2Label.setText(CONSTANTS.pawn2());
  }

  public int getStepsPawn1() {
    if (stepsPawn1.getValue().isEmpty()) {
      return 0;
    }
    return Integer.parseInt(stepsPawn1.getValue());
  }

  public int getStepsPawn2() {
    if (stepsPawn2.getValue().isEmpty()) {
      return 0;
    }
    return Integer.parseInt(stepsPawn2.getValue());
  }

  public void showPawnTextBoxes(boolean show) {
    pawnIntegerBoxes.setVisible(show);
  }

  public Button getSendButton() {
    return sendButton;
  }

  public Button getForfeitButton() {
    return forfeitButton;
  }

  public Button getLeaveGameButton() {
    return leaveGameButton;
  }

  public VerticalPanel getPlayerListContainer() {
    return playerListContainer2;
  }

  public Context2d getCanvasCardsContext() {
    return ((CanvasElement) document.getElementById("canvasCards2")).getContext2d();
  }

  public CanvasElement getCanvasCards() {
    return (CanvasElement) document.getElementById("canvasCards2");
  }

  public void createPlayerList(List<PlayerClient> players) {
    playerListContainer2.clear();
    playerListContainer2.add(createPlayerGrid(players));
  }

  public void animatePawns(MoveResponseDTO moveResponse, Runnable onComplete) {
    GWT.log("view.animatepawns");
    PawnAnimation.animateSequence(pawnElements, moveResponse, onComplete);
  }

  public void drawBoard(List<TileMapping> tiles, List<PlayerClient> players, double cellDistance) {
    GWT.log("drawing board");

    for (TileMapping mapping : tiles) {
      String color = "#f2f2f2";
      int tileNr = mapping.getTileNr();
      // only player tiles get a color
      if (tileNr <= 0 || tileNr >= 16) {
        PlayerClient player = getPlayerById(mapping.getPlayerId(), players);
        color = player.getColor();
      }
      DivElement circle =
          createCircle(
              mapping.getTileId(),
              mapping.getPosition().getX() - cellDistance / 2,
              mapping.getPosition().getY() - cellDistance / 2,
              cellDistance / 2,
              color);
      tileBoard.getElement().appendChild(circle);
    }
  }

  public void enableButtons(Boolean enabled) {
    sendButton.setEnabled(enabled);
    forfeitButton.setEnabled(enabled);
  }

  public void setSendButtonAnimating(boolean animating) {
//    if (animating) {
//      DivElement loader = Document.get().createDivElement();
//      loader.setClassName("sendButtonLoader");
//      loader.setId("sendButtonLoader");
//      sendButtonWrapper.getElement().appendChild(loader);
//    } else {
//      Element loader = Document.get().getElementById("sendButtonLoader");
//      if (loader != null) {
//        loader.removeFromParent();
//      }
//    }
  }

  public void createPawns(List<PawnClient> pawns, PawnAndCardSelection pawnAndCardSelection, Runnable onPawnSelected) {
    Point point = new Point(0, 0);
    GWT.log("createPawns : " + pawns.size() + " pawns to be drawn");
    for (PawnClient pawn : pawns) {
      GWT.log("pooo");
      GWT.log("" + pawn.getPawnId());
      GWT.log("" + pawn.getPlayerId());
      String pawnId = pawn.getPawnId();
      GWT.log("pawnId: " + pawnId);
      DivElement pawnElement = pawnElements.get(pawn.getPawnId());
      if (pawnElement == null) {
        pawnElement = createPawn(pawn, pawnAndCardSelection, onPawnSelected);
        pawnElements.put(pawn.getPawnId(), pawnElement);
        pawnBoard.getElement().appendChild(pawnElement);
      }

      for (TileMapping mapping : Board.getTiles()) {
        if (mapping.getTileId().equals(pawn.getCurrentTileId())) {
          point = mapping.getPosition();
          System.out.println("");
        }
      }
      PawnLayout.applyPosition(pawnElement, point);
    }
  }

  public void drawCardsIcons(HashMap<String, Integer> nrCardsPerPlayerUUID, Image spriteImage) {
    GWT.log("drawCardsIcons");
    for (Map.Entry<String, Integer> entry : nrCardsPerPlayerUUID.entrySet()) {
      String uuid = entry.getKey();
      if (uuid.equals(Cookie.getPlayerId())) {
        // skip drawing card icons for the current player, their hand is already drawn showing the
        // cards
        // so you don't need to draw the back side to indicate how many cards they currently have
        continue;
      }
      ArrayList<Point> cardpoints = Board.getCardsDeckPointsForPlayer(uuid);
      Point startPoint = cardpoints.get(0);
      Point endPoint = cardpoints.get(1);
      double dx = (endPoint.getX() - startPoint.getX()) / 4;
      double dy = (endPoint.getY() - startPoint.getY()) / 4;
      GWT.log(
          "drawCardsIcons \n"
              + "PlayerUUID "
              + uuid
              + "\n"
              + "Start "
              + startPoint
              + "\n"
              + "End "
              + endPoint
              + "\n"
              + "dx "
              + dx
              + "\n"
              + "dy "
              + dy);

      Integer nrCards = entry.getValue();
      for (int i = 0; i < nrCards; i++) {
        // Define the source rectangle (from the sprite sheet): this image belongs to the backside
        // image
        double spriteWidth = 1920 / 13.0;
        double spriteHeight = 1150 / 5.0;
        double sourceX = spriteWidth * 2;
        double sourceY = spriteHeight * 4;

        // Define the destination rectangle (on the canvas)
        double imageWidth = 20;
        double destWidth = imageWidth; // Card width on canvas
        double destHeight = destWidth / spriteWidth * spriteHeight; // Maintain aspect ratio
        double destX = startPoint.getX() - destWidth / 2 + dx * i; // Offset for each card
        double destY = startPoint.getY() - destHeight / 2 + dy * i;

        // Draw the card image on the canvas
        getCanvasCardsContext()
            .drawImage(
                ImageElement.as(spriteImage.getElement()),
                sourceX,
                sourceY,
                spriteWidth,
                spriteHeight,
                destX,
                destY,
                destWidth,
                destHeight);
      }
    }
  }

  private void drawPlayerCardsInHand(
      List<CardClient> cards, PawnAndCardSelection pawnAndCardSelection, Image spriteImage) {
    CardClient selectedCard = pawnAndCardSelection.getCard();
    // create divs
    cardsContainer.getElement().removeAllChildren();
    GWT.log("cards have now become stale");

    GWT.log("cards = " + cards);
    for (CardClient card : cards) {
      // Define sprite dimensions
      double spriteWidth = 1920 / 13.0; // One card's width in sprite
      double spriteHeight = 1150 / 5.0; // One card's height in sprite
      double destWidth = 100.0; // Final width on screen
      double factor = destWidth / spriteWidth;
      double destHeight = factor * spriteHeight; // Maintain aspect ratio

      // Calculate correct sprite offset (WITHOUT scaling)
      double sourceX = spriteWidth * (card.getValue() - 1);
      double sourceY = spriteHeight * card.getSuit();

      // Create the card element
      DivElement cardElement = Document.get().createDivElement();
      int v = card.getValue();
      boolean isSpecial = v == 1 || v == 4 || v == 7 || v == 11 || v == 12 || v == 13;
      cardElement.setClassName(isSpecial ? "cardDiv specialCard" : "cardDiv");
      cardElement.setId(card.toString());
      cardElement
          .getStyle()
          .setProperty("backgroundPosition", -sourceX * factor + "px " + -sourceY * factor + "px");

      // Scale entire sprite sheet
      cardElement
          .getStyle()
          .setProperty("backgroundSize", (1920 * factor) + "px " + (1150 * factor) + "px");

      // Set visible card size
      cardElement.getStyle().setProperty("width", destWidth + "px");
      cardElement.getStyle().setProperty("height", destHeight + "px");

      Event.sinkEvents(cardElement, Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);
      Event.setEventListener(
          cardElement,
          new com.google.gwt.user.client.EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
              int eventType = DOM.eventGetType(event);
              if (eventType == Event.ONMOUSEOVER) {
                updateCardHint(card);
                return;
              }
              if (eventType == Event.ONMOUSEOUT) {
                updateCardHint(selectedCard); // revert to selected card, or clear if none
                return;
              }
              if (eventType == Event.ONCLICK) {
                pawnAndCardSelection.setCard(card);
                GWT.log("pawnAndCardSelection = " + pawnAndCardSelection);

                if (pawnAndCardSelection.getCard() == null) {
                  StepsAnimation.resetStepsAnimation();
                  String pawn1Color = pawnAndCardSelection.getPawn1() != null ? pawnAndCardSelection.getPawn1().getUri() : null;
                  clearPawnHighlightsExceptPawn1(pawnAndCardSelection.getPawnId1(), pawn1Color);
                  drawPlayerCardsInHand(cards, pawnAndCardSelection, spriteImage);
                  return;
                }

                // Switching to a different card may have cleared pawn2 (e.g. switching away from
                // a 7). Sync the visual highlight so pawn2 is no longer shown as selected.
                if (pawnAndCardSelection.getPawn2() == null) {
                  String pawn1Color = pawnAndCardSelection.getPawn1() != null ? pawnAndCardSelection.getPawn1().getUri() : null;
                  clearPawnHighlightsExceptPawn1(pawnAndCardSelection.getPawnId1(), pawn1Color);
                }

                MoveRequestJsonBuilder builder =
                    new MoveRequestJsonBuilder()
                        .withPlayerId(Cookie.getPlayerId())
                        .withCardId(pawnAndCardSelection.getCard())
                        .withPawn1(pawnAndCardSelection.getPawn1())
                        .withPawn2(pawnAndCardSelection.getPawn2())
                        .withStepsPawn1(pawnAndCardSelection.getNrStepsPawn1())
                        .withStepsPawn2(pawnAndCardSelection.getNrStepsPawn2())
                        .withTempMessageType("CHECK_MOVE");

                GWT.log("testmove anton: " + builder.build());
                ApiClient apiClient = new ApiClient();
                GWT.log("pawn 1: " + pawnAndCardSelection.getPawn1());
                apiClient.checkMove(
                    Cookie.getSessionID(),
                    Cookie.getPlayerId(),
                    builder.build(),
                    new ApiCallback<TestMoveResponseDTO>() {
                      @Override
                      public void onSuccess(TestMoveResponseDTO result) {
                        GWT.log(" successful XXXX" + result.toString());
                        ArrayList<TileId> tiles = new ArrayList<>();
                        for (int i = 0; i < result.getTiles().length(); i++) {
                          tiles.add(
                              new TileId(
                                  result.getTiles().get(i).getPlayerId(),
                                  result.getTiles().get(i).getTileNr()));
                        }
                        GWT.log("tiles = " + tiles);
                        StepsAnimation.resetStepsAnimation();
                        StepsAnimation.updateStepsAnimation(tiles);
                      }

                      @Override
                      public void onHttpError(int statusCode, String statusText) {
                        GWT.log("anton httperror view" + statusCode + ":" + statusText);
                        StepsAnimation.resetStepsAnimation();
                      }

                      @Override
                      public void onFailure(Throwable caught) {
                        GWT.log("anton failure view" + caught.getMessage());
                        StepsAnimation.resetStepsAnimation();
                      }
                    });

                drawPlayerCardsInHand(cards, pawnAndCardSelection, spriteImage);
              }
            }
          });

      // HorizontalPanel manages layout only for widgets added via add(...),
      // not for raw DOM elements added via getElement().appendChild(...).
      // By manually appending DivElements, you're not triggering the horizontal layout behavior —
      // so the browser defaults to stacking them vertically like block elements.
      // to solve this I used inline-block, but this might not be the most elegant solution
      // Highlight selected card, if any
      if (Objects.equals(card, selectedCard)) {
        cardElement.getStyle().setBorderWidth(3, Style.Unit.PX);
        cardElement.getStyle().setBorderColor("red");
        cardElement.getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        cardElement.getStyle().setProperty("borderRadius", "8px");
      } else {
        cardElement.getStyle().setBorderWidth(0, Style.Unit.PX);
        cardElement.getStyle().clearBorderColor();
        cardElement.getStyle().clearBorderStyle();
      }

      cardsContainer.getElement().appendChild(cardElement);
    }
    updateCardHint(selectedCard);
  }

  private void updateCardHint(CardClient card) {
    if (card == null) {
      cardHintLabel.setText("");
      return;
    }
    String hint;
    switch (card.getValue()) {
      case 1:  hint = CONSTANTS.hintAce();   break;
      case 4:  hint = CONSTANTS.hintFour();  break;
      case 7:  hint = CONSTANTS.hintSeven(); break;
      case 11: hint = CONSTANTS.hintJack();  break;
      case 12: hint = CONSTANTS.hintQueen(); break;
      case 13: hint = CONSTANTS.hintKing();  break;
      default: hint = ""; break;
    }
    cardHintLabel.setText(hint);
  }

  public void drawCards(CardsDeck cardsDeck, PawnAndCardSelection pawnAndCardSelection) {
    List<CardClient> cards = cardsDeck.getCards();
    HashMap<String, Integer> nrCardsPerPlayerUUID = cardsDeck.getNrCardsPerPlayer();
    ArrayList<CardClient> playedCards = (ArrayList<CardClient>) cardsDeck.getPlayedCards();

    // Create an image to represent the card deck
    Image img = new Image(GWT.getHostPageBaseURL() + "card-deck.png");
    img.getElement().setId("card-deck-dynamic");
    GWT.log("drawcards method");
    // Add a LoadHandler to ensure the image is fully loaded before drawing
    img.addLoadHandler(
        new LoadHandler() {
          @Override
          public void onLoad(LoadEvent event) {
            // Clear the canvas to prepare for drawing new cards
            getCanvasCardsContext()
                .clearRect(0, 0, getCanvasCards().getWidth(), getCanvasCards().getHeight());

            GWT.log("\n\ndrawing cards");
            drawPlayerCardsInHand(cards, pawnAndCardSelection, img);
            drawCardsIcons(nrCardsPerPlayerUUID, img);
            drawPlayedCards(playedCards, img);
          }
        });

    // Trigger the image loading by adding it to the DOM
    removeCardDeckImage();
    img.setVisible(false);
    RootPanel.get().add(img);
  }

  private void drawPlayedCards(ArrayList<CardClient> playedCards, Image spriteImage) {
    // Expose count for Selenium tests (canvas content is not DOM-inspectable)
    getCanvasCards().setAttribute("data-played-count", String.valueOf(playedCards.size()));

    // Loop through the cards to draw them
    // the cards are drawn rotating with an angle of 45 degrees
    // meaning that after 8 cards you will draw a card over a previous drawn card
    // In order to not do any unnecessary drawing, we will skip the first N cards
    // if more than 8 cards were drawn.
    int angleDegrees = 45;
    int startDrawingFromCardIndex = 0;
    if (playedCards.size() > 8) {
      startDrawingFromCardIndex = playedCards.size() - 8;
    }

    for (int i = 0; i < playedCards.size(); i++) {
      angleDegrees = angleDegrees + 45;

      if (i >= startDrawingFromCardIndex) {
        CardClient card = playedCards.get(i);
        double angleRadians = Math.toRadians(angleDegrees);

        // Define the source rectangle (from the sprite sheet)
        double spriteWidth = 1920 / 13.0;
        double spriteHeight = 1150 / 5.0;
        double sourceX = spriteWidth * (card.getValue() - 1);
        double sourceY = spriteHeight * card.getSuit();

        // Define the destination rectangle (on the canvas)
        double destWidth = 60.0; // Card width on canvas
        double destHeight = destWidth / spriteWidth * spriteHeight; // Maintain aspect ratio
        double destX =
            300 - destWidth / 4 + 10 * Math.cos(angleRadians); // rotate for each card 45 degrees
        double destY = 300 - destHeight / 2 + 10 * Math.sin(angleRadians);

        // Draw the card image on the canvas
        getCanvasCardsContext()
            .drawImage(
                ImageElement.as(spriteImage.getElement()),
                sourceX,
                sourceY,
                spriteWidth,
                spriteHeight,
                destX,
                destY,
                destWidth,
                destHeight);
      }
    }
  }

  public void removeCardDeckImage() {
    // todo: can this be done any other way? how was it done for the pawns? There it didn't need a
    // loadHandler
    Element el = Document.get().getElementById("card-deck-dynamic");
    if (el != null) {
      el.removeFromParent();
      GWT.log("Image removed successfully.");
    } else {
      GWT.log("Image not found in DOM.");
    }
  }

  public void animatePawnsToPositions(List<PawnClient> pawns) {
    for (PawnClient pawn : pawns) {
      DivElement pawnElement = pawnElements.get(pawn.getPawnId());
      if (pawnElement == null) continue;
      for (TileMapping mapping : Board.getTiles()) {
        if (mapping.getTileId().equals(pawn.getCurrentTileId())) {
          Point point = mapping.getPosition();
          pawnElement.getStyle().setProperty("transition", "left 600ms ease-in-out, top 600ms ease-in-out");
          pawnElement.getOffsetWidth(); // force reflow so transition applies
          PawnLayout.applyPosition(pawnElement, point);
          break;
        }
      }
    }
  }

  public Button getChatSendButton() { return chatSendButton; }

  public void setChatInputRowVisible(boolean visible) { chatInputRow.setVisible(visible); }

  public String getChatInput() { return chatInputField.getText(); }

  public void clearChatInput() { chatInputField.setText(""); }

  public void refreshChat(String chatText) {
    Element el = chatDisplayField.getElement();
    boolean wasAtBottom = el.getScrollTop() + el.getClientHeight() >= el.getScrollHeight() - 5;
    chatDisplayField.setText(chatText);
    if (wasAtBottom) {
      el.setScrollTop(el.getScrollHeight());
    }
  }

  public void clearCanvasCards() {
    getCanvasCardsContext()
        .clearRect(0, 0, getCanvasCards().getWidth(), getCanvasCards().getHeight());
  }
}
