package ADG;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Label;
import ADG.services.PollingService;

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
	private final GameStateServiceAsync gameStateService = GWT.create(GameStateService.class);
	private final CardsServiceAsync cardsService = GWT.create(CardsService.class);
	private final PollingService pollingService = GWT.create(PollingService.class);
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
		GameBoardPresenter gameBoardPresenter = new GameBoardPresenter(new GameBoardModel(), gameBoardView, gameStateService, cardsService, pollingService);
		gameBoardPresenter.start();
	}
}
