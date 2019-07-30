package org.emerjoin.hi.web.events;

import org.emerjoin.hi.web.AppContext;

/**
 * @author Mário Júnior
 */
public class ApplicationStartupEvent extends HiEvent {

    private AppContext context;

    public ApplicationStartupEvent(AppContext context){

        this.context = context;

    }

    public AppContext getContext() {
        return context;
    }
}
