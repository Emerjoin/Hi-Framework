package org.emerjoin.hi.web.events.sse;

import org.emerjoin.hi.web.events.HiEvent;

/**
 * @author Mario Junior.
 */
public abstract class AbstractChannelEvent extends HiEvent {

    private String userId;
    private String channel;

    public AbstractChannelEvent(String userId, String channel){
        this.userId = userId;
        this.channel = channel;
    }

    public String getUserId() {
        return userId;
    }

    public String getChannel() {
        return channel;
    }
}
