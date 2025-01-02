package gwtks;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerList {
    private static int playerIdPlaying;
    private final GameStateServiceAsync gameStateService = GWT.create(GameStateService.class);
    private static ArrayList<Integer> activePlayers = new ArrayList<>();
    private static ArrayList<Integer> winners = new ArrayList<>();
    private static boolean isUpToDate = false;
    private static int nrPlayers;

    public static void refresh(){
        isUpToDate = false;
    }

    public void setPlayerIdPlayingAndDrawPlayerList(int playerIdPlaying) {
        if (playerIdPlaying == PlayerList.playerIdPlaying && isUpToDate) {
            return;
        }
        PlayerList.playerIdPlaying = playerIdPlaying;
        drawPlayers();
        isUpToDate = true;
    }
    public static void setNrPlayers(int nrPlayers){
        PlayerList.nrPlayers = nrPlayers;
    }

    public static void setActivePlayers(ArrayList<Integer> activePlayers){
        PlayerList.activePlayers = activePlayers;
    }

    public static int getPlayerIdPlaying() {
        return playerIdPlaying;
    }

    public void drawPlayers(){
        String[] playerNames = {"Player1", "Player2", "Player3", "Player4", "Player5", "Player6", "Player7", "Player8"};
        String inactiveColor = "#c2bfb6";
        List<Integer> column1 = Arrays.asList(0, 1, 2, 3);
        List<Integer> column2 = Arrays.asList(4, 5, 6, 7);
        int colCount = 2;
        int rowCount = (int) Math.ceil((double) playerNames.length / colCount);
        Grid grid = new Grid(rowCount, colCount);
        RootPanel.get("playerListContainer").clear();
        // todo: to view
        GameBoardView gameBoardView = new GameBoardView();
        gameBoardView.getPlayerListContainer().clear();

        for (int playerId = 0; playerId < nrPlayers; playerId++) {
            int imagePixelSize = 50;

            ImageElement img = Document.get().createImageElement();
            img.setSrc("/profilepics.png");
            HorizontalPanel hp = new HorizontalPanel();
            Label playerName = new Label(playerNames[playerId]);
            playerName.getElement().getStyle().setMarginRight(30, Style.Unit.PX);
            playerName.getElement().getStyle().setMarginLeft(10, Style.Unit.PX);

            if(!activePlayers.contains(playerId) && !winners.contains(playerId)){
                playerName.getElement().getStyle().setTextDecoration(Style.TextDecoration.LINE_THROUGH);
            }

            hp.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
            hp.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

            Canvas canvas = Canvas.createIfSupported();
            canvas.setWidth(imagePixelSize + "px");
            canvas.setHeight(imagePixelSize + "px");
            canvas.setCoordinateSpaceWidth(imagePixelSize);
            canvas.setCoordinateSpaceHeight(imagePixelSize);
            canvas.setStyleName("profilepic");
            if(activePlayers.contains(playerId)){
                canvas.getElement().getStyle().setBorderColor(PlayerColors.getHexColor(playerId));
            }else{
                canvas.getElement().getStyle().setBorderColor(inactiveColor);
            }
            Context2d ctx = canvas.getContext2d();
            // source image
            double sw = 1024/4.0;
            double sh = 1024/4.0;
            double sx = sw*(playerId%4);
            double sy = sh*(playerId > 3 ? 1:0);
            // destination
            double dy = 0-5/2;
            double dx = 0-5/2;
            double dw = imagePixelSize+5;
            double dh = imagePixelSize+5;
            GWT.log("source image is "+sx+","+sh+", "+playerId);
            GWT.log("card size = "+dh+","+dw);
            // for spritesheets dx dy
            ctx.drawImage(img, sx,sy,sw,sh,dx,dy,dw,dh);
            canvas.asWidget().setStyleName("profilepic");
            if(playerId == playerIdPlaying) {
                hp.asWidget().setStyleName("playerPlaying");
            }

            // winners can be in either column 1 or 2: only add a medal to the winner,
            // but add an empty canvas to the other players in that column so that they are aligned
            if(!winners.isEmpty() &&
                    ((column1.contains(playerId) && winners.stream().anyMatch(c -> c < 4)) ||
                    (column2.contains(playerId) && winners.stream().anyMatch(c -> c > 4)))){
                int i = winners.indexOf(playerId);
                Canvas canvasMedal = Canvas.createIfSupported();
                canvasMedal.setWidth(imagePixelSize + "px");
                canvasMedal.setHeight(imagePixelSize + "px");
                canvasMedal.setCoordinateSpaceWidth(imagePixelSize);
                canvasMedal.setCoordinateSpaceHeight(imagePixelSize);

                ImageElement imgMedals = Document.get().createImageElement();
                imgMedals.setSrc("/medals.png");

                // image has 220px empty space on top and botoom
                double sw1 = 2500/3.0;
                double sh1 = 1668-440;
                double sx1 = sw1*i;
                double sy1 = 220;
                // destination
                double dy1 = 0;
                double dx1 = 0;
                double dh1 = imagePixelSize+5;
                double dw1 = dh1/sh1*sw1;

                Context2d ctxMedals = canvasMedal.getContext2d();
                if(winners.contains(playerId)){
                    ctxMedals.drawImage(imgMedals, sx1,sy1,sw1,sh1,dx1,dy1,dw1,dh1);
                }
                hp.add(canvasMedal.asWidget());
            }
            hp.add(canvas.asWidget());
            hp.add(playerName);
            int row = playerId % 4;
            int col = (playerId > 3) ? 1 : 0;
            grid.setWidget(row, col, hp);

        }
        // todo: old
        grid.getElement().getStyle().setMargin(50, Style.Unit.PX);
        RootPanel.get("playerListContainer").add(grid);
        //todo: new
//        gameBoardView.getPlayerListContainer().add(grid); uncommenting demolishes the other grid
    }

    public static void setWinners(ArrayList<Integer> winners) {
        PlayerList.winners = winners;
    }
}
