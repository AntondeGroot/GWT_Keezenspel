package gwtks;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CardsServiceAsync {
    void getCards(AsyncCallback<CardResponse> callback)
            throws IllegalArgumentException;
}
