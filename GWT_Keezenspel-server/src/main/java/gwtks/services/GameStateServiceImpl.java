package gwtks.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
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
        response.setWinners(GameState.getWinners());
        return response;
    }

    @Override
    public ArrayList<Player> getPlayers(){
        return GameState.getPlayers();
    }

    public synchronized Player addPlayer(Player player){
        GameState.addPlayer(player);
        return player;
    }

    @Override
    public void startGame() throws IllegalStateException {
        //todo: remove testdata
        if(GameState.getPawns().isEmpty()){
            for (int i = 0; i < 3; i++) {
                Player player = new Player("player"+i,"123-567");
                if(i==0){
                    player.setIsPlaying(true);
                }
                GameState.addPlayer(player);
            }
        }
        //todo: remove testdata
        //todo: replace with isRunning method
        if(GameState.getPawns().isEmpty()){
            GameState.start();
        }
    }
}
