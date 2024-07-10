package gwtks;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

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
	private Canvas canvas;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button sendButton = new Button("Make Move");
		final Button swapButton = new Button("Swap");
		final Button pawnOnTheBoardButton = new Button("King/Ace");

		final TextBox playerIdField = new TextBox();
		final TextBox pawnIdField = new TextBox();
		final TextBox playerId2Field = new TextBox();
		final TextBox pawnId2Field = new TextBox();
		final TextBox stepsField = new TextBox();
		final Label errorLabel = new Label();

		// Create a Canvas for the Parcheesi board
		canvas = Canvas.createIfSupported();
		if (canvas == null) {
			RootPanel.get().add(new Label("Canvas is not supported in your browser."));
			return;
		}

		int canvasSize = 600;
		canvas.setWidth(canvasSize + "px");
		canvas.setHeight(canvasSize + "px");
		canvas.setCoordinateSpaceWidth(canvasSize);
		canvas.setCoordinateSpaceHeight(canvasSize);

		VerticalPanel mainPanel = new VerticalPanel();

		// Initialize the board (this is a very simplified version)
		// Add board and other components to the main panel
		int nrPlayers = 8;

		Board board = new Board();
		board.createBoard(nrPlayers,600);
		canvas = board.drawBoard(canvas);
		mainPanel.add(canvas);

		// Add the main panel to the RootPanel
		RootPanel.get("boardContainer").add(mainPanel);

		board.drawPawns(canvas);;

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");
		pawnOnTheBoardButton.addStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("playerIdFieldContainer").add(playerIdField);
		RootPanel.get("pawnIdFieldContainer").add(pawnIdField);
		RootPanel.get("stepsFieldContainer").add(stepsField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("pawnOnTheBoardButtonContainer").add(pawnOnTheBoardButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);

		RootPanel.get("playerId2FieldContainer").add(playerId2Field);
		RootPanel.get("pawnId2FieldContainer").add(pawnId2Field);
		RootPanel.get("swapButtonContainer").add(swapButton);

		// Focus the cursor on the name field when the app loads
//		nameField.setFocus(true);
//		nameField.selectAll();

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendMoveToServer();
			}

			/**
			 * Send the MoveMessage to the server and wait for a response.
			 */
			private void sendMoveToServer() {
				// First, we validate the input.
				errorLabel.setText("");

				MoveMessage moveMessage = new MoveMessage();
				int playerId = Integer.parseInt(playerIdField.getText());
				int pawnNr = Integer.parseInt(pawnIdField.getText());
				PawnId selectedPawnId = new PawnId(playerId,pawnNr);
				Pawn pawn = Board.getPawn(selectedPawnId);

				moveMessage.setPawnId1(selectedPawnId);
				moveMessage.setMoveType(MoveType.MOVE);
				moveMessage.setTileId(pawn.getCurrentTileId());
//				moveMessage.setTileId(Board.getPawns().get(0).getCurrentTileId());
				moveMessage.setStepsPawn1(Integer.parseInt(stepsField.getText()));

				movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
					public void onFailure(Throwable caught) {}
					public void onSuccess(MoveResponse result) {
						Context2d context = canvas.getContext2d();
						context.clearRect(0, 0, context.getCanvas().getWidth(), context.getCanvas().getHeight());
						Canvas canvas1 = board.drawBoard(canvas);
						MoveController.movePawn(canvas, result);
					}
				} );
			}
		}

		// Create a handler for the sendButton and nameField
		class MyKingHandler implements ClickHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendOnboardMessageToServer();
			}

			/**
			 * Send the MoveMessage to the server and wait for a response.
			 */
			private void sendOnboardMessageToServer() {
				// First, we validate the input.
				errorLabel.setText("");

				MoveMessage moveMessage = new MoveMessage();
				int playerId = Integer.parseInt(playerIdField.getText());
				int pawnNr = Integer.parseInt(pawnIdField.getText());
				PawnId selectedPawnId = new PawnId(playerId, pawnNr);

				moveMessage.setPawnId1(selectedPawnId);
				moveMessage.setMoveType(MoveType.ONBOARD);

				movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
					public void onFailure(Throwable caught) {
					}

					public void onSuccess(MoveResponse result) {
						Context2d context = canvas.getContext2d();
						context.clearRect(0, 0, context.getCanvas().getWidth(), context.getCanvas().getHeight());
						Canvas canvas1 = board.drawBoard(canvas);
						MoveController.movePawn(canvas, result);
					}
				});
			}
		}

		// Create a handler for the sendButton and nameField
		class MySwapHandler implements ClickHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendOnboardMessageToServer();
			}

			/**
			 * Send the MoveMessage to the server and wait for a response.
			 */
			private void sendOnboardMessageToServer() {
				// First, we validate the input.
				errorLabel.setText("");

				MoveMessage moveMessage = new MoveMessage();
				int playerId = Integer.parseInt(playerIdField.getText());
				int pawnNr = Integer.parseInt(pawnIdField.getText());
				PawnId selectedPawnId = new PawnId(playerId, pawnNr);
				int playerId2 = Integer.parseInt(playerId2Field.getText());
				int pawnNr2 = Integer.parseInt(pawnId2Field.getText());
				PawnId selectedPawnId2 = new PawnId(playerId2, pawnNr2);

				moveMessage.setPawnId1(selectedPawnId);
				moveMessage.setPawnId2(selectedPawnId2);
				moveMessage.setMoveType(MoveType.SWITCH);

				movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
					public void onFailure(Throwable caught) {
					}

					public void onSuccess(MoveResponse result) {
						Context2d context = canvas.getContext2d();
						context.clearRect(0, 0, context.getCanvas().getWidth(), context.getCanvas().getHeight());
						Canvas canvas1 = board.drawBoard(canvas);
						MoveController.movePawn(canvas, result);
					}
				});
			}
		}

		// Add a handler to send the MOVE to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);

		// Add a handler to send the name to the server
		MyKingHandler kingHandler = new MyKingHandler();
		pawnOnTheBoardButton.addClickHandler(kingHandler);

		// Add a handler to send the name to the server
		MySwapHandler swapHandler = new MySwapHandler();
		pawnOnTheBoardButton.addClickHandler(swapHandler);
	}
}
