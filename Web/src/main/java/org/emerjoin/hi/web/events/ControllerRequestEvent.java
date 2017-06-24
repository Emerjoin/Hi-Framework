package org.emerjoin.hi.web.events;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
public class ControllerRequestEvent extends MethodCallEvent {


    public ControllerRequestEvent(boolean isAfter) {
        super(isAfter);
    }

    public ControllerRequestEvent(){

        super();

    }

}
