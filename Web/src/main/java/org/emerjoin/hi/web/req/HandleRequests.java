package org.emerjoin.hi.web.req;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Mario Junior.
 */


@Retention(RetentionPolicy.RUNTIME)
public @interface HandleRequests {


    public String regexp();
    public boolean supportPostMethod() default false;

}
