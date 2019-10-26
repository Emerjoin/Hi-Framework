package org.emerjoin.hi.web.events.sse;

import org.emerjoin.hi.web.ActiveUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.AsyncContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mario Junior.
 */
@ApplicationScoped
public class WebEventsController {

    private List<WebEventsListener> listeners = new ArrayList<>();
    private Map<String,ListenersPool> pools = new ConcurrentHashMap<>();
    private Map<String,List<WebEventsListener>> userListenersMap = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(WebEventsController.class);

    public void execute(WebEventPublishRequest request){
        if(request==null)
            throw new IllegalArgumentException("request must not be null");
        if(request.getChannelsList().isEmpty()) {
            this.publish(request.getEvent());
            return;
        }
        this.publish(request.getEvent(),request.getChannelsArray());
    }

    public void quitChannel(String userId, String channel){
        if(userId==null||userId.isEmpty())
            throw new IllegalArgumentException("userId reference must not be null nor empty");
        if(channel==null||channel.isEmpty())
            throw new IllegalArgumentException("channel reference must not be null nor empty");
        LOGGER.info(String.format("User [%s] quitting channel [%s]",userId,channel));
        ListenersPool consumersPool = pools.get(channel);
        if(consumersPool==null){
            LOGGER.warn(String.format("There is no listeners pool named [%s]",channel));
            return;
        }
        List<WebEventsListener> listenerList = userListenersMap.get(userId);
        if(listenerList==null){
            LOGGER.info("No Listeners found for user with Id="+userId);
            return;
        }
        LOGGER.info(String.format("Detaching %d listener(s) from channel",listenerList.size()));
        for(WebEventsListener listener: listenerList){
            consumersPool.remove(listener);
        }
    }

    public void joinChannel(String userId, String channel){
        if(userId==null||userId.isEmpty())
            throw new IllegalArgumentException("userId reference must not be null nor empty");
        if(channel==null||channel.isEmpty())
            throw new IllegalArgumentException("channel reference must not be null nor empty");
        LOGGER.info(String.format("User [%s] joining channel [%s]",userId,channel));
        ListenersPool consumersPool = pools.get(channel);
        if(consumersPool==null){
            consumersPool= new ListenersPool(channel);
            pools.put(channel,consumersPool);
        }
        List<WebEventsListener> listenerList = userListenersMap.get(userId);
        if(listenerList==null||listenerList.isEmpty()){
            LOGGER.info("There are no listeners bound to user with Id="+userId);
            return;
        }else LOGGER.info(String.format("Adding %d listener(s) to channel",listenerList.size()));
        for(WebEventsListener listener: listenerList){
            consumersPool.addListener(listener);
        }
    }

    WebEventsListener addListener(ActiveUser activeUser, AsyncContext context){
        String userId = activeUser.getUniqueId();
        WebEventsListener listener = new WebEventsListener(context, userId);
        LOGGER.info(String.format("Listener [%s] joining...",listener));
        listeners.add(listener);
        List<WebEventsListener> eventsListeners = userListenersMap.get(userId);
        if(eventsListeners==null){
            eventsListeners = Collections.synchronizedList(new ArrayList<>());
            userListenersMap.put(userId,eventsListeners);
        }
        eventsListeners.add(listener);
        joinListener(listener,activeUser.getWebEventChannel());
        for(String channel: activeUser.getWebEventSubscriptions()){
            joinListener(listener,channel);
        }
        return listener;
    }

    private void joinListener(WebEventsListener listener, String channel){
        ListenersPool pool = pools.get(channel);
        LOGGER.info(String.format("Adding Listener [%s] to channel [%s]",listener,channel));
        if(pool==null){
            pool = new ListenersPool(channel);
            pools.put(channel,pool);
        }
        pool.addListener(listener);
    }

    private void publish(WebEvent event, String... channels){
        Collection<ListenersPool> listenerPools = getConsumerPools(channels);
        Collection<WebEventsListener> listeners = getListeners(listenerPools);
        this.deliver(event,listeners);
    }

    private void publish(WebEvent event){
        Collection<WebEventsListener> consumers = getListeners(pools.values());
        this.deliver(event,consumers);
    }

    private Collection<WebEventsListener> getListeners(String... channels){
        Collection<ListenersPool> consumersPools = getConsumerPools(
                channels);
        return getListeners(consumersPools);
    }

    private Collection<WebEventsListener> getListeners(Collection<ListenersPool> listenerPools){
        Collection<WebEventsListener> eventListeners = new ArrayList<>();
        for(ListenersPool pool: listenerPools){
            for(WebEventsListener listener: pool.getEventListeners()){
                if(!eventListeners.contains(listener))
                    eventListeners.add(listener);
            }
        }
        return eventListeners;
    }

    private Collection<ListenersPool> getConsumerPools(String... channels){
        List<ListenersPool> poolList = new ArrayList<>();
        for(String channel: channels){
            ListenersPool pool = pools.get(channel);
            if(pool==null){
                LOGGER.warn(String.format("There is no Listeners pool with name=[%s]",channel));
                continue;
            }
            poolList.add(pool);
        }
        return poolList;
    }

    private void deliver(WebEvent event, Collection<WebEventsListener> listeners){
        for(WebEventsListener listener: listeners){
            try {
                listener.deliver(event);
                LOGGER.info(String.format("[%s] delivered successfully",event.getClass().getSimpleName()));
            }catch (Exception ex){
                LOGGER.error("error found while sending message to Listener",ex);
                Collection<ListenersPool> poolsList = pools.values();
                for(ListenersPool pool: poolsList)
                    pool.remove(listener);
                listeners.remove(listener);
                List<WebEventsListener> eventsListeners = userListenersMap.get(listener.getUserId());
                if(eventsListeners!=null)
                    eventsListeners.remove(listener);
                listener.dispose();
            }
        }
    }


}
