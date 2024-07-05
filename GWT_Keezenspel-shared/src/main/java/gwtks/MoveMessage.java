package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MoveMessage implements IsSerializable {
        private Integer playerId;
        private Integer pawnId1;
        private Integer pawnId2;
        private MoveType moveType;
        private Integer stepsPawn1;
        private Integer stepsPawn2;

        public Integer getPlayerId() {
            return playerId;
        }

        public void setPlayerId(Integer playerId) {
            this.playerId = playerId;
        }

        public Integer getPawnId1() {
            return pawnId1;
        }

        public void setPawnId1(Integer pawnId1) {
            this.pawnId1 = pawnId1;
        }

        public Integer getPawnId2() {
            return pawnId2;
        }

        public void setPawnId2(Integer pawnId2) {
            this.pawnId2 = pawnId2;
        }

        public MoveType getMoveType() {
            return moveType;
        }

        public void setMoveType(MoveType moveType) {
            this.moveType = moveType;
        }

        public Integer getStepsPawn1() {
            return stepsPawn1;
        }

        public void setStepsPawn1(Integer stepsPawn1) {
            this.stepsPawn1 = stepsPawn1;
        }

        public Integer getStepsPawn2() {
            return stepsPawn2;
        }

        public void setStepsPawn2(Integer stepsPawn2) {
            this.stepsPawn2 = stepsPawn2;
        }
}

