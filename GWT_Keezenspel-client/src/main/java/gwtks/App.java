package gwtks;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

	private final MovingServiceAsync movingService = GWT.create(MovingService.class);
	private Canvas canvas;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button sendButton = new Button("Next Player");
		final TextBox nameField = new TextBox();
		nameField.setText("1");
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
		Board board = new Board();
		board.createBoard(4,600);
		canvas = board.drawBoard(canvas, 4, 600);
		mainPanel.add(canvas);
		// Add the main panel to the RootPanel
		RootPanel.get("boardContainer").add(mainPanel);

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);

		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendNameToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a response.
			 */
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = nameField.getText();

				MoveMessage moveMessage = new MoveMessage();
				moveMessage.setUserId(Integer.parseInt(nameField.getText()));

				movingService.makeMove(moveMessage, new AsyncCallback<MoveResponse>() {
					public void onFailure(Throwable caught) {}
					public void onSuccess(MoveResponse result) {
						nameField.setText(String.valueOf(result.getNextUserId()));

					}
				} );

				greetingService.greetServer(textToServer,
						new AsyncCallback<GreetingResponse>() {
							public void onFailure(Throwable caught) {}
							public void onSuccess(GreetingResponse result) {}
						});
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
	}





//	private void drawBoard() {
//		canvas.setHeight("600px");
//		canvas.setWidth("600px");
//		Context2d context = canvas.getContext2d();
//
//		//context.clearRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
//		context.clearRect(0, 0, 600, 600);
//		int boardSize = 600;
//		int cellSize = 40;
//
//		drawCircle(context, 0, 0, 10);
//		drawCircle(context, 600, 0, 10);
//		for (int row = 0; row < boardSize; row++) {
//			for (int col = 0; col < boardSize; col++) {
//				drawCircle(context, col * cellSize + cellSize / 2, row * cellSize + cellSize / 2, cellSize / 2 - 2);
//			}
//		}
//	}
//
	private void drawCircle(Context2d context, double x, double y, double radius) {
		context.beginPath();
		context.arc(x, y, radius, 0, 2 * Math.PI);
		context.setFillStyle("#D3D3D3");
		context.fill();
		context.setStrokeStyle("#000000");
		context.stroke();
		context.closePath();
	}
}
