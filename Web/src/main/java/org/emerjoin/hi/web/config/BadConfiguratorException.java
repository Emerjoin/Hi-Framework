package org.emerjoin.hi.web.config;

import org.emerjoin.hi.web.exceptions.HiException;

/**
 * @author Mário Júnior
 */
public class BadConfiguratorException extends HiException {

    public BadConfiguratorException(String message){

        super(message);

    }

    public BadConfiguratorException(String message, Throwable ex){

        super(message,ex);

    }

}
