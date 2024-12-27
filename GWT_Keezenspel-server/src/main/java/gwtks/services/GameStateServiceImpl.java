package gwtks.services;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import gwtks.*;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;

@SuppressWarnings("serial")
@WebServlet("/app/gamestate")
public class GameStateServiceImpl extends RemoteServiceServlet implements GameStateService {

    @Override
    public GameStateResponse getGameState(){
        GameStateResponse response = new GameStateResponse();
        response.setNrPlayers(GameState.getNrPlayers());
        response.setPawns(GameState.getPawns());
        response.setPlayerIdTurn(GameState.getPlayerIdTurn());
        response.setActivePlayers(GameState.getActivePlayers());
        response.setWinners(GameState.getWinners());
        return response;
    }

    @Override
    public void startGame(){
        // todo: create N players, and N playerIds, one of which is the current player
        // todo: create a Board
        // todo: create the Gamestate
        // todo; the gamestate should include the Model
    }
}
