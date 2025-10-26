package ADG.Games.Keezen.board;

import ADG.Games.Keezen.CardsDeck;
import ADG.Games.Keezen.PawnAndCardSelection;
import ADG.Games.Keezen.Point;
import ADG.Games.Keezen.TileId;
import ADG.Games.Keezen.TileMapping;
import ADG.Games.Keezen.animations.AnimationSequence;
import ADG.Games.Keezen.animations.PawnAnimation;
import ADG.Games.Keezen.animations.StepsAnimation;
import ADG.Games.Keezen.dto.CardClient;
import ADG.Games.Keezen.dto.PawnClient;
import ADG.Games.Keezen.dto.PlayerClient;
import ADG.Games.Keezen.dto.PlayerDTO;
import ADG.Games.Keezen.dto.TestMoveResponseDTO;
import ADG.Games.Keezen.moving.Move;
import ADG.Games.Keezen.services.ApiClient;
import ADG.Games.Keezen.services.ApiClient.ApiCallback;
import ADG.Games.Keezen.util.Cookie;
import ADG.Games.Keezen.util.MoveRequestJsonBuilder;
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

import static ADG.Games.Keezen.ViewHelpers.ViewDrawing.*;
import static ADG.Games.Keezen.util.JsUtilities.playersToArrayList;
import static ADG.Games.Keezen.util.PlayerUtil.getPlayerById;

public class GameBoardView extends Composite {

  private final Document document;

  interface Binder extends UiBinder<Widget, GameBoardView> {

  }

  private static final Binder uiBinder = GWT.create(Binder.class);

  @UiField
  Button sendButton;

  @UiField
  Button forfeitButton;

  @UiField
  Label errorLabel;

  @UiField
  VerticalPanel playerListContainer2;

  //    needed to add click listeners to the Canvas. GWT Canvas does not have
  //    a dom element which you could find by .findElementById()
  @UiField
  HTMLPanel canvasWrapper;

  @UiField
  HTMLPanel tileBoard;

  @UiField
  HTMLPanel pawnBoard;

  @UiField
  FlowPanel cardsContainer;

  @UiField
  HorizontalPanel pawnIntegerBoxes;

  @UiField
  TextBox stepsPawn1;

  @UiField
  TextBox stepsPawn2;

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

  public void animatePawns() {
    //pawns.sort(new PawnComparator()); // todo: will pawns be drawn incorrectly when on finish tile?
    GWT.log("view.animatepawns");
    PawnAnimation animation = new PawnAnimation(pawnElements);
    animation.animateSequence(AnimationSequence.getFirst());
    animation.animateSequence(AnimationSequence.getLast());
    AnimationSequence.reset();
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
      DivElement circle = createCircle(mapping.getTileId(),
          mapping.getPosition().getX() - cellDistance / 2,
          mapping.getPosition().getY() - cellDistance / 2, cellDistance / 2, color);
      tileBoard.getElement().appendChild(circle);
    }
  }

  public void enableButtons(Boolean enabled) {
    sendButton.setEnabled(enabled);
    forfeitButton.setEnabled(enabled);
  }

  public void createPawns(List<PawnClient> pawns, PawnAndCardSelection pawnAndCardSelection) {
    double desiredWidth = 40;
    double desiredHeight = 40;
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
        pawnElement = createPawn(pawn, pawnAndCardSelection);
        pawnElements.put(pawn.getPawnId(), pawnElement);
        pawnBoard.getElement().appendChild(pawnElement);
      }

      for (TileMapping mapping : Board.getTiles()) {
        if (mapping.getTileId().equals(pawn.getCurrentTileId())) {
          point = mapping.getPosition();
          System.out.println("");
        }
      }
      pawnElement.getStyle().setLeft(point.getX() - desiredWidth / 2, Style.Unit.PX);
      pawnElement.getStyle().setTop(point.getY() - desiredHeight / 2 - 15, Style.Unit.PX);
    }
  }

  public void drawCardsIcons(HashMap<String, Integer> nrCardsPerPlayerUUID, Image spriteImage) {
    GWT.log("drawCardsIcons");
    for (Map.Entry<String, Integer> entry : nrCardsPerPlayerUUID.entrySet()) {
      String uuid = entry.getKey();
      if (uuid.equals(Cookie.getPlayerId())) {
        // skip drawing card icons for the current player, their hand is already drawn showing the cards
        // so you don't need to draw the back side to indicate how many cards they currently have
        continue;
      }
      ArrayList<Point> cardpoints = Board.getCardsDeckPointsForPlayer(uuid);
      Point startPoint = cardpoints.get(0);
      Point endPoint = cardpoints.get(1);
      double dx = (endPoint.getX() - startPoint.getX()) / 4;
      double dy = (endPoint.getY() - startPoint.getY()) / 4;
      GWT.log("drawCardsIcons \n" +
          "PlayerUUID " + uuid + "\n" +
          "Start " + startPoint + "\n" +
          "End " + endPoint + "\n" +
          "dx " + dx + "\n" +
          "dy " + dy
      );

      Integer nrCards = entry.getValue();
      for (int i = 0; i < nrCards; i++) {
        // Define the source rectangle (from the sprite sheet): this image belongs to the backside image
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
        getCanvasCardsContext().drawImage(
            ImageElement.as(spriteImage.getElement()),
            sourceX, sourceY, spriteWidth, spriteHeight,
            destX, destY, destWidth, destHeight
        );
      }
    }
  }

  private void drawPlayerCardsInHand(List<CardClient> cards,
      PawnAndCardSelection pawnAndCardSelection,
      Image spriteImage) {
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
      cardElement.setClassName("cardDiv");
      cardElement.setId(card.toString());
      cardElement.getStyle()
          .setProperty("backgroundPosition", -sourceX * factor + "px " + -sourceY * factor + "px");

      // Scale entire sprite sheet
      cardElement.getStyle()
          .setProperty("backgroundSize", (1920 * factor) + "px " + (1150 * factor) + "px");

      // Set visible card size
      cardElement.getStyle().setProperty("width", destWidth + "px");
      cardElement.getStyle().setProperty("height", destHeight + "px");

      Event.sinkEvents(cardElement, Event.ONCLICK);
      Event.setEventListener(cardElement, new com.google.gwt.user.client.EventListener() {
        @Override
        public void onBrowserEvent(Event event) {
          if (DOM.eventGetType(event) == Event.ONCLICK) {
            pawnAndCardSelection.setCard(card);
            GWT.log("pawnAndCardSelection = " + pawnAndCardSelection);

            MoveRequestJsonBuilder builder = new MoveRequestJsonBuilder()
                .withPlayerId(Cookie.getPlayerId())
                .withCardId(pawnAndCardSelection.getCard())
                .withMoveType("move")
                .withPawn1(pawnAndCardSelection.getPawn1())
                .withPawn2(pawnAndCardSelection.getPawn2())
                .withStepsPawn1(pawnAndCardSelection.getNrStepsPawn1())
                .withStepsPawn2(pawnAndCardSelection.getNrStepsPawn2())
                .withTempMessageType("CHECK_MOVE");

            GWT.log("testmove anton: " + builder.build());
            ApiClient apiClient = new ApiClient();
            GWT.log("pawn 1: " + pawnAndCardSelection.getPawn1());
            apiClient.checkMove(Cookie.getSessionID(), Cookie.getPlayerId(), builder.build(),
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
                    GWT.log("anton httperror view"+statusCode+":"+statusText);
                    StepsAnimation.resetStepsAnimation();
                  }

                  @Override
                  public void onFailure(Throwable caught) {
                    GWT.log("anton failure view"+caught.getMessage());
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
  }

  public void drawCards(CardsDeck cardsDeck, PawnAndCardSelection pawnAndCardSelection) {
    List<CardClient> cards = cardsDeck.getCards();
    HashMap<String, Integer> nrCardsPerPlayerUUID = cardsDeck.getNrCardsPerPlayer();
    ArrayList<CardClient> playedCards = (ArrayList<CardClient>) cardsDeck.getPlayedCards();

    // Create an image to represent the card deck
    Image img = new Image("/card-deck.png");
    GWT.log("drawcards method");
    // Add a LoadHandler to ensure the image is fully loaded before drawing
    img.addLoadHandler(new LoadHandler() {
      @Override
      public void onLoad(LoadEvent event) {
        // Clear the canvas to prepare for drawing new cards
        getCanvasCardsContext().clearRect(0, 0, getCanvasCards().getWidth(),
            getCanvasCards().getHeight());

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
        getCanvasCardsContext().drawImage(
            ImageElement.as(spriteImage.getElement()),
            sourceX, sourceY, spriteWidth, spriteHeight,
            destX, destY, destWidth, destHeight
        );
      }
    }
  }

  public void removeCardDeckImage() {
    // todo: can this be done any other way? how was it done for the pawns? There it didn't need a loadHandler
    // Get the parent element (e.g., RootPanel)
    Element parentElement = RootPanel.get().getElement();

    // Iterate through child elements to find the matching <img>
    NodeList<Element> imgElements = parentElement.getElementsByTagName("img");
    for (int i = 0; i < imgElements.getLength(); i++) {
      Element imgElement = imgElements.getItem(i);

      // Check if the <img> has the desired `src` attribute
      if ("/card-deck.png".equals(imgElement.getAttribute("src"))) {
        imgElement.removeFromParent(); // Remove the element from the DOM
        GWT.log("Image removed successfully.");
        return;
      }
    }

    GWT.log("Image not found in DOM.");
  }

  public void clearCanvasCards() {
    getCanvasCardsContext().clearRect(0, 0, getCanvasCards().getWidth(),
        getCanvasCards().getHeight());
  }
}