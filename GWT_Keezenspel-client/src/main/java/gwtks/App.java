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
	private final GameStateServiceAsync gameStateService = GWT.create(GameStateService.class);
	private GameAnimation gameAnimation;
    private Context2d ctxPawns;
	private Context2d ctxBoard;
	private GameBoardView gameBoardView;

    /**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		// Create a Canvas for the Parcheesi board
		if(Canvas.createIfSupported()==null){
			RootPanel.get().add(new Label("Canvas is not supported in your browser."));
			return;
		}

		gameBoardView = new GameBoardView();
		RootPanel.get().clear();
		RootPanel.get().add(gameBoardView);
		//todo add model
		GameBoardPresenter gameBoardPresenter = new GameBoardPresenter(new GameBoardModel(), gameBoardView, gameStateService);
		gameBoardPresenter.start();

		final Button sendButton = new Button("Play Card");
		final Button forfeitButton = new Button("Forfeit");

		Document document = Document.get();
		ctxPawns = ((CanvasElement) document.getElementById("canvasPawns")).getContext2d();
		ctxBoard = ((CanvasElement) document.getElementById("canvasBoard")).getContext2d();

		PlayerList playerList = new PlayerList();
		playerList.createListElement();

		//set playerId to first player in list todo: use Cookie
		PawnAndCardSelection.setPlayerId(0);

		// add widgets
		sendButton.addStyleName("sendButton");
		forfeitButton.addStyleName("sendButton");

		// Use RootPanel.get() to get the entire body element
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("forfeitButtonContainer").add(forfeitButton);

		// Add a handler to send the MOVE to the server
		//todo: remove
		SendHandler handler = new SendHandler();
		sendButton.addClickHandler(handler);

		//todo: remove
		ForfeitHandler forfeitHandler = new ForfeitHandler();
		forfeitButton.addClickHandler(forfeitHandler);

		// Start the game
		gameAnimation = new GameAnimation();
		animate();

		Timer timer = new Timer() {
			@Override
			public void run() {
				pollServerForCards();
				pollServerForGameState();
			}
		};
		// Schedule the timer to run every 200ms
		timer.scheduleRepeating(200);
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

	public void pollServerForCards(){
		// todo: this should actually only get the cards for the Cookie.playerId
		cardsService.getCards(PlayerList.getPlayerIdPlaying(), new AsyncCallback<CardResponse>() {
			public void onFailure(Throwable caught) {
				StepsAnimation.reset();
			}
			public void onSuccess(CardResponse result) {

				PawnAndCardSelection.setPlayerId(result.getPlayerId());
				if(CardsDeck.areCardsDifferent(result.getCards())){
					CardsDeck.setCards(result.getCards());
					gameBoardView.drawCards(CardsDeck.getCards());// todo: remove old method
					gameBoardView.drawCards_new(CardsDeck.getCards());// todo: keep new method
					PlayerList.refresh();
				}
			}
		} );
	}
	public void pollServerForGameState(){
		gameStateService.getGameState( new AsyncCallback<GameStateResponse>() {
			public void onFailure(Throwable caught) {
				StepsAnimation.reset();
			}
			public void onSuccess(GameStateResponse result) {
				GWT.log(result.toString());
				// only set the board when empty, e.g.
				// when the browser was refreshed or when you join the game for the first time
				if(!Board.isInitialized()){
					Board board = new Board();
					Board.setPawns(result.getPawns());
					board.createBoard(result.getNrPlayers(),600);
					board.drawBoard(ctxBoard);// todo: remove
					board.drawBoard(gameBoardView.getCanvasBoard().getContext2d());
					board.drawPawns(ctxPawns); // todo: remove
					board.drawPawns(gameBoardView.getCanvasPawns().getContext2d());
					PlayerList.setNrPlayers(result.getNrPlayers());
				}
				PlayerList playerList = new PlayerList();
				PlayerList.setActivePlayers(result.getActivePlayers());
				PlayerList.setWinners(result.getWinners());
				playerList.setPlayerIdPlayingAndDrawPlayerList(result.getPlayerIdTurn());
			}
		} );
	}
}
