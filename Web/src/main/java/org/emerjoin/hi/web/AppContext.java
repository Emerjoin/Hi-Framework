package org.emerjoin.hi.web;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.emerjoin.hi.web.boot.BootAgent;
import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.events.GsonInitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.Serializable;

@ApplicationScoped
public class AppContext implements Serializable {

    private static GsonBuilder GSON_BUILDER = null;

    static {

        GSON_BUILDER =new GsonBuilder();

    }

    private static String assetVersionPrefix = ".vd3p1d";
    private static final Logger LOGGER = LoggerFactory.getLogger(AppContext.class);

    public static void setAssetVersionPrefix(String prefix) {

        assetVersionPrefix = prefix;

    }


    @Inject
    private BootAgent bootAgent;

    @Inject
    private Event<GsonInitEvent> gsonInitEvent;

    public String getAssetVersionToken(){

       return  assetVersionPrefix+String.valueOf(getDeployId());

    }

    public String getDeployId(){

        return bootAgent.getDeployId();

    }

    public AppConfigurations.DeploymentMode getDeployMode(){

        return AppConfigurations.get().getDeploymentMode();

    }


    @PostConstruct
    public void setup(){
        if(AppContext.GSON_BUILDER==null)
            AppContext.GSON_BUILDER = new GsonBuilder();
        //Fire the GsonInit event
        LOGGER.info("Firing GSON initialization event...");
        gsonInitEvent.fire(new GsonInitEvent(AppContext.GSON_BUILDER));
    }


    public Gson createGsonInstance(){

        return AppContext.createGson();


    }

    public GsonBuilder getGsonBuilderInstance(){

        return AppContext.getGsonBuilder();

    }


    public static GsonBuilder getGsonBuilder(){

        return AppContext.GSON_BUILDER;

    }


    public static Gson createGson(){

        return AppContext.GSON_BUILDER.create();

    }

    public static void setGsonBuilder(GsonBuilder builder){
        if(builder==null)
            throw new IllegalArgumentException("builder reference must not be null");
        AppContext.GSON_BUILDER = builder;

    }


}
