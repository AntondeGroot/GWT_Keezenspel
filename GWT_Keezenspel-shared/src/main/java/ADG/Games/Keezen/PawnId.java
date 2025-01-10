package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Objects;

public class PawnId implements IsSerializable {
    private String playerId;
    private int pawnNr;

    public PawnId(String playerId, int pawnNr) {
        this.playerId = playerId;
        this.pawnNr = pawnNr;
    }

    public PawnId() {
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getPawnNr() {
        return pawnNr;
    }

    public void setPawnNr(int pawnNr) {
        this.pawnNr = pawnNr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PawnId pawnId = (PawnId) o;
        return playerId == pawnId.playerId && pawnNr == pawnId.pawnNr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, pawnNr);
    }

    @Override
    public String toString() {
        return "PawnId{" + playerId +"," + pawnNr +'}';
    }
}
