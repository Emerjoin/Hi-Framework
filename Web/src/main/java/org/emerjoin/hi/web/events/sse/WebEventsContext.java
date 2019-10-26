package org.emerjoin.hi.web.events.sse;

/**
 * @author Mario Junior.
 */
public interface WebEventsContext {

    void publish(WebEvent event);
    void publish(WebEvent event, String... channels);

}
