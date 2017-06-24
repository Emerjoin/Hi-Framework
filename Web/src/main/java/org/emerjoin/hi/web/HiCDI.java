package org.emerjoin.hi.web;

import org.emerjoin.hi.web.exceptions.NoCDIScopeException;

import javax.enterprise.context.*;
import java.lang.annotation.Annotation;

/**
 * Created by Mario Junior.
 */
public class HiCDI {

    public static void shouldHaveCDIScope(Class clazz) throws NoCDIScopeException{

        //TODO: This shouldn't throw and exception because there might be custom scopes
        Annotation scope1 = clazz.getAnnotation(RequestScoped.class);
        Annotation scope2 = clazz.getAnnotation(ApplicationScoped.class);
        Annotation scope3 = clazz.getAnnotation(SessionScoped.class);
        Annotation scope4 = clazz.getAnnotation(ConversationScoped.class);
        Annotation scope5 = clazz.getAnnotation(Dependent.class);

        if(scope1==null&&scope2==null&&scope3==null&&scope4==null&&scope5==null)
            throw new NoCDIScopeException(clazz);

    }

}
