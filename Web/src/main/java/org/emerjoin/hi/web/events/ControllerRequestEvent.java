package org.emerjoin.hi.web.events;

import java.util.Map;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
public class ControllerRequestEvent extends MethodCallEvent {

    private Map<String,Object> arguments;

    public ControllerRequestEvent(boolean isAfter) {
        super(isAfter);
    }

    public ControllerRequestEvent(boolean isAfter, Map<String,Object> arguments) {
        super(isAfter);
        this.arguments = arguments;
    }

    public ControllerRequestEvent(){
        super();
    }

    public ControllerRequestEvent(Map<String,Object> arguments){
        super();
        this.arguments = arguments;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}
