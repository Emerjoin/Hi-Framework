package org.emerjoin.hi.web.exceptions;

import javax.servlet.ServletException;

/**
 * Created by Mario Junior.
 */
public class HiException extends ServletException {

    public HiException(String m){

        super(m);

    }

    public HiException(String m, Throwable throwable){

        super(m,throwable);

    }

}
