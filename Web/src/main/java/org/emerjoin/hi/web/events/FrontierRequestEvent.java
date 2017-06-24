package org.emerjoin.hi.web.events;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
public class FrontierRequestEvent extends MethodCallEvent {


    public FrontierRequestEvent(boolean isAfter) {
        super(isAfter);
    }

    public FrontierRequestEvent(){

        super();

    }


}
