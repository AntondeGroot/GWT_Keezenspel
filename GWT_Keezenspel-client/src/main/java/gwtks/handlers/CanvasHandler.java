package gwtks.handlers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class CanvasHandler  {

    public void addHandler(){
        // add click handler to canvasContainer
        NodeList<Element> elements = com.google.gwt.dom.client.Document.get().getElementsByTagName("div");

        for (int i = 0; i < elements.getLength(); i++) {
            Element element = elements.getItem(i);
            if (element.hasClassName("canvasWrapper")) {
                addClickListener(element);
                break;  // Assuming you only need the first element with this class
            }
        }
    }

    private void addClickListener(Element element) {
        DOM.sinkEvents(element, Event.ONCLICK);
        DOM.setEventListener(element, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (event.getTypeInt() == Event.ONCLICK) {
                    // Your click handling logic here
                    handleClick(event);
                }
            }
        });
    }

    private void handleClick(Event event) {
        int x = event.getClientX();
        int y = event.getClientY();

        if(y>600){
            handleOnCanvasClick(x,y);
        }else{
            handleOnBoardClick(x,y);
        }
    }

    private void handleOnCanvasClick(int x, int y){
        GWT.log("Clicked on Canvas");
    }

    private void handleOnBoardClick(int x, int y){
        GWT.log("Clicked on Board");
    }
}
