package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class MoveMessage implements IsSerializable {
        private PawnId pawnId1;
        private PawnId pawnId2;
        private TileId tileId; // should be removed as this should be stored in the backend not frontend
        private MoveType moveType;
        private Integer stepsPawn1;
        private Integer stepsPawn2;

        public TileId getTileId() {
                return tileId;
        }

        public void setTileId(TileId tileId) {
                this.tileId = tileId;
        }

        public PawnId getPawnId1() {
                return pawnId1;
        }

        public void setPawnId1(PawnId pawnId1) {
                this.pawnId1 = pawnId1;
        }

        public PawnId getPawnId2() {
                return pawnId2;
        }

        public void setPawnId2(PawnId pawnId2) {
                this.pawnId2 = pawnId2;
        }

        public MoveType getMoveType() {
                return moveType;
        }

        public void setMoveType(MoveType moveType) {
                this.moveType = moveType;
        }

        public Integer getStepsPawn1() {
                return stepsPawn1 == null ? 0 : stepsPawn1;
        }

        public void setStepsPawn1(Integer stepsPawn1) {
                this.stepsPawn1 = stepsPawn1;
        }

        public Integer getStepsPawn2() {
                return stepsPawn2 == null ? 0 : stepsPawn2;
        }

        public void setStepsPawn2(Integer stepsPawn2) {
                this.stepsPawn2 = stepsPawn2;
        }

        @Override
        public String toString() {
                return "MoveMessage{" +
                        "pawnId1=" + pawnId1 +
                        ", pawnId2=" + pawnId2 +
                        ", moveType=" + moveType +
                        ", stepsPawn1=" + stepsPawn1 +
                        ", stepsPawn2=" + stepsPawn2 +
                        '}';
        }
}

