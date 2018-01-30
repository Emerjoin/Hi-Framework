package org.emerjoin.hi.web.events;

import com.google.gson.GsonBuilder;

/**
 * @author Mário Júnior
 */
public class GsonInitEvent extends HiEvent {

    private GsonBuilder builder;

    public GsonInitEvent(GsonBuilder builder){
        if(builder==null)
            throw new IllegalArgumentException("builder reference must not be null");
        this.builder  = builder;
    }

    public GsonBuilder getBuilder(){

        return this.builder;

    }

}
