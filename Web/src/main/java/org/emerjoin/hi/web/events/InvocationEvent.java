package org.emerjoin.hi.web.events;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
public abstract class InvocationEvent extends HiEvent {

    private boolean after;

    public InvocationEvent(boolean isAfter){

        this.after = isAfter;

    }

    public InvocationEvent(){



    }


    public boolean isAfter(){

        return after;

    }

    public boolean isBefore(){

        return !after;

    }

    public void setAfter(){

        this.after = true;

    }

    public void setBefore(){

        this.after = false;

    }

}
