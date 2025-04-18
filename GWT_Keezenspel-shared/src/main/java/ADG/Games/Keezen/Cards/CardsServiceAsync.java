package ADG.Games.Keezen.Cards;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CardsServiceAsync {
    void getCards(String playerUUID, AsyncCallback<CardResponse> callback)
            throws IllegalArgumentException;
}
