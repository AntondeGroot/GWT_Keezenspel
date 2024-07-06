package gwtks;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.List;
import java.util.Objects;

public class Pawn implements IsSerializable {
    private int playerId;
    private PawnId pawnId;
    private Point currentPosition;
    private Point nestTilePosition;

    public Pawn(PawnId pawnId, Point nestTilePosition) {
        this.playerId = pawnId.getPlayerId();
        this.pawnId = pawnId;
        this.nestTilePosition = new Point(nestTilePosition);
        this.currentPosition = new Point(nestTilePosition);
    }

    public PawnId getPawnId() {
        return pawnId;
    }

    public void setPawnId(PawnId pawnId) {
        this.pawnId = pawnId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public Point getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Point currentPosition) {
        if(currentPosition != null) {
            this.currentPosition = currentPosition;
        }
    }

    public Point getNestTilePosition() {
        return nestTilePosition;
    }

    public void setNestTilePosition(Point nestTilePosition) {
        this.nestTilePosition = nestTilePosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pawn pawn = (Pawn) o;
        return playerId == pawn.playerId && Objects.equals(nestTilePosition, pawn.nestTilePosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, nestTilePosition);
    }
}
