package gwtks.services;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import gwtks.*;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;

@SuppressWarnings("serial")
@WebServlet("/app/gamestate")
public class GameStateServiceImpl extends RemoteServiceServlet implements GameStateService {

    @Override
    public GameStateResponse getGameState() throws IllegalArgumentException {
        GameStateResponse response = new GameStateResponse();
        response.setNrPlayers(GameState.getNrPlayers());
        response.setPawns(GameState.getPawns());
        response.setPlayerIdTurn(GameState.getPlayerIdTurn());
        response.setActivePlayers(GameState.getActivePlayers());
        return response;
    }
}
