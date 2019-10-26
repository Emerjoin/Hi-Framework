package org.emerjoin.hi.web.events.sse;

/**
 * @author Mario Junior.
 */
public class ChannelJoinEvent extends AbstractChannelEvent {

    public ChannelJoinEvent(String userId, String channel) {
        super(userId, channel);
    }

}
