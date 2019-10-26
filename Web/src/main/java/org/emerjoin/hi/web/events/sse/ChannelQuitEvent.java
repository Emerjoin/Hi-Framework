package org.emerjoin.hi.web.events.sse;

/**
 * @author Mario Junior.
 */
public class ChannelQuitEvent extends AbstractChannelEvent {

    public ChannelQuitEvent(String userId, String channel) {
        super(userId, channel);
    }
}
