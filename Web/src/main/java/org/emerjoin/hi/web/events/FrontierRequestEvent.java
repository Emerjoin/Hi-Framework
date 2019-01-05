package org.emerjoin.hi.web.events;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
public class FrontierRequestEvent extends MethodCallEvent {

    private Object override = null;
    private Object[] arguments;

    public FrontierRequestEvent(boolean isAfter) {
        super(isAfter);
    }

    public FrontierRequestEvent(boolean isAfter, Object[] arguments){
        super(isAfter);
        this.arguments = arguments;
    }

    public FrontierRequestEvent(Object[] arguments){
        super();
        this.arguments = arguments;
    }

    public FrontierRequestEvent(){

        super();

    }

    public void overrideResult(Object value){
        if(value==null)
            throw new IllegalArgumentException("Value object instance must not be null");
        this.override = value;

    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public boolean valueOverriden(){

        return this.override!=null;

    }

    public Object getOverrideValue(){

        return this.override;

    }


}
