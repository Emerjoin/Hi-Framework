package org.emerjoin.hi.web.config;

/**
 * @author Mario Junior.
 */
public class Events {

    private long reconnectInterval = 2000;//2 seconds

    public long getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(long reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }
}
