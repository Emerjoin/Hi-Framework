package org.emerjoin.hi.web.events;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
public class FrontierRequestEvent extends MethodCallEvent {

    private Object override = null;

    public FrontierRequestEvent(boolean isAfter) {
        super(isAfter);
    }

    public FrontierRequestEvent(){

        super();

    }

    public void overrideResult(Object value){
        if(value==null)
            throw new IllegalArgumentException("Value object instance must not be null");
        this.override = value;

    }


    public boolean valueOverriden(){

        return this.override!=null;

    }

    public Object getOverrideValue(){

        return this.override;

    }


}
