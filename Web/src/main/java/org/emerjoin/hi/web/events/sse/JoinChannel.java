package org.emerjoin.hi.web.events.sse;

import org.emerjoin.hi.web.ActiveUser;

/**
 * @author Mario Junior.
 */
public class JoinChannel extends UserSubscriptionEvent {

    public JoinChannel(ActiveUser user, String channel) {
        super(user, channel);
    }

}
