package org.emerjoin.hi.web.events.sse;

import org.emerjoin.hi.web.AppContext;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Mario Junior.
 */
class WebEventsListener {

    private AsyncContext context;
    private String name = "Unknown";
    private String userId;

    WebEventsListener(AsyncContext context){
        this(context,"Unknown");
    }

    WebEventsListener(AsyncContext context, String userId){
        HttpServletRequest request = (HttpServletRequest) context.getRequest();
        String browser = request.getHeader("User-Agent");
        if(browser!=null&&!browser.isEmpty())
            this.name = browser;
        this.context = context;
        this.userId = userId;
    }

    void deliver(WebEvent event){
        try {
            String eventJson = AppContext.createGson().toJson(event);
            PrintWriter writer = context.getResponse().getWriter();
            writer.write("event: " + event.getClass().getSimpleName() + "\n");
            writer.write("data: " + eventJson + "\n\n");
            writer.flush();
        }catch (IOException ex){
            throw new RuntimeException("error getting response writer",
                    ex);
        }
    }

    void setReconnectInterval(long interval){
        try {
            PrintWriter writer = context.getResponse().getWriter();
            writer.write("retry: " + interval + "\n\n");
            writer.flush();
        }catch (IOException ex){
            throw new RuntimeException("error getting response writer",
                    ex);
        }
    }

    String getUserId(){

        return this.userId;

    }

    void dispose(){
        this.context.complete();
    }

    @Override
    public String toString() {
        return name;
    }
}
