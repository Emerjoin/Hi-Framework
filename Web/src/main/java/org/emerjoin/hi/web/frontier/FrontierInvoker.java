package org.emerjoin.hi.web.frontier;

import org.emerjoin.hi.web.frontier.model.FrontierClass;
import org.emerjoin.hi.web.frontier.model.FrontierMethod;
import org.emerjoin.hi.web.frontier.model.MethodParam;

import javax.validation.ConstraintViolationException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mario Junior.
 */
public class FrontierInvoker {

    private FrontierClass frontier;
    private FrontierMethod method;
    private Map params;
    private Object returnedObject = new HashMap<>();


    public FrontierInvoker(FrontierClass frontierClass, FrontierMethod method, Map params){

        this.frontier = frontierClass;
        this.method = method;
        this.params = params;

    }

    public void setReturnedObject(Object value){

        if(value==null)
            throw new IllegalArgumentException("Returned value must not be null");
        this.returnedObject = value;

    }

    public Object[] getCallArguments(){
        MethodParam methodParams[] = method.getParams();
        Object[] callParams = new Object[params.size()];
        int i = 0;
        for(MethodParam methodParam: methodParams){
            Object paramValue = params.get(methodParam.getName());
            callParams[i] = paramValue;
            i++;
        }
        return callParams;
    }

    public boolean invoke() throws Exception {

        return this.invoke(getCallArguments());

    }

    public boolean invoke(Object[] arguments) throws Exception {

        Object refreshedObj = frontier.getObject();
        try {

            returnedObject = method.getMethod().invoke(refreshedObj, arguments);

        }catch (Throwable ex){

            if(ex instanceof InvocationTargetException){
                Throwable throwable = ex.getCause();
                if(throwable instanceof ConstraintViolationException)
                    throw (ConstraintViolationException) throwable;
            }

            throw ex;

        }
        return true;
    }

    public Object getReturnedObject() {

        return returnedObject;

    }
}
