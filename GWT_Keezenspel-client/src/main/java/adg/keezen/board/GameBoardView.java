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

  @UiField HTMLPanel cardBackBoard;

  @UiField FlowPanel cardsContainer;

  @UiField Label cardHintLabel;

  @UiField HorizontalPanel pawnIntegerBoxes;

  @UiField Label pawn1Label;

  @UiField Label pawn2Label;

  @UiField TextBox stepsPawn1;

  @UiField TextBox stepsPawn2;

  @UiField HorizontalPanel chatInputRow;

  @UiField VerticalPanel chatContainer;

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
    stepsPawn1.setValue("0");
    stepsPawn2.setValue("7");
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

  /**
   * Set by GameBoardPresenter before drawCards() when it detects that one or
   * more opponents just played a card.  Comma-separated player IDs.
   */
  private String playersWhoJustPlayed;

  /** Incremented on each drawCards() call; LoadHandlers that don't match are stale and skip. */
  private int drawGeneration = 0;

  /** Value/suit of the most recently played card (for pile flip animation). */
  private int pendingPlayedCardValue = 0;
  private int pendingPlayedCardSuit  = 0;
  /** Total pile size (including the card being animated) for landing-position math. */
  private int pendingPlayedPileSize  = 0;
  /** True while the own-card fly animation is in flight; suppresses the static pile card. */
  private boolean ownCardAnimationInFlight = false;

  /** Snapshot of the card's start position (board coordinates) captured at click time. */
  private double snapshotStartX = 0;
  private double snapshotStartY = 0;

  public void setPlayersWhoJustPlayed(String commaIds) {
    this.playersWhoJustPlayed = commaIds;
  }

  /**
   * Animate a face-up card from the current player flying toward the pile.
   * The clone stays at the pile position as a DOM pile card until the next
   * drawCards() call cleans it up.
   */
  public void animateOwnPlayedCard(com.google.gwt.dom.client.Element cardEl,
                                   int pileSize, int cardValue, int cardSuit) {
    if (cardEl == null) return;
    ownCardAnimationInFlight = true;
    new com.google.gwt.user.client.Timer() {
      @Override public void run() { ownCardAnimationInFlight = false; }
    }.schedule(800);
    doAnimateOwnCard(cardEl, canvasWrapper.getElement(), cardBackBoard.getElement(),
        pileSize, cardValue, cardSuit);
  }

  private static native void doAnimateOwnCard(
      com.google.gwt.dom.client.Element cardEl,
      com.google.gwt.dom.client.Element wrapper,
      com.google.gwt.dom.client.Element board,
      int pileSize, int cardValue, int cardSuit) /*-{
    var cr = cardEl.getBoundingClientRect();
    var wr = wrapper.getBoundingClientRect();
    if (wr.width === 0) return;
    var scale  = wr.width / 650;
    var startX = (cr.left - wr.left) / scale + (cr.width  / scale) / 2;
    var startY = (cr.top  - wr.top)  / scale + (cr.height / scale) / 2;

    // Card starts at hand size (≈100px wide); pile cards are 60px wide.
    var handW  = 100;
    var spriteW = 1920 / 13;
    var spriteH = 1150 / 5;
    var handH   = handW / spriteW * spriteH;
    var factor  = handW / spriteW;
    var srcX    = spriteW * (cardValue - 1);
    var srcY    = spriteH * cardSuit;
    var bgPos   = (-srcX * factor) + 'px ' + (-srcY * factor) + 'px';
    var bgSize  = (1920 * factor) + 'px ' + (1150 * factor) + 'px';

    // Landing position — same formula as drawPlayedCardsDom.
    var angleDeg  = 45 + pileSize * 45;
    var angleRad  = angleDeg * Math.PI / 180;
    var destCX    = 315 + 10 * Math.cos(angleRad);
    var destCY    = 300 + 10 * Math.sin(angleRad);
    var pileScale = 60 / handW;   // 0.6

    var clone = $doc.createElement('div');
    clone.style.position           = 'absolute';
    clone.style.width              = handW + 'px';
    clone.style.height             = handH + 'px';
    clone.style.left               = startX + 'px';
    clone.style.top                = startY + 'px';
    clone.style.backgroundImage    = "url('card-deck.png')";
    clone.style.backgroundPosition = bgPos;
    clone.style.backgroundSize     = bgSize;
    clone.style.backgroundRepeat   = 'no-repeat';
    clone.style.borderRadius       = '5px';
    clone.style.boxShadow          = '2px 4px 12px rgba(0,0,0,0.6)';
    clone.style.transform          = 'translate(-50%,-50%) scale(1)';
    clone.style.zIndex             = '200';
    clone.style.transition         = 'none';
    board.appendChild(clone);

    requestAnimationFrame(function() {
      clone.style.transition = 'transform 0.18s ease-out, box-shadow 0.18s';
      clone.style.transform  = 'translate(-50%,-50%) scale(1.2)';
      clone.style.boxShadow  = '0 0 18px rgba(255,255,255,0.5)';
    });

    setTimeout(function() {
      clone.style.transition = 'all 0.5s cubic-bezier(0.25,0.46,0.45,0.94)';
      clone.style.left       = destCX + 'px';
      clone.style.top        = destCY + 'px';
      clone.style.transform  = 'translate(-50%,-50%) scale(' + pileScale + ')';
      clone.style.boxShadow  = '1px 2px 5px rgba(0,0,0,0.5)';
    }, 200);

    // Settle as a permanent pile card (cleaned up on next drawCards call).
    setTimeout(function() {
      clone.setAttribute('data-pile', 'true');
      clone.style.zIndex    = '10';
      clone.style.transition = 'none';
    }, 750);
  }-*/;

  /**
   * Captures the card's start position in board coordinates and stores it for later use by
   * {@link #animateOwnPlayedCardFromSnapshot}. Must be called while the card element is in the DOM.
   */
  public void captureCardStartPos(com.google.gwt.dom.client.Element cardEl) {
    if (cardEl == null) return;
    com.google.gwt.dom.client.Element wrapper = canvasWrapper.getElement();
    int wrapperWidth = wrapper.getClientWidth();
    if (wrapperWidth == 0) return;
    double scale = wrapperWidth / 650.0;
    double left = cardEl.getAbsoluteLeft() - wrapper.getAbsoluteLeft();
    double top  = cardEl.getAbsoluteTop()  - wrapper.getAbsoluteTop();
    snapshotStartX = left / scale + (cardEl.getOffsetWidth()  / scale) / 2;
    snapshotStartY = top  / scale + (cardEl.getOffsetHeight() / scale) / 2;
  }

  /**
   * Animates a card flying to the pile using the position captured by {@link #captureCardStartPos}.
   * Call this from {@code onSuccess} so animation only plays when the move is accepted.
   */
  public void animateOwnPlayedCardFromSnapshot(int pileSize, int cardValue, int cardSuit) {
    ownCardAnimationInFlight = true;
    new com.google.gwt.user.client.Timer() {
      @Override public void run() { ownCardAnimationInFlight = false; }
    }.schedule(800);
    doAnimateOwnCardFromPos(snapshotStartX, snapshotStartY, cardBackBoard.getElement(),
        pileSize, cardValue, cardSuit);
  }

  private static native void doAnimateOwnCardFromPos(
      double startX, double startY,
      com.google.gwt.dom.client.Element board,
      int pileSize, int cardValue, int cardSuit) /*-{
    var handW   = 100;
    var spriteW = 1920 / 13;
    var spriteH = 1150 / 5;
    var handH   = handW / spriteW * spriteH;
    var factor  = handW / spriteW;
    var srcX    = spriteW * (cardValue - 1);
    var srcY    = spriteH * cardSuit;
    var bgPos   = (-srcX * factor) + 'px ' + (-srcY * factor) + 'px';
    var bgSize  = (1920 * factor) + 'px ' + (1150 * factor) + 'px';

    var angleDeg  = 45 + pileSize * 45;
    var angleRad  = angleDeg * Math.PI / 180;
    var destCX    = 315 + 10 * Math.cos(angleRad);
    var destCY    = 300 + 10 * Math.sin(angleRad);
    var pileScale = 60 / handW;

    var clone = $doc.createElement('div');
    clone.style.position           = 'absolute';
    clone.style.width              = handW + 'px';
    clone.style.height             = handH + 'px';
    clone.style.left               = startX + 'px';
    clone.style.top                = startY + 'px';
    clone.style.backgroundImage    = "url('card-deck.png')";
    clone.style.backgroundPosition = bgPos;
    clone.style.backgroundSize     = bgSize;
    clone.style.backgroundRepeat   = 'no-repeat';
    clone.style.borderRadius       = '5px';
    clone.style.boxShadow          = '2px 4px 12px rgba(0,0,0,0.6)';
    clone.style.transform          = 'translate(-50%,-50%) scale(1)';
    clone.style.zIndex             = '200';
    clone.style.transition         = 'none';
    board.appendChild(clone);

    requestAnimationFrame(function() {
      clone.style.transition = 'transform 0.18s ease-out, box-shadow 0.18s';
      clone.style.transform  = 'translate(-50%,-50%) scale(1.2)';
      clone.style.boxShadow  = '0 0 18px rgba(255,255,255,0.5)';
    });

    setTimeout(function() {
      clone.style.transition = 'all 0.5s cubic-bezier(0.25,0.46,0.45,0.94)';
      clone.style.left       = destCX + 'px';
      clone.style.top        = destCY + 'px';
      clone.style.transform  = 'translate(-50%,-50%) scale(' + pileScale + ')';
      clone.style.boxShadow  = '1px 2px 5px rgba(0,0,0,0.5)';
    }, 200);

    setTimeout(function() {
      clone.setAttribute('data-pile', 'true');
      clone.style.zIndex    = '10';
      clone.style.transition = 'none';
    }, 750);
  }-*/;

  /** Returns a JSON snapshot of {playerId -> [{cx,cy,t}]} from current card elements. */
  private static native String snapshotCardPositions(
      com.google.gwt.dom.client.Element board) /*-{
    var map = {};
    var nodes = board.childNodes;
    for (var i = 0; i < nodes.length; i++) {
      var el = nodes[i];
      var pid = el.getAttribute && el.getAttribute('data-player-id');
      if (!pid) continue;
      if (!map[pid]) map[pid] = [];
      map[pid].push({ cx: el.style.left, cy: el.style.top, t: el.style.transform });
    }
    return JSON.stringify(map);
  }-*/;

  /**
   * Removes fan-card elements (data-player-id) and trims pile-card elements
   * (data-pile) to a maximum of 8, oldest first.
   */
  private static native void cleanFanAndPileCards(
      com.google.gwt.dom.client.Element board) /*-{
    var toRemove = [];
    var pileCards = [];
    var ch = board.childNodes;
    for (var i = 0; i < ch.length; i++) {
      var el = ch[i];
      if (!el.getAttribute) continue;
      if (el.getAttribute('data-player-id')) {
        toRemove.push(el);
      } else if (el.getAttribute('data-pile')) {
        pileCards.push(el);
      }
    }
    for (var i = 0; i < toRemove.length; i++) board.removeChild(toRemove[i]);
    if (pileCards.length > 8) {
      var excess = pileCards.length - 8;
      for (var i = 0; i < excess; i++) board.removeChild(pileCards[i]);
    }
  }-*/;

  /**
   * FLIP-animates opponent cards after a card was played.
   * The clone flips from back to face mid-flight, then lands at the pile
   * position and stays as a DOM pile card until the next drawCards() call.
   */
  private static native void applyCardAnimations(
      com.google.gwt.dom.client.Element board,
      String oldSnapshotJson,
      String playedCommaIds,
      int lastCardValue,
      int lastCardSuit,
      int pileSize) /*-{
    if (!oldSnapshotJson || !playedCommaIds) return;
    var snap;
    try { snap = JSON.parse(oldSnapshotJson); } catch (e) { return; }

    // Landing position for the new pile card (same formula as drawPlayedCards).
    var angleDeg = 45 + pileSize * 45;
    var angleRad = angleDeg * Math.PI / 180;
    var destCX   = 315 + 10 * Math.cos(angleRad);
    var destCY   = 300 + 10 * Math.sin(angleRad);

    // Card-face sprite data for the revealed side.
    var spriteW  = 1920 / 13;
    var spriteH  = 1150 / 5;
    var cloneW   = 60;
    var cloneH   = cloneW / spriteW * spriteH;
    var factor   = cloneW / spriteW;
    var srcX     = spriteW * (lastCardValue - 1);
    var srcY     = spriteH * lastCardSuit;
    var faceBgPos  = (-srcX * factor) + 'px ' + (-srcY * factor) + 'px';
    var faceBgSize = (1920 * factor) + 'px ' + (1150 * factor) + 'px';

    var pids = playedCommaIds.split(',');
    for (var pi = 0; pi < pids.length; pi++) {
      var pid = pids[pi];
      var oldPos = snap[pid];
      if (!oldPos || oldPos.length === 0) continue;

      var newCards = [];
      var ch = board.childNodes;
      for (var ci = 0; ci < ch.length; ci++) {
        if (ch[ci].getAttribute && ch[ci].getAttribute('data-player-id') === pid) {
          newCards.push(ch[ci]);
        }
      }
      var oldN = oldPos.length;
      var newN = newCards.length;
      if (newN !== oldN - 1) continue;
      var positionsValid = true;
      for (var vi = 0; vi < oldN; vi++) {
        if (!oldPos[vi].cx || !oldPos[vi].cy) { positionsValid = false; break; }
      }
      if (!positionsValid) continue;

      var playedI = Math.floor((oldN - 1) / 2);
      var playedP = oldPos[playedI];

      // Midpoint between fan position and pile (where the flip happens).
      var midCX = (parseFloat(playedP.cx) + destCX) / 2;
      var midCY = (parseFloat(playedP.cy) + destCY) / 2;

      // Clone starts as a face-down back card.
      var clone = $doc.createElement('div');
      clone.className = 'cardBackIcon animating-clone';
      clone.innerHTML = '&#9733;';
      clone.style.position   = 'absolute';
      clone.style.left       = playedP.cx;
      clone.style.top        = playedP.cy;
      clone.style.transform  = playedP.t;
      clone.style.zIndex     = '200';
      clone.style.transition = 'none';
      board.appendChild(clone);

      // FLIP setup: save new positions, teleport fan cards back to old positions.
      var remOld = [];
      for (var j = 0; j < oldN; j++) {
        if (j !== playedI) remOld.push(oldPos[j]);
      }
      var newPos = [];
      for (var k = 0; k < newN; k++) {
        newPos.push({ cx: newCards[k].style.left, cy: newCards[k].style.top, t: newCards[k].style.transform });
        newCards[k].style.transition = 'none';
        newCards[k].style.left      = remOld[k].cx;
        newCards[k].style.top       = remOld[k].cy;
        newCards[k].style.transform = remOld[k].t;
      }

      (function(clone, newCards, newPos, playedP, midCX, midCY, destCX, destCY,
                faceBgPos, faceBgSize, cloneW, cloneH) {
        requestAnimationFrame(function() {
          // Release fan-card FLIP transition.
          requestAnimationFrame(function() {
            for (var k = 0; k < newCards.length; k++) {
              newCards[k].style.transition = 'left 0.35s ease, top 0.35s ease, transform 0.35s ease';
              newCards[k].style.left       = newPos[k].cx;
              newCards[k].style.top        = newPos[k].cy;
              newCards[k].style.transform  = newPos[k].t;
            }
          });

          // Briefly enlarge the clone.
          clone.style.transition = 'transform 0.15s ease-out';
          clone.style.transform  = playedP.t + ' scale(1.3)';

          // Phase 1: fly to midpoint, collapsing scaleX to 0 (first half of flip).
          setTimeout(function() {
            clone.style.transition = 'left 0.25s ease-in, top 0.25s ease-in, transform 0.2s ease-in';
            clone.style.left      = midCX + 'px';
            clone.style.top       = midCY + 'px';
            clone.style.transform = 'translate(-50%,-50%) scaleX(0)';
          }, 180);

          // Phase 2: swap to card face, expand scaleX, continue to pile.
          setTimeout(function() {
            clone.className              = 'animating-clone';
            clone.innerHTML              = '';
            clone.style.width            = cloneW + 'px';
            clone.style.height           = cloneH + 'px';
            clone.style.background       = 'none';
            clone.style.border           = 'none';
            clone.style.backgroundImage  = "url('card-deck.png')";
            clone.style.backgroundPosition = faceBgPos;
            clone.style.backgroundSize   = faceBgSize;
            clone.style.backgroundRepeat = 'no-repeat';
            clone.style.borderRadius     = '5px';
            clone.style.boxShadow        = '2px 4px 10px rgba(0,0,0,0.6)';
            clone.style.transition = 'left 0.32s ease-out, top 0.32s ease-out, transform 0.25s ease-out';
            clone.style.left      = destCX + 'px';
            clone.style.top       = destCY + 'px';
            clone.style.transform = 'translate(-50%,-50%) scale(1)';
          }, 430);

          // Settle as a permanent pile card.
          setTimeout(function() {
            clone.setAttribute('data-pile', 'true');
            clone.style.zIndex    = '10';
            clone.style.transition = 'none';
          }, 820);
        });
      })(clone, newCards, newPos, playedP, midCX, midCY, destCX, destCY,
         faceBgPos, faceBgSize, cloneW, cloneH);
    }
  }-*/;

  /** Renders face-down card icons as HTML elements on the cardBackBoard overlay. */
  public void drawCardsIcons(HashMap<String, Integer> nrCardsPerPlayerUUID) {
    // Snapshot old positions BEFORE clearing so the FLIP animation knows where
    // each card started.  Also captures which player IDs are present.
    String oldSnapshot = snapshotCardPositions(cardBackBoard.getElement());
    String animatePlayers = this.playersWhoJustPlayed;
    this.playersWhoJustPlayed = null;

    // Remove fan cards and trim pile cards to max 8; leave flying clones alone.
    cleanFanAndPileCards(cardBackBoard.getElement());

    for (Map.Entry<String, Integer> entry : nrCardsPerPlayerUUID.entrySet()) {
      String uuid = entry.getKey();
      if (uuid.equals(Cookie.getPlayerId())) continue;

      ArrayList<Point> cardpoints = Board.getCardsDeckPointsForPlayer(uuid);
      Point startPoint = cardpoints.get(0);
      Point endPoint   = cardpoints.get(1);
      Integer nrCards  = entry.getValue();
      if (nrCards <= 0) continue;

      /* ── Fan geometry ──────────────────────────────────────────────── */
      double rawMidX = (startPoint.getX() + endPoint.getX()) / 2;
      double rawMidY = (startPoint.getY() + endPoint.getY()) / 2;
      double radX   = rawMidX - 300;
      double radY   = rawMidY - 300;
      double radLen = Math.sqrt(radX * radX + radY * radY);
      if (radLen < 1) continue;
      double radNx = radX / radLen;
      double radNy = radY / radLen;

      /* Shift the fan midpoint outward so cards are clear of the tiles. */
      double extraPush = 40;
      double midX = rawMidX + radNx * extraPush;
      double midY = rawMidY + radNy * extraPush;

      /* Pivot further from the board so cards sit clear of the tiles.  */
      double pivotDist = 120;
      double pivotX    = midX + radNx * pivotDist;
      double pivotY    = midY + radNy * pivotDist;

      /* Full spread for max 5 cards (4 gaps).  Scale down as cards are played
         so the fan shrinks proportionally and no gaps appear.              */
      double fullSpreadW   = Math.sqrt(
          Math.pow(endPoint.getX() - startPoint.getX(), 2) +
          Math.pow(endPoint.getY() - startPoint.getY(), 2));
      double scaledSpreadW = fullSpreadW * Math.max(0, nrCards - 1) / 4.0;
      double halfFan   = Math.atan2(scaledSpreadW / 2, pivotDist);
      double fanRadius = Math.sqrt(pivotDist * pivotDist + (scaledSpreadW / 2) * (scaledSpreadW / 2));
      double baseDir   = Math.atan2(300 - pivotY, 300 - pivotX);

      for (int i = 0; i < nrCards; i++) {
        double t         = (nrCards <= 1) ? 0.0 : (double) i / (nrCards - 1);
        double cardAngle = baseDir - halfFan + t * 2 * halfFan;
        double cx        = pivotX + fanRadius * Math.cos(cardAngle);
        double cy        = pivotY + fanRadius * Math.sin(cardAngle);
        double rotRad    = cardAngle + Math.PI / 2;

        DivElement card = Document.get().createDivElement();
        card.setClassName("cardBackIcon");
        card.setInnerHTML("&#9733;");
        card.getStyle().setLeft(cx, Style.Unit.PX);
        card.getStyle().setTop(cy, Style.Unit.PX);
        card.getStyle().setProperty("transform",
            "translate(-50%, -50%) rotate(" + rotRad + "rad)");
        card.setAttribute("data-player-id", uuid);
        card.setAttribute("data-card-index", String.valueOf(i));
        cardBackBoard.getElement().appendChild(card);
      }
    }
    if (animatePlayers != null && !animatePlayers.isEmpty()) {
      applyCardAnimations(cardBackBoard.getElement(), oldSnapshot, animatePlayers,
          pendingPlayedCardValue, pendingPlayedCardSuit, pendingPlayedPileSize);
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

                // Keep step boxes in sync with the model: setCard() may have reset the
                // split steps (e.g. re-selecting a 7 card resets to 0/7 internally).
                stepsPawn1.setValue(String.valueOf(pawnAndCardSelection.getNrStepsPawn1()));
                stepsPawn2.setValue(String.valueOf(pawnAndCardSelection.getNrStepsPawn2()));

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

  /**
   * Repaints only the player's hand so the selected-card border reflects the current
   * selection. Used after a pawn click that auto-selects a card, where no server push
   * (and therefore no drawCards()) occurs. Lighter than drawCards(): it skips the
   * deck-image reload, pile redraw and animations.
   */
  public void refreshHandCardSelection(CardsDeck cardsDeck, PawnAndCardSelection pawnAndCardSelection) {
    drawPlayerCardsInHand(cardsDeck.getCards(), pawnAndCardSelection, null);
  }

  public void drawCards(CardsDeck cardsDeck, PawnAndCardSelection pawnAndCardSelection) {
    List<CardClient> cards = cardsDeck.getCards();
    HashMap<String, Integer> nrCardsPerPlayerUUID = cardsDeck.getNrCardsPerPlayer();
    ArrayList<CardClient> playedCards = (ArrayList<CardClient>) cardsDeck.getPlayedCards();

    // Capture pile state for the flip animation (used inside drawCardsIcons).
    this.pendingPlayedPileSize = playedCards.size();
    if (!playedCards.isEmpty()) {
      CardClient last = playedCards.get(playedCards.size() - 1);
      this.pendingPlayedCardValue = last.getValue();
      this.pendingPlayedCardSuit  = last.getSuit();
    }

    final int myGeneration = ++drawGeneration;
    // Capture now (before loadHandler fires) whether any card animation is in flight,
    // so drawPlayedCardsDom can skip the newest pile card while the clone is still flying.
    final boolean skipLastPileCard =
        ownCardAnimationInFlight
        || (this.playersWhoJustPlayed != null && !this.playersWhoJustPlayed.isEmpty());

    // Create an image to represent the card deck
    Image img = new Image(GWT.getHostPageBaseURL() + "card-deck.png");
    img.getElement().setId("card-deck-dynamic");
    GWT.log("drawcards method");
    // Add a LoadHandler to ensure the image is fully loaded before drawing
    img.addLoadHandler(
        new LoadHandler() {
          @Override
          public void onLoad(LoadEvent event) {
            // Drop stale handlers: a newer drawCards() call has already superseded this one.
            if (myGeneration != drawGeneration) return;

            GWT.log("\n\ndrawing cards");
            drawPlayerCardsInHand(cards, pawnAndCardSelection, img);
            drawCardsIcons(nrCardsPerPlayerUUID);
            drawPlayedCardsDom(playedCards, skipLastPileCard);
          }
        });

    // Trigger the image loading by adding it to the DOM
    removeCardDeckImage();
    img.setVisible(false);
    RootPanel.get().add(img);
  }

  private static native void removeStaticPileCards(
      com.google.gwt.dom.client.Element board) /*-{
    var ch = board.childNodes;
    var toRemove = [];
    for (var i = 0; i < ch.length; i++) {
      if (ch[i].getAttribute && ch[i].getAttribute('data-pile-static')) {
        toRemove.push(ch[i]);
      }
    }
    for (var i = 0; i < toRemove.length; i++) board.removeChild(toRemove[i]);
  }-*/;

  private void drawPlayedCardsDom(ArrayList<CardClient> playedCards, boolean skipLast) {
    // Keep count accessible for Selenium tests.
    getCanvasCards().setAttribute("data-played-count", String.valueOf(playedCards.size()));

    removeStaticPileCards(cardBackBoard.getElement());

    double spriteWidth  = 1920 / 13.0;
    double spriteHeight = 1150 / 5.0;
    double destWidth    = 60.0;
    double destHeight   = destWidth / spriteWidth * spriteHeight;
    double factor       = destWidth / spriteWidth;

    int angleDegrees = 45;
    // When an animation is in flight the newest card is shown by the animated clone —
    // draw only the older cards so the static element doesn't pop in mid-animation.
    int drawUpTo = skipLast ? playedCards.size() - 1 : playedCards.size();
    int startFrom = drawUpTo > 8 ? drawUpTo - 8 : 0;

    for (int i = 0; i < drawUpTo; i++) {
      angleDegrees += 45;
      if (i >= startFrom) {
        CardClient card = playedCards.get(i);
        double angleRad = Math.toRadians(angleDegrees);
        double destX    = 300 - destWidth / 4  + 10 * Math.cos(angleRad);
        double destY    = 300 - destHeight / 2 + 10 * Math.sin(angleRad);
        double srcX     = spriteWidth  * (card.getValue() - 1);
        double srcY     = spriteHeight * card.getSuit();

        DivElement el = Document.get().createDivElement();
        el.setAttribute("data-pile-static", "true");
        el.getStyle().setProperty("position",           "absolute");
        el.getStyle().setProperty("width",              destWidth + "px");
        el.getStyle().setProperty("height",             destHeight + "px");
        el.getStyle().setLeft(destX, Style.Unit.PX);
        el.getStyle().setTop(destY,  Style.Unit.PX);
        el.getStyle().setProperty("backgroundImage",    "url('card-deck.png')");
        el.getStyle().setProperty("backgroundPosition",
            (-srcX * factor) + "px " + (-srcY * factor) + "px");
        el.getStyle().setProperty("backgroundSize",
            (1920 * factor) + "px " + (1150 * factor) + "px");
        el.getStyle().setProperty("backgroundRepeat",   "no-repeat");
        el.getStyle().setProperty("borderRadius",       "4px");
        el.getStyle().setProperty("boxShadow",          "1px 2px 5px rgba(0,0,0,0.5)");
        el.getStyle().setProperty("zIndex",             "2");
        cardBackBoard.getElement().appendChild(el);
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

  public void setChatVisible(boolean visible) {
    chatContainer.setVisible(visible);
  }

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

}
