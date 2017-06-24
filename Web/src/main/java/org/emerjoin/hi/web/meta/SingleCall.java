package org.emerjoin.hi.web.meta;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
//TODO: JavaDoc
public @interface SingleCall {

    public enum  Detection {

        METHOD_CALL,CALL_PARAMS;

    }

    public enum  AbortPolicy{

        ABORT_NEW_INVOCATION, ABORT_ONGOING_INVOCATION;

    }

    public Detection detectionMethod() default Detection.METHOD_CALL;
    public AbortPolicy abortionPolicy() default AbortPolicy.ABORT_ONGOING_INVOCATION;

}
