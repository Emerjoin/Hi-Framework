package org.emerjoin.hi.web;

import org.emerjoin.hi.web.events.sse.UserSubscriptionEvent;
import org.emerjoin.hi.web.events.sse.JoinChannel;
import org.emerjoin.hi.web.events.sse.QuitChannel;
import org.emerjoin.hi.web.security.SecureTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
@SessionScoped
public class ActiveUser implements Serializable {

    private String uniqueId = null;
    private String webEventChannel = "default";
    private List<String> webEventSubscriptions = new ArrayList<>();
    private String csrfToken = "";
    private String eventsToken = "";
    private HashMap<String,Object> data = new HashMap<>();
    private static final Logger _log = LoggerFactory.getLogger(ActiveUser.class);
    private static final SecureTokenUtil csrfTokeUtil = new SecureTokenUtil();

    @Inject
    private transient Event<UserSubscriptionEvent> channelEvent;

    @PostConstruct
    public void init(){
        this.csrfToken = csrfTokeUtil.makeJwtToken();
        this.eventsToken = csrfTokeUtil.makeJwtToken();
        this.uniqueId = UUID.randomUUID().toString();
    }

    public void expireTokens(){
        this.csrfToken = csrfTokeUtil.makeJwtToken();
        this.eventsToken = csrfTokeUtil.makeJwtToken();
    }

    public String getEventsToken() {
        return eventsToken;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public Object getProperty(String name){

        return data.get(name);

    }

    public Object getProperty(String name,Object defaultValue){
        Object value = data.get(name);
        if(value==null)
            return defaultValue;
        return value;
    }

    public void setProperty(String name, Object value){

        data.put(name,value);

    }

    public List<String> getWebEventSubscriptions() {
        return Collections.unmodifiableList(
                webEventSubscriptions);
    }

    public void subscribe(String webEventChannel) {
        if (webEventChannel == null || webEventChannel.isEmpty())
            throw new IllegalArgumentException("webEventChannel must not be null nor empty");
        this.webEventSubscriptions.add(webEventChannel);
        channelEvent.fire(new JoinChannel(this,
                webEventChannel));
    }

    public void unsubscribe(String webEventChannel){
        if(webEventChannel==null||webEventChannel.isEmpty())
            throw new IllegalArgumentException("webEventChannel must not be null nor empty");
        if(this.webEventSubscriptions.remove(webEventChannel))
            channelEvent.fire(new QuitChannel(this,
                    webEventChannel));
    }

    public boolean isSubscribedTo(String webEventChannel){
        if(webEventChannel==null||webEventChannel.isEmpty())
            throw new IllegalArgumentException("webEventChannel must not be null nor empty");
        return this.webEventSubscriptions.contains(
                webEventChannel);
    }

    public String getWebEventChannel() {
        return webEventChannel;
    }

    public void setWebEventChannel(String webEventChannel) {
        this.webEventChannel = webEventChannel;
    }

    public String getUniqueId() {
        return uniqueId;
    }
}
