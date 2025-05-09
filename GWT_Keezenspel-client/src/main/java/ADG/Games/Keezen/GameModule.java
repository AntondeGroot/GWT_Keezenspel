package ADG.Games.Keezen;

import ADG.Games.Keezen.Cards.CardsService;
import ADG.Games.Keezen.Cards.CardsServiceAsync;
import ADG.Games.Keezen.State.GameStateService;
import ADG.Games.Keezen.State.GameStateServiceAsync;
import ADG.Games.Keezen.Move.MovingService;
import ADG.Games.Keezen.Move.MovingServiceAsync;
import ADG.Games.Keezen.board.GameBoardPresenter;
import ADG.Games.Keezen.board.GameBoardView;
import ADG.Games.Keezen.services.PollingService;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class GameModule {
    private final GameStateServiceAsync gameStateService = GWT.create(GameStateService.class);
    private final CardsServiceAsync cardsService = GWT.create(CardsService.class);
    private final PollingService pollingService = GWT.create(PollingService.class);
    private static final MovingServiceAsync movingService = GWT.create(MovingService.class);
    private GameBoardView gameBoardView;

    public void onStart() {

        // Create a Canvas for the Parcheesi board
        if(Canvas.createIfSupported()==null){
            RootPanel.get().add(new Label("Canvas is not supported in your browser."));
            return;
        }

        gameBoardView = new GameBoardView();
        RootPanel.get().clear();
        RootPanel.get().add(gameBoardView);
        //todo add model
        GameBoardPresenter gameBoardPresenter = new GameBoardPresenter(gameBoardView, gameStateService, cardsService, movingService, pollingService);
        gameBoardPresenter.start();
    }
}
