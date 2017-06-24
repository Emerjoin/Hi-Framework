package org.emerjoin.hi.web.exceptions;

/**
 * Created by Mario Junior.
 */
public class NoCDIScopeException extends TutorialException {

    public NoCDIScopeException(Class clazz){

        super("Class <"+clazz.getCanonicalName()+"> was not assigned a CDI Scope.");

    }

    @Override
    public String getContentPath() {

        return null;

    }
}
