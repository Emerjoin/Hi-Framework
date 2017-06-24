package org.emerjoin.hi.web.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is processed by Yayee annotations processor. Make sure the processor is correctly configured
 * in order to get the Frontiers working.
 */
//TODO: JavaDoc
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Frontier {

    public String name() default "";

}
