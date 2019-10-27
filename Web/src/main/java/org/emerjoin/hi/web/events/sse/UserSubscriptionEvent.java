package org.emerjoin.hi.web.events.sse;

import org.emerjoin.hi.web.ActiveUser;
import org.emerjoin.hi.web.events.HiEvent;

/**
 * @author Mario Junior.
 */
public abstract class UserSubscriptionEvent extends HiEvent {

    private ActiveUser user;
    private String channel;

    public UserSubscriptionEvent(ActiveUser user, String channel){
        this.user = user;
        this.channel = channel;
    }

    public ActiveUser getUser() {
        return user;
    }

    public String getChannel() {
        return channel;
    }
}
