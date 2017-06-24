package org.emerjoin.hi.web.events;

import org.emerjoin.hi.web.mvc.Controller;

import java.lang.reflect.Method;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
public abstract class MethodCallEvent extends InvocationEvent {

    private Class<? extends Controller> clazz = null;
    private Method method = null;

    public MethodCallEvent(boolean isAfter) {
        super(isAfter);
    }

    public MethodCallEvent(){

        super();

    }

    public Class<? extends Controller> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends Controller> clazz) {
        this.clazz = clazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
