package org.emerjoin.hi.web.config;

import org.emerjoin.hi.web.exceptions.HiException;

/**
 * @author Mário Júnior
 */
public class BadConfigException extends HiException {

    public BadConfigException(String msg){

        super(msg);

    }

    public BadConfigException(String msg, Exception ex){

        super(msg,ex);

    }

}
