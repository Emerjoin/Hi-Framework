package org.emerjoin.hi.web.events.sse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mario Junior.
 */
public class ListenersPool implements Serializable {

    private String name;
    private List<WebEventsListener> eventListeners = new ArrayList<>();

    ListenersPool(String name){
        this.name = name;
    }

    String getName() {
        return name;
    }

    void addListener(WebEventsListener consumer){
        this.eventListeners.add(consumer);
    }

    void remove(WebEventsListener consumer){
        eventListeners.remove(consumer);
    }

    List<WebEventsListener> getEventListeners(){
        return Collections.unmodifiableList(eventListeners);
    }

}
