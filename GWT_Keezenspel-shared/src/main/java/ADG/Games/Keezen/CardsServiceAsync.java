package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CardsServiceAsync {
    void getCards(int playerId, AsyncCallback<CardResponse> callback)
            throws IllegalArgumentException;
}
