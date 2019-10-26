package org.emerjoin.hi.web.events.sse;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Mario Junior.
 */
public class WebEventPublishRequest implements Serializable {

    private WebEvent event;
    private List<String> channelsList = Collections.emptyList();

    public WebEventPublishRequest(WebEvent event){
        this(event,new String[]{});
    }

    public WebEventPublishRequest(WebEvent event, String... channels){
        if(event==null)
            throw new IllegalArgumentException("event must not be null");
        if(channels!=null&&channels.length>0)
            channelsList = Arrays.asList(channels);
        this.event = event;
    }

    public WebEvent getEvent() {
        return event;
    }

    List<String> getChannelsList() {
        return channelsList;
    }

    String[] getChannelsArray(){
        String[] array = new String[channelsList.size()];
        channelsList.toArray(array);
        return array;
    }
}
