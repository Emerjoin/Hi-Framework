package org.emerjoin.hi.web.mvc.exceptions;

import org.emerjoin.hi.web.exceptions.HiException;

/**
 * Created by Mario Junior.
 */
public class MvcException extends HiException {

    public MvcException(String msg, Throwable throwable){

        super(msg,throwable);

    }

    public MvcException(String msg){

        super(msg);

    }

}
