package gwtks;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import gwtks.animations.GameAnimation;
import gwtks.animations.StepsAnimation;
import gwtks.handlers.*;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class App implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Moving service.
	 */
	private final MovingServiceAsync movingService = GWT.create(MovingService.class);
	private final CardsServiceAsync cardsService = GWT.create(CardsService.class);
	private GameAnimation gameAnimation;
    private Context2d ctxPawns;
	private Context2d ctxBoard;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button sendButton = new Button("Make Move");
		final Button swapButton = new Button("Swap");
		final Button testMoveButton = new Button("Test Move");
		final Button pawnOnTheBoardButton = new Button("King/Ace");
		final Label errorLabel = new Label();

		// Create a Canvas for the Parcheesi board
		if(Canvas.createIfSupported()==null){
			RootPanel.get().add(new Label("Canvas is not supported in your browser."));
			return;
		}
		Document document = Document.get();
		ctxPawns = ((CanvasElement) document.getElementById("canvasPawns")).getContext2d();
		ctxBoard = ((CanvasElement) document.getElementById("canvasBoard")).getContext2d();

		int nrPlayers = 8;
        Board board = new Board();
		board.createBoard(nrPlayers,600);
		board.drawBoard(ctxBoard); // let this stay here
		board.drawPawns(ctxPawns);

		PlayerList playerList = new PlayerList();
		playerList.createListElement();

		//set playerId
		PawnAndCardSelection.setPlayerId(0);

		// add widgets
		sendButton.addStyleName("sendButton");
		pawnOnTheBoardButton.addStyleName("sendButton");

		// Use RootPanel.get() to get the entire body element
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("pawnOnTheBoardButtonContainer").add(pawnOnTheBoardButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);
		RootPanel.get("swapButtonContainer").add(swapButton);
		RootPanel.get("testMoveButtonContainer").add(testMoveButton);


		// Add a handler to send the MOVE to the server
		SendHandler handler = new SendHandler();
		sendButton.addClickHandler(handler);

		TestMoveHandler testMoveHandler = new TestMoveHandler();
		testMoveButton.addClickHandler(testMoveHandler);

		// Add a handler to send the name to the server
		KingHandler kingHandler = new KingHandler();
		pawnOnTheBoardButton.addClickHandler(kingHandler);

		// Add a handler to send the name to the server
		SwapHandler swapHandler = new SwapHandler();
		swapButton.addClickHandler(swapHandler);



		// Start the game
		gameAnimation = new GameAnimation();
		animate();

		Timer timer = new Timer() {
			@Override
			public void run() {
				pollServer();
			}
		};
		// Schedule the timer to run every 200ms
		timer.scheduleRepeating(200);

		CardsDeck.drawCards();

		CanvasClickHandler.addClickHandler();
	}

	public void animate(){
		ctxPawns.clearRect(0,0, 600, 600);
		gameAnimation.update();
		gameAnimation.draw();
		AnimationScheduler.AnimationCallback animationCallback = new AnimationScheduler.AnimationCallback() {
			@Override
			public void execute(double v) {
				animate();
			}
		};
		AnimationScheduler.get().requestAnimationFrame(animationCallback);
	};

	public void pollServer(){
		cardsService.getCards(0, new AsyncCallback<CardResponse>() {
			public void onFailure(Throwable caught) {
				StepsAnimation.reset();
			}
			public void onSuccess(CardResponse result) {
				GWT.log("cards playerId"+result.getPlayerId());
				GWT.log("Cards received: " + result.getCards());
				GWT.log("Cards per player: " + result.getNrOfCardsPerPlayer());
				if(result != null && result.getCards() != null && !result.getCards().isEmpty()){
					if(CardsDeck.areCardsDifferent(result.getCards())){
						CardsDeck.setCards(result.getCards());
						CardsDeck.drawCards();
					}
				}
			}
		} );
	}


}
