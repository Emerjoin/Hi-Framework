package org.emerjoin.hi.web.mvc.exceptions;

import org.emerjoin.hi.web.exceptions.HiException;

/**
 * Created by Mario Junior.
 */
public class MissingResourcesLibException extends HiException {

    public MissingResourcesLibException(){

        super("The Yayee Resources Lib could not be found. Include de jar in your artifact or make it available on your application server");

    }

    public MissingResourcesLibException(Throwable cause){

        super("The Yayee Resources Lib could not be found. Include de jar in your artifact or make it available on your application server",cause);

    }

}
