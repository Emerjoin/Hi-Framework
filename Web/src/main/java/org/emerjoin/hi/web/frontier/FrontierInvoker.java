package org.emerjoin.hi.web.frontier;

import org.emerjoin.hi.web.frontier.model.FrontierClass;
import org.emerjoin.hi.web.frontier.model.FrontierMethod;
import org.emerjoin.hi.web.frontier.model.MethodParam;

import javax.validation.ConstraintViolationException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by Mario Junior.
 */
public class FrontierInvoker {

    private FrontierClass frontier;
    private FrontierMethod method;
    private Map params;
    private Object returnedObject;

    public FrontierInvoker(FrontierClass frontierClass, FrontierMethod method, Map params){

        this.frontier = frontierClass;
        this.method = method;
        this.params = params;

    }


    public boolean invoke() throws Exception {

        MethodParam methodParams[] = method.getParams();
        Object[] invocationParams = new Object[params.size()];

        int i = 0;
        for(MethodParam methodParam: methodParams){
            Object paramValue = params.get(methodParam.getName());
            invocationParams[i] = paramValue;
            i++;
        }

        Object refreshedObj = frontier.getObject();
        try {

            returnedObject = method.getMethod().invoke(refreshedObj, invocationParams);

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
