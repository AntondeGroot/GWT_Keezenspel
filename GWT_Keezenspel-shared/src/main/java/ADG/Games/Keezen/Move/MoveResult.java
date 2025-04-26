package ADG.Games.Keezen.Move;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum MoveResult implements IsSerializable {
    CAN_MAKE_MOVE, CANNOT_MAKE_MOVE, PLAYER_DOES_NOT_HAVE_CARD, INVALID_SELECTION
}

