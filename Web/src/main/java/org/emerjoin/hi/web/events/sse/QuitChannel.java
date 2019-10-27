package org.emerjoin.hi.web.events.sse;

import org.emerjoin.hi.web.ActiveUser;

/**
 * @author Mario Junior.
 */
public class QuitChannel extends UserSubscriptionEvent {

    public QuitChannel(ActiveUser user, String channel) {
        super(user, channel);
    }
}
